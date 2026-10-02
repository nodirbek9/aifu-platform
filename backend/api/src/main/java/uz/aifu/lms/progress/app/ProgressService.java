package uz.aifu.lms.progress.app;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uz.aifu.lms.coding.app.SubmissionService;
import uz.aifu.lms.coding.domain.Submission;
import uz.aifu.lms.coding.domain.SubmissionStatus;
import uz.aifu.lms.coding.domain.Verdict;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.events.QuizAttemptGradedEvent;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.curriculum.app.LessonService;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.progress.domain.ActivityProgress;
import uz.aifu.lms.progress.domain.ActivityProgressStatus;
import uz.aifu.lms.progress.domain.CourseProgress;
import uz.aifu.lms.progress.domain.LessonProgress;
import uz.aifu.lms.progress.domain.ModuleProgress;
import uz.aifu.lms.progress.domain.ProgressStatus;
import uz.aifu.lms.progress.persistence.ActivityProgressRepository;
import uz.aifu.lms.progress.persistence.CourseProgressRepository;
import uz.aifu.lms.progress.persistence.LessonProgressRepository;
import uz.aifu.lms.progress.persistence.ModuleProgressRepository;

/**
 * Four-level progress (docs/planning/phase-plan.md Phase 6, ADR-012). {@code lesson_progress} is
 * the source of truth a student's action writes to; module- and course-level rows are roll-ups
 * <b>recomputed from source on every write, never incremented</b> — the same COUNT query run twice
 * in a row yields the same answer, which is what makes "completing a lesson twice does not
 * double-count" true by construction rather than by a special-cased guard.
 *
 * <p>Concurrent writers can race on the same module/course roll-up row — same enrollment, different
 * lessons in the same module, completed at once. Two shapes of race are both possible and both
 * retried by {@link #recordLessonProgress} through {@code self}: an {@link
 * ObjectOptimisticLockingFailureException} when both racers found an existing row and one's {@code
 * version} is now stale, or a {@link DataIntegrityViolationException} on {@code
 * uq_module_progress}/{@code course_progress}'s primary key when this is the FIRST write for that
 * enrollment and both racers found no row and tried to insert one (a classic find-then-create
 * race). Same {@code self}-proxy technique as {@code EnrollmentService.bulkEnroll}, needed here
 * because a failed flush marks the enclosing transaction rollback-only, so each attempt must be its
 * own fresh transaction, not a retry inside one.
 */
@Service
public class ProgressService {

    private static final int MAX_OPTIMISTIC_RETRIES = 5;

    private final CourseProgressRepository courseProgress;
    private final ModuleProgressRepository moduleProgress;
    private final LessonProgressRepository lessonProgress;
    private final ActivityProgressRepository activityProgress;
    private final LessonService lessons;
    private final ActivityService activities;
    private final EnrollmentService enrollments;
    private final SubmissionService submissions;
    private final Clock clock;
    private final ProgressService self;

    public ProgressService(
            CourseProgressRepository courseProgress,
            ModuleProgressRepository moduleProgress,
            LessonProgressRepository lessonProgress,
            ActivityProgressRepository activityProgress,
            LessonService lessons,
            ActivityService activities,
            EnrollmentService enrollments,
            SubmissionService submissions,
            Clock clock,
            @Lazy ProgressService self) {
        this.courseProgress = courseProgress;
        this.moduleProgress = moduleProgress;
        this.lessonProgress = lessonProgress;
        this.activityProgress = activityProgress;
        this.lessons = lessons;
        this.activities = activities;
        this.enrollments = enrollments;
        this.submissions = submissions;
        this.clock = clock;
        this.self = self;
    }

    // ---------------------------------------------------------------- write path

