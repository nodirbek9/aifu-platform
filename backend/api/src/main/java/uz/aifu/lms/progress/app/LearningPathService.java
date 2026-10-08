package uz.aifu.lms.progress.app;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.common.error.DomainException;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.curriculum.app.CourseOutlineService;
import uz.aifu.lms.curriculum.app.CourseOutlineService.OutlineLesson;
import uz.aifu.lms.curriculum.app.CourseOutlineService.OutlineModule;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.curriculum.domain.PathStep;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.progress.app.PathRules.LessonInput;
import uz.aifu.lms.progress.app.PathRules.LessonPathState;
import uz.aifu.lms.progress.app.PathRules.LessonProgressInput;
import uz.aifu.lms.progress.app.PathRules.LessonSteps;
import uz.aifu.lms.progress.app.PathRules.LockReason;
import uz.aifu.lms.progress.app.PathRules.MidtermConfig;
import uz.aifu.lms.progress.app.PathRules.StepState;
import uz.aifu.lms.progress.domain.ActivityProgress;
import uz.aifu.lms.progress.domain.ActivityProgressStatus;
import uz.aifu.lms.progress.domain.CourseLearningPath;
import uz.aifu.lms.progress.domain.LessonProgress;
import uz.aifu.lms.progress.domain.ProgressStatus;
import uz.aifu.lms.progress.persistence.ActivityProgressRepository;
import uz.aifu.lms.progress.persistence.CourseLearningPathRepository;
import uz.aifu.lms.progress.persistence.LessonProgressRepository;

/**
 * Read model and gate enforcement for the server-side learning path (Phase 5, B4, B5, ADR-015,
 * ADR-017, ADR-018).
 *
 * <p>Enforces the lesson progression order and midterm gate. Only callers enrolled in a course
 * where {@link CourseLearningPath#isEnabled()} is true are gated.
 */
@Service
public class LearningPathService {

    private final CourseLearningPathRepository pathRepository;
    private final CourseOutlineService outlines;
    private final ActivityService activities;
    private final LessonProgressRepository lessonProgress;
    private final ActivityProgressRepository activityProgress;
    private final EnrollmentService enrollments;
    private final Clock clock;

    public LearningPathService(
            CourseLearningPathRepository pathRepository,
            CourseOutlineService outlines,
            ActivityService activities,
            LessonProgressRepository lessonProgress,
            ActivityProgressRepository activityProgress,
            EnrollmentService enrollments,
            Clock clock) {
        this.pathRepository = pathRepository;
        this.outlines = outlines;
        this.activities = activities;
        this.lessonProgress = lessonProgress;
        this.activityProgress = activityProgress;
        this.enrollments = enrollments;
        this.clock = clock;
    }

    /**
     * The ordered chain of published, required lessons in the course (excluding INDEPENDENT_STUDY).
     */
    @Transactional(readOnly = true)
    public List<OutlineLesson> chainOf(UUID courseId) {
        List<OutlineModule> outline = outlines.publishedOutline(courseId, LocaleCode.DEFAULT);
        List<OutlineLesson> chain = new ArrayList<>();
        for (OutlineModule module : outline) {
            for (OutlineLesson lesson : module.lessons()) {
                if (lesson.required() && lesson.sessionType() != SessionType.INDEPENDENT_STUDY) {
                    chain.add(lesson);
                }
            }
        }
        return chain;
    }

    /** Evaluates the path state of all published lessons in an enrollment's course. */
    @Transactional(readOnly = true)
    public Map<UUID, LessonPathState> pathState(UUID enrollmentId) {
        Enrollment enrollment = enrollments.require(enrollmentId);
        return pathState(enrollment.getCourse().getId(), enrollmentId);
    }

