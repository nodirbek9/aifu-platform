package uz.aifu.lms.progress.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.curriculum.app.LessonService;
import uz.aifu.lms.curriculum.app.ReadingTimeEstimator;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.progress.api.dto.ProgressDtos.LessonPathView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.ReadingBeatRequest;
import uz.aifu.lms.progress.app.PathRules.LessonPathState;
import uz.aifu.lms.progress.domain.CourseLearningPath;
import uz.aifu.lms.progress.domain.LessonProgress;
import uz.aifu.lms.progress.domain.ProgressStatus;
import uz.aifu.lms.progress.persistence.CourseLearningPathRepository;
import uz.aifu.lms.progress.persistence.LessonProgressRepository;

/**
 * Handles periodic reading heartbeats for learning path progress tracking (Phase 5, B6, ADR-015).
 *
 * <p>Server-measures reading time, enforces single-clock rules across parallel tabs, snapshots
 * required reading time on the first beat, marks sticky reached-end on the last visible block, and
 * delegates reevaluation to {@link LearningPathService}.
 */
@Service
public class ReadingService {

    private static final int MAX_OPTIMISTIC_RETRIES = 5;

    private final LessonProgressRepository lessonProgress;
    private final LessonService lessons;
    private final EnrollmentService enrollments;
    private final ReadingTimeEstimator readingTimeEstimator;
    private final LearningPathService learningPathService;
    private final CourseLearningPathRepository pathRepository;
    private final Clock clock;
    private final ReadingService self;

    public ReadingService(
            LessonProgressRepository lessonProgress,
            LessonService lessons,
            EnrollmentService enrollments,
            ReadingTimeEstimator readingTimeEstimator,
            LearningPathService learningPathService,
            CourseLearningPathRepository pathRepository,
            Clock clock,
            @Lazy ReadingService self) {
        this.lessonProgress = lessonProgress;
        this.lessons = lessons;
        this.enrollments = enrollments;
        this.readingTimeEstimator = readingTimeEstimator;
        this.learningPathService = learningPathService;
        this.pathRepository = pathRepository;
        this.clock = clock;
        this.self = self;
    }

    /**
     * Records a reading heartbeat for the caller's active enrollment with optimistic-lock retry.
     */
    public LessonPathView recordReadingBeat(
            UUID lessonId, ReadingBeatRequest request, UUID studentUserId) {
        return retrying(() -> self.doRecordReadingBeat(lessonId, request, studentUserId));
    }

    @Transactional
    public LessonPathView doRecordReadingBeat(
            UUID lessonId, ReadingBeatRequest request, UUID studentUserId) {
        Lesson lesson = lessons.require(lessonId);
        UUID courseId = lessons.courseIdOf(lessonId);
        Enrollment enrollment = enrollments.requireActiveEnrollment(courseId, studentUserId);

        learningPathService.requireLessonOpen(lessonId, enrollment.getId());

        Instant now = clock.instant();

        LessonProgress lp =
                lessonProgress
                        .findByEnrollment_IdAndLesson_Id(enrollment.getId(), lessonId)
                        .orElseGet(() -> new LessonProgress(enrollment, lesson));

        if (lp.getRequiredReadingSeconds() == null) {
            int estimatedSeconds = readingTimeEstimator.estimatedSeconds(lesson, null);
            BigDecimal fraction =
                    pathRepository
                            .findByCourseId(courseId)
                            .map(CourseLearningPath::getMinReadingFraction)
                            .orElse(new BigDecimal("0.50"));
            int required =
                    BigDecimal.valueOf(estimatedSeconds)
                            .multiply(fraction)
                            .setScale(0, RoundingMode.CEILING)
                            .intValueExact();
            lp.snapshotRequiredReadingSeconds(required);
        }

        Instant tenSecondsAgo = now.minusSeconds(10);
        boolean otherHeartbeatRecent =
                lessonProgress.hasOtherHeartbeatSince(enrollment.getId(), lessonId, tenSecondsAgo);
        if (!otherHeartbeatRecent) {
            lp.creditReading(now, 30, 10);
        } else {
            if (lp.getStatus() == ProgressStatus.NOT_STARTED) {
                lp.recordAccess(now, false, 0L);
            }
        }

        lp.updateMaxBlockPosition(request.maxBlockPosition());
        int lastVisible = lessons.lastVisibleBlockPosition(lessonId);
        if (request.atEnd() && request.maxBlockPosition() >= lastVisible) {
            lp.markReachedEnd(now);
        }

        lp = lessonProgress.saveAndFlush(lp);

        learningPathService.reevaluateLesson(enrollment, lesson);

        Map<UUID, LessonPathState> states =
                learningPathService.pathState(courseId, enrollment.getId());
        return learningPathService.lessonPathView(lessonId, states.get(lessonId), lp);
    }

    private <T> T retrying(Supplier<T> action) {
        int attempts = 0;
        while (true) {
            try {
                return action.get();
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
                attempts++;
                if (attempts >= MAX_OPTIMISTIC_RETRIES) {
                    throw Exceptions.conflict(
                            ErrorCode.CONCURRENT_MODIFICATION,
                            "Too many concurrent reading updates for this lesson; please retry");
                }
            }
        }
    }
}
