package uz.aifu.lms.progress.app;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.CourseDirectory;
import uz.aifu.lms.academics.app.CourseDirectory.CourseLabel;
import uz.aifu.lms.assessment.app.QuizService;
import uz.aifu.lms.assessment.domain.QuizTranslation;
import uz.aifu.lms.coding.app.CodingTaskService;
import uz.aifu.lms.coding.domain.CodingTaskTranslation;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.curriculum.app.CourseOutlineService;
import uz.aifu.lms.curriculum.app.CourseOutlineService.OutlineLesson;
import uz.aifu.lms.curriculum.app.CourseOutlineService.OutlineModule;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.curriculum.domain.ActivityTranslation;
import uz.aifu.lms.curriculum.domain.ActivityType;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.iam.app.access.AccessGuard;
import uz.aifu.lms.iam.app.access.CourseAccess;
import uz.aifu.lms.progress.api.dto.ProgressDtos.CoursePlanView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.MyActivityView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.PlanLesson;
import uz.aifu.lms.progress.api.dto.ProgressDtos.PlanModule;
import uz.aifu.lms.progress.domain.ActivityProgress;
import uz.aifu.lms.progress.domain.ActivityProgressStatus;
import uz.aifu.lms.progress.domain.LessonProgress;
import uz.aifu.lms.progress.domain.ProgressStatus;
import uz.aifu.lms.progress.persistence.ActivityProgressRepository;
import uz.aifu.lms.progress.persistence.LessonProgressRepository;

/**
 * What a student has to do and how far they are, as the course page and the dashboard show it
 * (product spec S-01, S-03.2, S-07): the course plan with a status on every lesson, and one list of
 * all quizzes and tasks across the student's courses.
 */
@Service
public class StudentWorkService {

    public static final String UPCOMING = "UPCOMING";
    public static final String TODO = "TODO";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String OVERDUE = "OVERDUE";

    private final CourseOutlineService outlines;
    private final ActivityService activities;
    private final EnrollmentService enrollments;
    private final CourseDirectory courses;
    private final QuizService quizzes;
    private final CodingTaskService codingTasks;
    private final LessonProgressRepository lessonProgress;
    private final ActivityProgressRepository activityProgress;
    private final AccessGuard accessGuard;
    private final Clock clock;