    /** Evaluates the path state of all published lessons for a specific course and enrollment. */
    @Transactional(readOnly = true)
    public Map<UUID, LessonPathState> pathState(UUID courseId, UUID enrollmentId) {
        List<OutlineModule> outline = outlines.publishedOutline(courseId, LocaleCode.DEFAULT);
        if (outline.isEmpty()) {
            return Collections.emptyMap();
        }

        Optional<CourseLearningPath> pathOpt = pathRepository.findByCourseId(courseId);
        boolean pathEnabled = pathOpt.map(CourseLearningPath::isEnabled).orElse(false);

        List<LessonInput> lessonInputs = new ArrayList<>();
        int moduleIndex = 0;
        for (OutlineModule module : outline) {
            int lessonIndex = 0;
            for (OutlineLesson lesson : module.lessons()) {
                lessonInputs.add(
                        new LessonInput(
                                lesson.id(),
                                module.id(),
                                moduleIndex,
                                lessonIndex,
                                lesson.sessionType(),
                                lesson.required(),
                                true,
                                lesson.prerequisiteLessonIds()));
                lessonIndex++;
            }
            moduleIndex++;
        }

        if (enrollmentId == null) {
            Map<UUID, LessonPathState> openMap = new LinkedHashMap<>();
            for (LessonInput l : lessonInputs) {
                openMap.put(
                        l.id(),
                        new LessonPathState(
                                l.id(),
                                new LessonSteps(StepState.TODO, StepState.NONE, StepState.NONE),
                                false,
                                true,
                                null,
                                null));
            }
            return openMap;
        }

        List<LessonProgress> lpList = lessonProgress.findByEnrollment_Id(enrollmentId);
        Map<UUID, LessonProgress> lpMap =
                lpList.stream()
                        .collect(
                                Collectors.toMap(
                                        lp -> lp.getLesson().getId(),
                                        Function.identity(),
                                        (a, b) -> a));

        List<ActivityProgress> apList = activityProgress.findByEnrollment_Id(enrollmentId);
        Set<UUID> completedActivityIds =
                apList.stream()
                        .filter(ap -> ap.getStatus() == ActivityProgressStatus.COMPLETED)
                        .map(ap -> ap.getActivity().getId())
                        .collect(Collectors.toSet());

        List<Activity> courseActivities = activities.listPublishedByCourses(List.of(courseId));
        Map<UUID, List<Activity>> activitiesByLesson =
                courseActivities.stream()
                        .filter(a -> a.getLesson() != null)
                        .collect(Collectors.groupingBy(a -> a.getLesson().getId()));

        Map<UUID, LessonProgressInput> progressInputs = new HashMap<>();
        for (LessonInput lesson : lessonInputs) {
            LessonProgress lp = lpMap.get(lesson.id());
            boolean completed = lp != null && lp.getStatus() == ProgressStatus.COMPLETED;
            boolean waived = lp != null && lp.isWaived();
            boolean readingGreen = lp != null && lp.isReadCompleted();

            List<Activity> lessonActs = activitiesByLesson.getOrDefault(lesson.id(), List.of());
            Activity testAct =
                    lessonActs.stream()
                            .filter(a -> a.getPathStep() == PathStep.TEST)
                            .findFirst()
                            .orElse(null);
            Activity practiceAct =
                    lessonActs.stream()
                            .filter(a -> a.getPathStep() == PathStep.PRACTICE)
                            .findFirst()
                            .orElse(null);

            boolean testPresent = testAct != null;
            boolean testDone = testPresent && completedActivityIds.contains(testAct.getId());
            boolean practicePresent = practiceAct != null;
            boolean practiceDone =
                    practicePresent && completedActivityIds.contains(practiceAct.getId());

            progressInputs.put(
                    lesson.id(),
                    new LessonProgressInput(
                            completed,
                            waived,
                            readingGreen,
                            testPresent,
                            testDone,
                            practicePresent,
                            practiceDone));
        }

        CourseLearningPath path = pathOpt.orElse(null);
        MidtermConfig midtermConfig;
        if (path != null) {
            UUID midtermActId =
                    path.getMidtermActivity() != null ? path.getMidtermActivity().getId() : null;
            UUID anchorLessonId =
                    path.getMidtermAfterLesson() != null
                            ? path.getMidtermAfterLesson().getId()
                            : null;
            boolean midtermPassed =
                    midtermActId != null && completedActivityIds.contains(midtermActId);
            midtermConfig =
                    new MidtermConfig(
                            midtermActId,
                            anchorLessonId,
                            midtermPassed,
                            path.getMidtermOpensAt(),
                            clock.instant());
        } else {
            midtermConfig = MidtermConfig.none();
        }

        Map<UUID, LessonPathState> evaluated =
                PathRules.evaluate(lessonInputs, progressInputs, midtermConfig);

        if (!pathEnabled) {
            // When path is disabled, lessons are never locked
            Map<UUID, LessonPathState> unlocked = new LinkedHashMap<>();
            for (Map.Entry<UUID, LessonPathState> entry : evaluated.entrySet()) {
                LessonPathState s = entry.getValue();
                unlocked.put(
                        entry.getKey(),
                        new LessonPathState(s.lessonId(), s.steps(), s.done(), true, null, null));
            }
            return unlocked;
        }

        return evaluated;
    }

