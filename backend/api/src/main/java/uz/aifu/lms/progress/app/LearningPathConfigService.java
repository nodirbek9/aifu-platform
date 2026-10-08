package uz.aifu.lms.progress.app;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.curriculum.app.CourseOutlineService.OutlineLesson;
import uz.aifu.lms.curriculum.app.LessonService;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.curriculum.domain.ActivityType;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.curriculum.domain.PathStep;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.iam.app.user.UserService;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.progress.api.dto.LearningPathDtos.LearningPathConfigRequest;
import uz.aifu.lms.progress.api.dto.LearningPathDtos.LearningPathConfigView;
import uz.aifu.lms.progress.domain.ActivityProgress;
import uz.aifu.lms.progress.domain.CourseLearningPath;
import uz.aifu.lms.progress.domain.LessonProgress;
import uz.aifu.lms.progress.domain.ProgressStatus;
import uz.aifu.lms.progress.persistence.ActivityProgressRepository;
import uz.aifu.lms.progress.persistence.CourseLearningPathRepository;
import uz.aifu.lms.progress.persistence.LessonProgressRepository;

/**
 * Service managing course learning path configuration and rollout waivers (Phase 5 B10, ADR-017,
 * ADR-018).
 */
@Service
public class LearningPathConfigService {

    private final CourseLearningPathRepository pathRepository;
    private final CourseService courses;
    private final LessonService lessons;
    private final ActivityService activities;
    private final LearningPathService learningPathService;
    private final EnrollmentService enrollmentService;
    private final LessonProgressRepository lessonProgressRepository;
    private final ActivityProgressRepository activityProgressRepository;
    private final UserService users;
    private final AuditService auditService;
    private final Clock clock;