    public StudentWorkService(
            CourseOutlineService outlines,
            ActivityService activities,
            EnrollmentService enrollments,
            CourseDirectory courses,
            QuizService quizzes,
            CodingTaskService codingTasks,
            LessonProgressRepository lessonProgress,
            ActivityProgressRepository activityProgress,
            AccessGuard accessGuard,
            Clock clock) {
        this.outlines = outlines;
        this.activities = activities;
        this.enrollments = enrollments;
        this.courses = courses;
        this.quizzes = quizzes;
        this.codingTasks = codingTasks;
        this.lessonProgress = lessonProgress;
        this.activityProgress = activityProgress;
        this.accessGuard = accessGuard;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ course plan

    /**
     * The published modules and lessons of a course with the caller's status on each lesson. A
     * teacher or admin gets the same tree with no statuses (a preview of what students see).
     */
    @Transactional(readOnly = true)
    public CoursePlanView plan(UUID courseId, LocaleCode locale) {
        CourseAccess access = accessGuard.requireCourseViewer(courseId);
        UUID enrollmentId = access.enrollment().orElse(null);
        List<OutlineModule> outline = outlines.publishedOutline(courseId, locale);

        Map<UUID, LessonProgress> progressByLesson = new HashMap<>();
        Set<UUID> completedActivities = new HashSet<>();
        if (enrollmentId != null) {
            for (LessonProgress p : lessonProgress.findByEnrollment_Id(enrollmentId)) {
                progressByLesson.put(p.getLesson().getId(), p);
            }
            for (ActivityProgress p :
                    activityProgress.findByEnrollment_IdIn(List.of(enrollmentId))) {
                if (p.getStatus() == ActivityProgressStatus.COMPLETED) {
                    completedActivities.add(p.getActivity().getId());
                }
            }
        }
        Map<UUID, List<UUID>> activitiesByLesson = new HashMap<>();
        for (Activity activity : activities.listPublishedByCourses(List.of(courseId))) {
            if (activity.getLesson() != null) {
                activitiesByLesson
                        .computeIfAbsent(activity.getLesson().getId(), k -> new ArrayList<>())
                        .add(activity.getId());
            }
        }

        Set<UUID> done =
                progressByLesson.entrySet().stream()
                        .filter(e -> e.getValue().getStatus() == ProgressStatus.COMPLETED)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());
        int total = 0;
        UUID continueLessonId = null;
        List<PlanModule> modules = new ArrayList<>();
        for (OutlineModule module : outline) {
            List<PlanLesson> lessons = new ArrayList<>();
            for (OutlineLesson lesson : module.lessons()) {
                total++;
                boolean locked =
                        enrollmentId != null && !done.containsAll(lesson.prerequisiteLessonIds());
                LessonProgress progress = progressByLesson.get(lesson.id());
                ProgressStatus status =
                        enrollmentId == null
                                ? null
                                : progress == null
                                        ? ProgressStatus.NOT_STARTED
                                        : progress.getStatus();
                if (continueLessonId == null && !locked && status != ProgressStatus.COMPLETED) {
                    continueLessonId = lesson.id();
                }
                List<UUID> lessonActivities =
                        activitiesByLesson.getOrDefault(lesson.id(), List.of());
                lessons.add(
                        new PlanLesson(
                                lesson.id(),
                                lesson.code(),
                                lesson.title(),
                                lesson.sessionType(),
                                lesson.allocatedHours(),
                                lesson.estimatedMinutes(),
                                lesson.required(),
                                status,
                                locked,
                                lesson.prerequisiteLessonIds(),
                                lessonActivities.size(),
                                (int)
                                        lessonActivities.stream()
                                                .filter(completedActivities::contains)
                                                .count()));
            }
            modules.add(new PlanModule(module.id(), module.code(), module.title(), lessons));
        }
        UUID lastLessonId =
                progressByLesson.values().stream()
                        .filter(p -> p.getLastAccessedAt() != null)
                        .max(Comparator.comparing(LessonProgress::getLastAccessedAt))
                        .map(p -> p.getLesson().getId())
                        .orElse(null);
        Set<UUID> shown =
                outline.stream()
                        .flatMap(m -> m.lessons().stream())
                        .map(OutlineLesson::id)
                        .collect(Collectors.toSet());
        return new CoursePlanView(
                courseId,
                enrollmentId,
                total,
                (int) done.stream().filter(shown::contains).count(),
                continueLessonId,
                shown.contains(lastLessonId) ? lastLessonId : null,
                modules);
    }

    // ------------------------------------------------------------------ my work