    /**
     * Enforces that the lesson is open for the given enrollment.
     *
     * @throws DomainException with {@link ErrorCode#LESSON_PREREQUISITE_NOT_MET} if locked
     */
    @Transactional(readOnly = true)
    public void requireLessonOpen(UUID lessonId, UUID enrollmentId) {
        if (enrollmentId == null) {
            return;
        }
        Enrollment enrollment = enrollments.require(enrollmentId);
        UUID courseId = enrollment.getCourse().getId();

        Optional<CourseLearningPath> pathOpt = pathRepository.findByCourseId(courseId);
        if (pathOpt.isEmpty() || !pathOpt.get().isEnabled()) {
            return;
        }

        CourseLearningPath path = pathOpt.get();
        Map<UUID, LessonPathState> states = pathState(courseId, enrollmentId);
        LessonPathState state = states.get(lessonId);

        if (state != null && !state.open()) {
            DomainException ex =
                    Exceptions.rule(
                            ErrorCode.LESSON_PREREQUISITE_NOT_MET,
                            "Lesson prerequisite not met: " + state.lockReason());
            if (state.lockReason() != null) {
                ex.with("reason", state.lockReason().name());
            }
            if (state.blockingLessonId() != null) {
                ex.with("blockingLessonId", state.blockingLessonId().toString());
            }
            if (state.lockReason() == LockReason.MIDTERM_REQUIRED
                    && path.getMidtermActivity() != null) {
                ex.with("midtermActivityId", path.getMidtermActivity().getId().toString());
            }
            throw ex;
        }
    }

    /**
     * Enforces that an activity is open for the given enrollment.
     *
     * @throws DomainException with {@link ErrorCode#ACTIVITY_NOT_AVAILABLE} if midterm before
     *     opensAt, or {@link ErrorCode#LESSON_PREREQUISITE_NOT_MET} if its lesson is locked
     */
    @Transactional(readOnly = true)
    public void requireActivityOpen(UUID activityId, UUID enrollmentId) {
        if (enrollmentId == null) {
            return;
        }
        Enrollment enrollment = enrollments.require(enrollmentId);
        UUID courseId = enrollment.getCourse().getId();

        Optional<CourseLearningPath> pathOpt = pathRepository.findByCourseId(courseId);
        if (pathOpt.isEmpty() || !pathOpt.get().isEnabled()) {
            return;
        }

        CourseLearningPath path = pathOpt.get();
        Activity activity = activities.require(activityId);

        boolean isMidterm =
                (path.getMidtermActivity() != null
                                && path.getMidtermActivity().getId().equals(activityId))
                        || activity.getPathStep() == PathStep.MIDTERM;

        if (isMidterm) {
            Instant opensAt =
                    path.getMidtermOpensAt() != null
                            ? path.getMidtermOpensAt()
                            : activity.getAvailableFrom();
            Instant now = clock.instant();
            if (opensAt != null && now.isBefore(opensAt)) {
                throw Exceptions.rule(
                                ErrorCode.ACTIVITY_NOT_AVAILABLE, "Midterm is not yet available")
                        .with("activityId", activityId.toString())
                        .with("availableFrom", opensAt.toString());
            }
            return;
        }

        if (activity.getLesson() != null) {
            requireLessonOpen(activity.getLesson().getId(), enrollmentId);
        }
    }
}
