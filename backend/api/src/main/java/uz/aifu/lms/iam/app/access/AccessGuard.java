package uz.aifu.lms.iam.app.access;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.security.AuthenticatedUser;
import uz.aifu.lms.common.security.CurrentUserProvider;

/**
 * The single place ownership authorization is decided (brief §9, ADR-004).
 *
 * <p>Two rules make this layer trustworthy and both are deliberate:
 *
 * <ol>
 *   <li><b>Resolve, never trust.</b> Callers pass a course/lesson/enrollment id. The guard derives
 *       the caller's relationship to it from the authenticated principal. A student id or
 *       enrollment id from the request body is never used for an access decision.
 *   <li><b>Absent looks identical to forbidden.</b> When a caller has no relationship to an object,
 *       the guard raises {@link ErrorCode#RESOURCE_NOT_FOUND}, not a 403. A 403 would confirm the
 *       object exists, which is an enumeration oracle across a 200-student cohort.
 * </ol>
 *
 * <p>The ADMIN bypass lives here and only here. No service may write {@code if (user.isAdmin())}.
 */
@Component
public class AccessGuard {

    private final CurrentUserProvider currentUser;
    private final Optional<CourseTeacherLookup> courseTeacherLookup;
    private final Optional<EnrollmentLookup> enrollmentLookup;
    private final Optional<CurriculumLocator> curriculumLocator;
    private final Optional<AssessmentLocator> assessmentLocator;
    private final Optional<CodingLocator> codingLocator;
    private final Optional<LessonGate> lessonGate;

    public AccessGuard(
            CurrentUserProvider currentUser,
            Optional<CourseTeacherLookup> courseTeacherLookup,
            Optional<EnrollmentLookup> enrollmentLookup,
            Optional<CurriculumLocator> curriculumLocator,
            Optional<AssessmentLocator> assessmentLocator,
            Optional<CodingLocator> codingLocator,
            Optional<LessonGate> lessonGate) {
        this.currentUser = currentUser;
        this.courseTeacherLookup = courseTeacherLookup;
        this.enrollmentLookup = enrollmentLookup;
        this.curriculumLocator = curriculumLocator;
        this.assessmentLocator = assessmentLocator;
        this.codingLocator = codingLocator;
        this.lessonGate = lessonGate;
    }

    private CourseTeacherLookup courseTeachers() {
        return require(courseTeacherLookup, "academics");
    }

    private EnrollmentLookup enrollments() {
        return require(enrollmentLookup, "enrollment");
    }

    private CurriculumLocator curriculum() {
        return require(curriculumLocator, "curriculum");
    }

    private AssessmentLocator assessment() {
        return require(assessmentLocator, "assessment");
    }

    private CodingLocator coding() {
        return require(codingLocator, "coding");
    }

    private static <T> T require(Optional<T> port, String owningModule) {
        return port.orElseThrow(
                () ->
                        new IllegalStateException(
                                "No "
                                        + owningModule
                                        + " adapter is registered yet for this AccessGuard port; the "
                                        + owningModule
                                        + " module has not been implemented in this phase"));
    }

    // ------------------------------------------------------------------ courses

    /**
     * Resolves read access to a course for the current caller.
     *
     * @throws uz.aifu.lms.common.error.DomainException with {@link ErrorCode#RESOURCE_NOT_FOUND}
     *     when the course does not exist <em>or</em> the caller has no relationship to it
     */
    public CourseAccess requireCourseViewer(UUID courseId) {
        AuthenticatedUser user = currentUser.require();
        if (!courseTeachers().courseExists(courseId)) {
            throw notFound("Course", courseId);
        }
        boolean published = courseTeachers().courseIsPublished(courseId);

        if (user.isAdmin()) {
            return CourseAccess.admin(courseId, user, published);
        }
        if (courseTeachers().isTeacherOfCourse(courseId, user.id())) {
            return CourseAccess.teacher(courseId, user, published);
        }
        Optional<UUID> enrollment = enrollments().activeEnrollmentId(courseId, user.id());
        if (enrollment.isPresent()) {
            if (!published) {
                // An enrolled student may not read a course that has been pulled back to DRAFT.
                throw notFound("Course", courseId);
            }
            return CourseAccess.student(courseId, user, enrollment.get(), true);
        }
        // Completed or withdrawn students keep read access to their own history.
        Optional<UUID> past = enrollments().latestEnrollmentId(courseId, user.id());
        if (past.isPresent() && published) {
            return CourseAccess.student(courseId, user, past.get(), true);
        }
        throw notFound("Course", courseId);
    }

    /** Resolves write access. Only an assigned teacher or an admin passes. */
    public CourseAccess requireCourseEditor(UUID courseId) {
        AuthenticatedUser user = currentUser.require();
        if (!courseTeachers().courseExists(courseId)) {
            throw notFound("Course", courseId);
        }
        boolean published = courseTeachers().courseIsPublished(courseId);
        if (user.isAdmin()) {
            return CourseAccess.admin(courseId, user, published);
        }
        if (courseTeachers().isTeacherOfCourse(courseId, user.id())) {
            return CourseAccess.teacher(courseId, user, published);
        }
        // Distinguishable here on purpose: an authenticated teacher who can see the course in the
        // catalogue but is not assigned to it gets a clear, actionable error rather than a fake
        // 404.
        if (user.isTeacher()) {
            throw Exceptions.rule(
                    ErrorCode.NOT_COURSE_TEACHER,
                    "You are not assigned as a teacher of this course");
        }
        throw notFound("Course", courseId);
    }