    /**
     * Every published activity of the caller's current courses, soonest deadline first. Optionally
     * only one course or one state.
     */
    @Transactional(readOnly = true)
    public List<MyActivityView> myWork(
            UUID studentUserId, UUID courseId, String state, LocaleCode locale) {
        Map<UUID, Enrollment> enrollmentByCourse = new HashMap<>();
        for (Enrollment e : enrollments.listByStudent(studentUserId, Pageable.unpaged())) {
            if (!e.getStatus().isTerminal()
                    && (courseId == null || e.getCourse().getId().equals(courseId))) {
                enrollmentByCourse.put(e.getCourse().getId(), e);
            }
        }
        if (enrollmentByCourse.isEmpty()) {
            return List.of();
        }
        List<Activity> found = activities.listPublishedByCourses(enrollmentByCourse.keySet());
        Map<UUID, ActivityProgress> progressByActivity = new HashMap<>();
        for (ActivityProgress p :
                activityProgress.findByEnrollment_IdIn(
                        enrollmentByCourse.values().stream().map(Enrollment::getId).toList())) {
            progressByActivity.put(p.getActivity().getId(), p);
        }
        Map<UUID, CourseLabel> courseLabels = courses.labels(enrollmentByCourse.keySet(), locale);
        Map<UUID, String> titles = titles(found, locale);
        Instant now = clock.instant();

        return found.stream()
                .map(
                        a -> {
                            ActivityProgress p = progressByActivity.get(a.getId());
                            CourseLabel course = courseLabels.get(a.getCourse().getId());
                            return new MyActivityView(
                                    a.getId(),
                                    a.getCourse().getId(),
                                    course == null ? null : course.subjectCode(),
                                    course == null ? null : course.title(),
                                    a.getLesson() == null ? null : a.getLesson().getId(),
                                    a.getActivityType(),
                                    a.getReferenceId(),
                                    titles.get(a.getId()),
                                    a.getAvailableFrom(),
                                    a.getDueAt(),
                                    a.isGraded(),
                                    a.getMaxPoints(),
                                    a.getAttemptLimit(),
                                    p == null ? 0 : p.getAttemptsUsed(),
                                    p == null ? null : p.getBestScore(),
                                    stateOf(a, p, now),
                                    p == null ? null : p.getCompletedAt());
                        })
                .filter(v -> state == null || state.equalsIgnoreCase(v.state()))
                .sorted(
                        Comparator.comparing(
                                        MyActivityView::dueAt,
                                        Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(v -> v.courseTitle() == null ? "" : v.courseTitle())
                                .thenComparing(v -> v.title() == null ? "" : v.title()))
                .toList();
    }

    static String stateOf(Activity activity, ActivityProgress progress, Instant now) {
        if (progress != null && progress.getStatus() == ActivityProgressStatus.COMPLETED) {
            return COMPLETED;
        }
        if (activity.getDueAt() != null && activity.getDueAt().isBefore(now)) {
            return OVERDUE;
        }
        if (activity.getAvailableFrom() != null && activity.getAvailableFrom().isAfter(now)) {
            return UPCOMING;
        }
        boolean started =
                progress != null
                        && (progress.getAttemptsUsed() > 0
                                || progress.getStatus() != ActivityProgressStatus.NOT_STARTED);
        return started ? IN_PROGRESS : TODO;
    }

    /** The activity's own title, else the title of the quiz or coding task it points at. */
    private Map<UUID, String> titles(List<Activity> found, LocaleCode locale) {
        Map<UUID, String> titles = new HashMap<>();
        activities
                .listTranslationsForActivities(found.stream().map(Activity::getId).toList())
                .forEach(
                        (id, all) ->
                                titles.put(
                                        id,
                                        pick(
                                                all,
                                                ActivityTranslation::getLocale,
                                                ActivityTranslation::getTitle,
                                                locale)));
        Map<UUID, String> quizTitles = new HashMap<>();
        quizzes.listTranslationsForQuizzes(references(found, ActivityType.QUIZ))
                .forEach(
                        (id, all) ->
                                quizTitles.put(
                                        id,
                                        pick(
                                                all,
                                                QuizTranslation::getLocale,
                                                QuizTranslation::getTitle,
                                                locale)));
        Map<UUID, String> taskTitles = new HashMap<>();
        codingTasks
                .listTranslationsForTasks(references(found, ActivityType.CODING_TASK))
                .forEach(
                        (id, all) ->
                                taskTitles.put(
                                        id,
                                        pick(
                                                all,
                                                CodingTaskTranslation::getLocale,
                                                CodingTaskTranslation::getTitle,
                                                locale)));
        for (Activity a : found) {
            if (titles.get(a.getId()) == null && a.getReferenceId() != null) {
                titles.put(
                        a.getId(),
                        a.getActivityType() == ActivityType.QUIZ
                                ? quizTitles.get(a.getReferenceId())
                                : taskTitles.get(a.getReferenceId()));
            }
        }
        return titles;
    }

    private static List<UUID> references(List<Activity> found, ActivityType type) {
        return found.stream()
                .filter(a -> a.getActivityType() == type && a.getReferenceId() != null)
                .map(Activity::getReferenceId)
                .distinct()
                .toList();
    }

    /** Requested locale, then Uzbek, then anything. */
    private static <T> String pick(
            List<T> all,
            Function<T, String> localeOf,
            Function<T, String> titleOf,
            LocaleCode locale) {
        Map<String, String> byLocale =
                all.stream().collect(Collectors.toMap(localeOf, titleOf, (a, b) -> a));
        String title = byLocale.get(locale.code());
        if (title == null) {
            title = byLocale.get(LocaleCode.DEFAULT.code());
        }
        return title != null ? title : byLocale.values().stream().findFirst().orElse(null);
    }
}