    /**
     * Records a view (and, optionally, completion) of a lesson for the caller's own ACTIVE
     * enrollment, then rolls the change up into that enrollment's module- and course-level
     * progress. Idempotent: a second completion of an already-completed lesson leaves {@code
     * completedAt} and the roll-up counts untouched (view count and last-accessed time still
     * advance — a repeat view is a real view).
     */
    public LessonProgress recordLessonProgress(
            UUID lessonId,
            boolean markCompleted,
            long additionalTimeSpentSeconds,
            UUID studentUserId) {
        return retrying(
                () ->
                        self.doRecordLessonProgress(
                                lessonId,
                                markCompleted,
                                additionalTimeSpentSeconds,
                                studentUserId));
    }

    @Transactional
    public LessonProgress doRecordLessonProgress(
            UUID lessonId,
            boolean markCompleted,
            long additionalTimeSpentSeconds,
            UUID studentUserId) {
        Lesson lesson = lessons.require(lessonId);
        CourseModule module = lesson.getModule();
        UUID courseId = lessons.courseIdOf(lessonId);
        Enrollment enrollment = enrollments.requireActiveEnrollment(courseId, studentUserId);
        Instant now = clock.instant();

        LessonProgress lp =
                lessonProgress
                        .findByEnrollment_IdAndLesson_Id(enrollment.getId(), lessonId)
                        .orElseGet(() -> new LessonProgress(enrollment, lesson));
        lp.recordAccess(now, markCompleted, additionalTimeSpentSeconds);
        LessonProgress saved = lessonProgress.saveAndFlush(lp);

        recomputeModuleProgress(enrollment, module, now);
        recomputeCourseProgress(enrollment, courseId, now);
        return saved;
    }

    private void recomputeModuleProgress(Enrollment enrollment, CourseModule module, Instant now) {
        ModuleProgress mp =
                moduleProgress
                        .findByEnrollment_IdAndModule_Id(enrollment.getId(), module.getId())
                        .orElseGet(() -> new ModuleProgress(enrollment, module));
        long total = lessons.countRequiredPublished(module.getId());
        long completed =
                lessonProgress.countCompletedRequiredInModule(
                        enrollment.getId(),
                        module.getId(),
                        PublicationStatus.PUBLISHED,
                        ProgressStatus.COMPLETED);
        mp.recordAccess(now);
        mp.applyRollup((int) total, (int) completed, now);
        moduleProgress.saveAndFlush(mp);
    }

    private void recomputeCourseProgress(Enrollment enrollment, UUID courseId, Instant now) {
        CourseProgress cp =
                courseProgress
                        .findById(enrollment.getId())
                        .orElseGet(() -> new CourseProgress(enrollment));
        long lessonsTotal = lessons.countRequiredPublishedInCourse(courseId);
        long lessonsCompleted =
                lessonProgress.countCompletedRequiredInCourse(
                        enrollment.getId(),
                        courseId,
                        PublicationStatus.PUBLISHED,
                        ProgressStatus.COMPLETED);
        long activitiesTotal = activities.countPublished(courseId);
        long activitiesCompleted =
                activityProgress.countCompletedInCourse(
                        enrollment.getId(), courseId, ActivityProgressStatus.COMPLETED);
        cp.recordAccess(now);
        cp.applyRollup(
                (int) lessonsTotal,
                (int) lessonsCompleted,
                (int) activitiesTotal,
                (int) activitiesCompleted,
                now);
        courseProgress.saveAndFlush(cp);
    }

    // ---------------------------------------------------------------- assessment integration