    /** Resolves the caller's ACTIVE enrollment in a course. Students only. */
    public UUID requireActiveEnrollment(UUID courseId) {
        AuthenticatedUser user = currentUser.require();
        return enrollments()
                .activeEnrollmentId(courseId, user.id())
                .orElseThrow(
                        () ->
                                Exceptions.rule(
                                        ErrorCode.NOT_ENROLLED,
                                        "You have no active enrollment in this course"));
    }

    // -------------------------------------------------------------- enrollments

    /**
     * Confirms the caller may read records belonging to an enrollment.
     *
     * <p>Students pass only for their own enrollment; teachers pass for enrollments in courses they
     * teach; admins always pass.
     */
    public CourseAccess requireEnrollmentViewer(UUID enrollmentId) {
        AuthenticatedUser user = currentUser.require();
        UUID courseId =
                enrollments()
                        .courseIdOf(enrollmentId)
                        .orElseThrow(() -> notFound("Enrollment", enrollmentId));

        if (user.isAdmin()) {
            return CourseAccess.admin(courseId, user, courseTeachers().courseIsPublished(courseId));
        }
        if (enrollments().isOwner(enrollmentId, user.id())) {
            return CourseAccess.student(
                    courseId, user, enrollmentId, courseTeachers().courseIsPublished(courseId));
        }
        if (courseTeachers().isTeacherOfCourse(courseId, user.id())) {
            return CourseAccess.teacher(
                    courseId, user, courseTeachers().courseIsPublished(courseId));
        }
        throw notFound("Enrollment", enrollmentId);
    }

    /** Strictly the owning student. Used where a teacher must not act on the student's behalf. */
    public UUID requireEnrollmentOwner(UUID enrollmentId) {
        AuthenticatedUser user = currentUser.require();
        if (!enrollments().isOwner(enrollmentId, user.id())) {
            throw notFound("Enrollment", enrollmentId);
        }
        return enrollmentId;
    }

    // ------------------------------------------------------------ nested content

    public CourseAccess requireModuleViewer(UUID moduleId) {
        return requireCourseViewer(
                resolve(curriculum().courseIdOfModule(moduleId), "Module", moduleId));
    }

    public CourseAccess requireModuleEditor(UUID moduleId) {
        return requireCourseEditor(
                resolve(curriculum().courseIdOfModule(moduleId), "Module", moduleId));
    }

    public CourseAccess requireLessonViewer(UUID lessonId) {
        return requireCourseViewer(
                resolve(curriculum().courseIdOfLesson(lessonId), "Lesson", lessonId));
    }

    public CourseAccess requireOpenLesson(UUID lessonId) {
        CourseAccess access = requireLessonViewer(lessonId);
        if (access.enrollment().isPresent() && lessonGate.isPresent()) {
            lessonGate.get().requireLessonOpen(lessonId, access.enrollment().get());
        }
        return access;
    }

    public CourseAccess requireLessonEditor(UUID lessonId) {
        return requireCourseEditor(
                resolve(curriculum().courseIdOfLesson(lessonId), "Lesson", lessonId));
    }

    public CourseAccess requireActivityViewer(UUID activityId) {
        return requireCourseViewer(
                resolve(curriculum().courseIdOfActivity(activityId), "Activity", activityId));
    }

    public CourseAccess requireActivityEditor(UUID activityId) {
        return requireCourseEditor(
                resolve(curriculum().courseIdOfActivity(activityId), "Activity", activityId));
    }

    public CourseAccess requireQuizViewer(UUID quizId) {
        return requireCourseViewer(resolve(assessment().courseIdOfQuiz(quizId), "Quiz", quizId));
    }

    public CourseAccess requireQuizEditor(UUID quizId) {
        return requireCourseEditor(resolve(assessment().courseIdOfQuiz(quizId), "Quiz", quizId));
    }

    public CourseAccess requireCodingTaskViewer(UUID taskId) {
        return requireCourseViewer(
                resolve(coding().courseIdOfCodingTask(taskId), "CodingTask", taskId));
    }

    public CourseAccess requireCodingTaskEditor(UUID taskId) {
        return requireCourseEditor(
                resolve(coding().courseIdOfCodingTask(taskId), "CodingTask", taskId));
    }

    // ---------------------------------------------------------------- self data

    /**
     * Confirms the caller may read another user's personal record. Admins may; everyone else may
     * read only their own. Returns nothing because there is no context to carry.
     */
    public void requireSelfOrAdmin(UUID targetUserId) {
        AuthenticatedUser user = currentUser.require();
        if (user.isAdmin() || user.id().equals(targetUserId)) {
            return;
        }
        throw notFound("User", targetUserId);
    }

    public AuthenticatedUser caller() {
        return currentUser.require();
    }

    // ----------------------------------------------------------------- helpers

    private UUID resolve(Optional<UUID> courseId, String type, UUID id) {
        return courseId.orElseThrow(() -> notFound(type, id));
    }

    private static uz.aifu.lms.common.error.DomainException notFound(String type, UUID id) {
        return Exceptions.notFound(type, id);
    }
}