    public LearningPathConfigService(
            CourseLearningPathRepository pathRepository,
            CourseService courses,
            LessonService lessons,
            ActivityService activities,
            LearningPathService learningPathService,
            EnrollmentService enrollmentService,
            LessonProgressRepository lessonProgressRepository,
            ActivityProgressRepository activityProgressRepository,
            UserService users,
            AuditService auditService,
            Clock clock) {
        this.pathRepository = pathRepository;
        this.courses = courses;
        this.lessons = lessons;
        this.activities = activities;
        this.learningPathService = learningPathService;
        this.enrollmentService = enrollmentService;
        this.lessonProgressRepository = lessonProgressRepository;
        this.activityProgressRepository = activityProgressRepository;
        this.users = users;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public LearningPathConfigView get(UUID courseId) {
        courses.require(courseId);
        Optional<CourseLearningPath> pathOpt = pathRepository.findByCourseId(courseId);
        if (pathOpt.isEmpty()) {
            return new LearningPathConfigView(
                    false, null, null, null, null, null, new BigDecimal("0.50"));
        }
        return toView(pathOpt.get());
    }

    @Transactional
    public LearningPathConfigView put(
            UUID courseId, LearningPathConfigRequest request, UUID adminUserId) {
        Course course = courses.require(courseId);
        User admin = users.require(adminUserId);
        Instant now = clock.instant();

        CourseLearningPath path =
                pathRepository
                        .findByCourseId(courseId)
                        .orElseGet(() -> new CourseLearningPath(course));

        UUID midtermActId = request.midtermActivityId();
        UUID midtermAnchorId = request.midtermAfterLessonId();

        if ((midtermActId == null && midtermAnchorId != null)
                || (midtermActId != null && midtermAnchorId == null)) {
            throw Exceptions.validation(
                    "midtermActivityId and midtermAfterLessonId must both be set or both null");
        }

        Activity midtermActivity = null;
        Lesson anchorLesson = null;
        Instant midtermOpensAt = null;

        if (midtermActId != null) {
            midtermActivity = activities.require(midtermActId);
            if (midtermActivity.getActivityType() != ActivityType.QUIZ
                    || midtermActivity.getPathStep() != PathStep.MIDTERM
                    || !midtermActivity.getCourse().getId().equals(courseId)) {
                throw Exceptions.validation(
                        "midtermActivity must be a QUIZ with path_step MIDTERM belonging to this course");
            }

            anchorLesson = lessons.require(midtermAnchorId);
            List<OutlineLesson> chain = learningPathService.chainOf(courseId);
            boolean inChain = chain.stream().anyMatch(l -> l.id().equals(midtermAnchorId));
            if (!inChain
                    || anchorLesson.getStatus() != PublicationStatus.PUBLISHED
                    || !anchorLesson.isRequired()
                    || anchorLesson.getSessionType() == SessionType.INDEPENDENT_STUDY
                    || anchorLesson.getDeletedAt() != null) {
                throw Exceptions.validation(
                        "midtermAfterLesson must be a published, required, non-independent-study lesson in the course chain");
            }

            midtermOpensAt = request.midtermOpensAt();
            if (midtermOpensAt == null) {
                midtermOpensAt = computeDefaultMidtermOpensAt(course.getTerm());
            }

            activities.setAvailableFrom(midtermActivity.getId(), midtermOpensAt);
        }

        path.configureMidterm(midtermActivity, anchorLesson, midtermOpensAt);

        boolean previouslyEnabled = path.isEnabled();
        boolean targetEnabled = request.enabled();
        int waiversCount = 0;

        if (targetEnabled) {
            if (!previouslyEnabled) {
                if (path.getEnabledAt() == null) {
                    path.enable(admin, now);
                    waiversCount = writeWaivers(courseId, now);
                } else {
                    path.enable(admin, path.getEnabledAt());
                }
            }
        } else {
            path.disable();
        }

        pathRepository.save(path);

        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("enabled", targetEnabled);
        auditDetails.put("waiversCount", waiversCount);
        auditDetails.put("midtermConfigured", (midtermActivity != null));
        if (midtermOpensAt != null) {
            auditDetails.put("midtermOpensAt", midtermOpensAt.toString());
        }

        auditService.record(
                AuditAction.LEARNING_PATH_CONFIGURED,
                adminUserId,
                "Course",
                courseId,
                null,
                null,
                auditDetails);

        return toView(path);
    }

    private static LearningPathConfigView toView(CourseLearningPath path) {
        return new LearningPathConfigView(
                path.isEnabled(),
                path.getMidtermActivity() != null ? path.getMidtermActivity().getId() : null,
                path.getMidtermAfterLesson() != null ? path.getMidtermAfterLesson().getId() : null,
                path.getMidtermOpensAt(),
                path.getEnabledAt(),
                path.getEnabledBy() != null ? path.getEnabledBy().getId() : null,
                path.getMinReadingFraction());
    }

    private static Instant computeDefaultMidtermOpensAt(Term term) {
        LocalDate startsOn = term.getStartsOn();
        LocalDate mondayWeek1 = startsOn.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate mondayWeek8 = mondayWeek1.plusWeeks(7);
        return mondayWeek8.atStartOfDay(ZoneId.of("Asia/Tashkent")).toInstant();
    }

    private int writeWaivers(UUID courseId, Instant now) {
        List<OutlineLesson> chain = learningPathService.chainOf(courseId);
        if (chain.isEmpty()) {
            return 0;
        }

        Map<UUID, Integer> chainPosition = new HashMap<>();
        Map<UUID, Lesson> lessonEntityMap = new HashMap<>();
        for (int i = 0; i < chain.size(); i++) {
            UUID lessonId = chain.get(i).id();
            chainPosition.put(lessonId, i);
            lessonEntityMap.put(lessonId, lessons.require(lessonId));
        }

        List<Activity> courseActivities = activities.listByCourse(courseId);
        Map<UUID, UUID> activityToLesson = new HashMap<>();
        for (Activity act : courseActivities) {
            if (act.getLesson() != null) {
                activityToLesson.put(act.getId(), act.getLesson().getId());
            }
        }

        List<Enrollment> activeEnrollments = enrollmentService.listActiveByCourse(courseId);
        int totalWaivers = 0;

        for (Enrollment enrollment : activeEnrollments) {
            List<LessonProgress> lpList =
                    lessonProgressRepository.findByEnrollment_Id(enrollment.getId());
            Map<UUID, LessonProgress> lpByLesson = new HashMap<>();
            int maxReachedPos = -1;
            LessonProgress furthestLp = null;

            for (LessonProgress lp : lpList) {
                UUID lessonId = lp.getLesson().getId();
                lpByLesson.put(lessonId, lp);
                if (lp.getStatus() != ProgressStatus.NOT_STARTED) {
                    Integer pos = chainPosition.get(lessonId);
                    if (pos != null && pos > maxReachedPos) {
                        maxReachedPos = pos;
                        furthestLp = lp;
                    }
                }
            }

            List<ActivityProgress> apList =
                    activityProgressRepository.findByEnrollment_Id(enrollment.getId());
            for (ActivityProgress ap : apList) {
                UUID lessonId = activityToLesson.get(ap.getActivity().getId());
                if (lessonId != null) {
                    Integer pos = chainPosition.get(lessonId);
                    if (pos != null && pos > maxReachedPos) {
                        maxReachedPos = pos;
                        furthestLp = lpByLesson.get(lessonId);
                    }
                }
            }

            if (maxReachedPos < 0) {
                continue;
            }

            int waiveUpTo =
                    (furthestLp != null && furthestLp.getStatus() == ProgressStatus.COMPLETED)
                            ? maxReachedPos
                            : maxReachedPos - 1;

            for (int idx = 0; idx <= waiveUpTo; idx++) {
                UUID lessonId = chain.get(idx).id();
                LessonProgress lp = lpByLesson.get(lessonId);
                if (lp == null) {
                    Lesson lesson = lessonEntityMap.get(lessonId);
                    LessonProgress newLp = new LessonProgress(enrollment, lesson);
                    newLp.waive(now);
                    lessonProgressRepository.save(newLp);
                    lpByLesson.put(lessonId, newLp);
                    totalWaivers++;
                } else if (!lp.isWaived()) {
                    lp.waive(now);
                    lessonProgressRepository.save(lp);
                    totalWaivers++;
                }
            }
        }
        return totalWaivers;
    }
}