    /**
     * {@code assessment.app.AttemptService} cannot call {@code progress} directly (module map:
     * {@code progress -> assessment}, not the reverse — ADR-014), so it publishes {@link
     * QuizAttemptGradedEvent} instead and this listens. {@code AFTER_COMMIT} so a grade this module
     * reacts to is always one {@code assessment} has actually committed, never one that attempt's
     * own transaction might still roll back.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuizAttemptGraded(QuizAttemptGradedEvent event) {
        if (event.activityId() == null) {
            return;
        }
        retryingVoid(() -> self.doRecordActivityAttempt(event));
    }

    /**
     * {@code REQUIRES_NEW}, not plain {@code REQUIRED}: this runs inside {@code
     * AttemptService.doSubmit}'s own {@code afterCommit} callback, where that transaction's
     * resources are already unbound from the thread even though its {@code TransactionInterceptor}
     * call stack has not technically returned yet. A plain {@code @Transactional} here tries to
     * join that defunct context and fails with {@code TransactionRequiredException: no transaction
     * is in progress} the moment a repository call flushes — {@code REQUIRES_NEW} forces a
     * genuinely fresh transaction/`EntityManager` instead of reusing stale synchronization state.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doRecordActivityAttempt(QuizAttemptGradedEvent event) {
        Enrollment enrollment = enrollments.require(event.enrollmentId());
        Activity activity = activities.require(event.activityId());
        ActivityProgress ap =
                activityProgress
                        .findByEnrollment_IdAndActivity_Id(enrollment.getId(), activity.getId())
                        .orElseGet(() -> new ActivityProgress(enrollment, activity));
        ap.recordAttempt(event.percent(), event.passed(), event.gradedAt());
        activityProgress.saveAndFlush(ap);

        UUID courseId = enrollments.courseIdOf(enrollment.getId());
        recomputeCourseProgress(enrollment, courseId, event.gradedAt());
    }

    // ---------------------------------------------------------------- coding integration

    /**
     * {@code execution-worker} is a <b>separate process</b> with no shared Spring event bus
     * (ADR-006): it writes {@code submissions}/{@code execution_results}/{@code
     * execution_test_results} via plain JDBC through the restricted role (V014), which has no grant
     * on {@code activity_progress} or {@code progress_synced_at} at all. So unlike {@link
     * #onQuizAttemptGraded}, which reacts to an in-process event, this module bridges back with the
     * scheduled poll ADR-007 explicitly sanctions ("progress recompute" is legitimate
     * {@code @Scheduled} work inside the API process) — every {@link
     * uz.aifu.lms.common.config.ExecutionProperties}-adjacent interval, find terminal submissions
     * tied to a gradable activity that {@code progress_synced_at} hasn't marked processed yet, and
     * fold each into {@code activity_progress} the same way {@link #doRecordActivityAttempt} does.
     */
    @Scheduled(fixedDelayString = "PT10S")
    public void syncCodingSubmissions() {
        for (Submission submission : submissions.listUnsyncedForProgress(50)) {
            retryingVoid(() -> self.doSyncCodingSubmission(submission.getId()));
        }
    }

