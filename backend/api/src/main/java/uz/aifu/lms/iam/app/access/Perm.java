package uz.aifu.lms.iam.app.access;

/**
 * Permission code constants, mirroring the {@code permissions} rows seeded by V003.
 *
 * <p>Use these in {@code @PreAuthorize("hasAuthority(T(uz.aifu.lms.iam.app.access.Perm).X)")}
 * rather than bare strings, so a typo is a compile error instead of a silently open endpoint.
 *
 * <p>Adding a permission means: add the row in a new migration, add the constant here, and grant it
 * to the roles that need it. Never grant by role name inside a service (ADR-004).
 */
public final class Perm {

    private Perm() {}

    // iam
    public static final String USER_READ = "USER_READ";
    public static final String USER_MANAGE = "USER_MANAGE";
    public static final String ROLE_MANAGE = "ROLE_MANAGE";

    // academics
    public static final String SUBJECT_READ = "SUBJECT_READ";
    public static final String SUBJECT_MANAGE = "SUBJECT_MANAGE";
    public static final String COURSE_READ = "COURSE_READ";
    public static final String COURSE_MANAGE = "COURSE_MANAGE";
    public static final String COURSE_TEACHER_MANAGE = "COURSE_TEACHER_MANAGE";

    // curriculum
    public static final String COURSE_CONTENT_READ = "COURSE_CONTENT_READ";
    public static final String COURSE_CONTENT_WRITE = "COURSE_CONTENT_WRITE";
    public static final String LESSON_READ = "LESSON_READ";
    public static final String ACTIVITY_MANAGE = "ACTIVITY_MANAGE";

    // enrollment
    public static final String ENROLLMENT_READ = "ENROLLMENT_READ";
    public static final String ENROLLMENT_MANAGE = "ENROLLMENT_MANAGE";
    public static final String ENROLLMENT_SELF_READ = "ENROLLMENT_SELF_READ";

    // progress
    public static final String PROGRESS_SELF_READ = "PROGRESS_SELF_READ";
    public static final String PROGRESS_SELF_WRITE = "PROGRESS_SELF_WRITE";
    public static final String PROGRESS_COURSE_READ = "PROGRESS_COURSE_READ";

    // assessment
    public static final String QUIZ_READ = "QUIZ_READ";
    public static final String QUIZ_MANAGE = "QUIZ_MANAGE";
    public static final String QUIZ_ATTEMPT_CREATE = "QUIZ_ATTEMPT_CREATE";
    public static final String QUIZ_ATTEMPT_SELF_READ = "QUIZ_ATTEMPT_SELF_READ";
    public static final String QUIZ_ATTEMPT_REVIEW = "QUIZ_ATTEMPT_REVIEW";

    // coding
    public static final String CODING_TASK_READ = "CODING_TASK_READ";
    public static final String CODING_TASK_MANAGE = "CODING_TASK_MANAGE";
    public static final String SUBMISSION_CREATE = "SUBMISSION_CREATE";
    public static final String SUBMISSION_SELF_READ = "SUBMISSION_SELF_READ";
    public static final String SUBMISSION_REVIEW = "SUBMISSION_REVIEW";

    // grading
    public static final String GRADE_SELF_READ = "GRADE_SELF_READ";
    public static final String GRADE_READ = "GRADE_READ";
    public static final String GRADE_WRITE = "GRADE_WRITE";
    public static final String FEEDBACK_WRITE = "FEEDBACK_WRITE";

    // files
    public static final String FILE_UPLOAD = "FILE_UPLOAD";
    public static final String FILE_MANAGE = "FILE_MANAGE";

    // analytics / audit / notification / admin
    public static final String ANALYTICS_COURSE_READ = "ANALYTICS_COURSE_READ";
    public static final String ANALYTICS_PLATFORM_READ = "ANALYTICS_PLATFORM_READ";
    public static final String AUDIT_READ = "AUDIT_READ";
    public static final String NOTIFICATION_SELF_READ = "NOTIFICATION_SELF_READ";
    public static final String ANNOUNCEMENT_WRITE = "ANNOUNCEMENT_WRITE";
    public static final String SYSTEM_CONFIG_MANAGE = "SYSTEM_CONFIG_MANAGE";

    // scheduling
    public static final String GROUP_READ = "GROUP_READ";
    public static final String GROUP_MANAGE = "GROUP_MANAGE";
    public static final String TIMETABLE_READ = "TIMETABLE_READ";
    public static final String TIMETABLE_MANAGE = "TIMETABLE_MANAGE";

    /** Convenience for {@code @PreAuthorize}. */
    public static final String HAS_AUDIT_READ = "hasAuthority('AUDIT_READ')";
}