    /**
     * {@code REQUIRES_NEW} for the same reason {@link #doRecordActivityAttempt} is: each submission
     * is processed in its own fresh transaction so one bad row can never poison the whole batch,
     * and a retry after an optimistic-lock loss starts from a clean read.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doSyncCodingSubmission(UUID submissionId) {
        Submission submission = submissions.require(submissionId);
        if (submission.getProgressSyncedAt() != null) {
            return;
        }
        Instant now = clock.instant();
        if (submission.getStatus() == SubmissionStatus.COMPLETED) {
            Enrollment enrollment = submission.getEnrollment();
            Activity activity = submission.getActivity();
            BigDecimal percent = codingSubmissionPercent(submission);
            boolean passed = submission.getVerdict() == Verdict.ACCEPTED;
            ActivityProgress ap =
                    activityProgress
                            .findByEnrollment_IdAndActivity_Id(enrollment.getId(), activity.getId())
                            .orElseGet(() -> new ActivityProgress(enrollment, activity));
            ap.recordAttempt(percent, passed, now);
            activityProgress.saveAndFlush(ap);

            UUID courseId = enrollments.courseIdOf(enrollment.getId());
            recomputeCourseProgress(enrollment, courseId, now);
        }
        // FAILED/CANCELLED submissions (sandbox infra failure, or an abandoned run) carry no
        // meaningful score — marked synced so the sweeper stops reprocessing them, but no attempt
        // is recorded into activity_progress.
        submissions.markProgressSynced(submissionId, now);
    }

    private static BigDecimal codingSubmissionPercent(Submission submission) {
        BigDecimal score = submission.getScore();
        BigDecimal maxScore = submission.getMaxScore();
        if (score == null || maxScore == null || maxScore.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return score.multiply(BigDecimal.valueOf(100))
                .divide(maxScore, 2, java.math.RoundingMode.HALF_UP);
    }

    // ---------------------------------------------------------------- read path (student)

    @Transactional(readOnly = true)
    public CourseProgressSnapshot courseProgressForStudent(UUID courseId, UUID studentUserId) {
        Enrollment enrollment = enrollments.requireLatestEnrollment(courseId, studentUserId);
        return snapshotOf(enrollment, courseId);
    }

    @Transactional(readOnly = true)
    public List<ModuleProgress> moduleProgressForStudent(UUID courseId, UUID studentUserId) {
        Enrollment enrollment = enrollments.requireLatestEnrollment(courseId, studentUserId);
        return moduleProgress.findByEnrollment_Id(enrollment.getId());
    }

    // ---------------------------------------------------------------- read path (teacher)

    @Transactional(readOnly = true)
    public Page<EnrollmentProgress> courseProgressRoster(UUID courseId, Pageable pageable) {
        Page<Enrollment> page = enrollments.listByCourse(courseId, null, pageable);
        List<UUID> enrollmentIds = page.getContent().stream().map(Enrollment::getId).toList();
        Map<UUID, CourseProgress> byEnrollment =
                courseProgress.findByEnrollmentIdIn(enrollmentIds).stream()
                        .collect(Collectors.toMap(CourseProgress::getEnrollmentId, cp -> cp));
        int lessonsTotal = (int) lessons.countRequiredPublishedInCourse(courseId);
        int activitiesTotal = (int) activities.countPublished(courseId);
        return page.map(
                enrollment ->
                        new EnrollmentProgress(
                                enrollment,
                                snapshotOf(
                                        enrollment,
                                        courseId,
                                        byEnrollment.get(enrollment.getId()),
                                        lessonsTotal,
                                        activitiesTotal)));
    }

    // ---------------------------------------------------------------- helpers

    private CourseProgressSnapshot snapshotOf(Enrollment enrollment, UUID courseId) {
        CourseProgress row = courseProgress.findById(enrollment.getId()).orElse(null);
        int lessonsTotal = (int) lessons.countRequiredPublishedInCourse(courseId);
        int activitiesTotal = (int) activities.countPublished(courseId);
        return snapshotOf(enrollment, courseId, row, lessonsTotal, activitiesTotal);
    }

    private CourseProgressSnapshot snapshotOf(
            Enrollment enrollment,
            UUID courseId,
            CourseProgress row,
            int lessonsTotal,
            int activitiesTotal) {
        if (row != null) {
            return new CourseProgressSnapshot(
                    enrollment.getId(),
                    courseId,
                    row.getStatus(),
                    row.getCompletionPercent(),
                    row.getLessonsTotal(),
                    row.getLessonsCompleted(),
                    row.getActivitiesTotal(),
                    row.getActivitiesCompleted(),
                    row.getTimeSpentSeconds(),
                    row.getStartedAt(),
                    row.getLastAccessedAt(),
                    row.getCompletedAt());
        }
        return new CourseProgressSnapshot(
                enrollment.getId(),
                courseId,
                ProgressStatus.NOT_STARTED,
                BigDecimal.ZERO,
                lessonsTotal,
                0,
                activitiesTotal,
                0,
                0L,
                null,
                null,
                null);
    }

    private void retryingVoid(Runnable action) {
        retrying(
                () -> {
                    action.run();
                    return null;
                });
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
                            "Too many concurrent progress updates for this lesson; please retry");
                }
            }
        }
    }
}
