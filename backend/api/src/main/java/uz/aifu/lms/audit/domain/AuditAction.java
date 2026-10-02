package uz.aifu.lms.audit.domain;

/**
 * Enumeration of auditable actions.
 *
 * <p>Values are persisted as strings in {@code audit_log.action}. NEVER rename an existing value:
 * historical rows would become unreadable. Add new values at the end of the relevant group.
 */
public enum AuditAction {

    // --- Authentication (iam) ---
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    ACCOUNT_LOCKED,
    TOKEN_REFRESH,
    TOKEN_REUSE_DETECTED,
    LOGOUT,
    LOGOUT_ALL,
    PASSWORD_CHANGED,

    // --- Account administration ---
    USER_CREATED,
    USER_UPDATED,
    USER_STATUS_CHANGED,
    USER_PASSWORD_RESET,
    ROLE_GRANTED,
    ROLE_REVOKED,

    // --- Academics / curriculum ---
    SUBJECT_CREATED,
    SUBJECT_UPDATED,
    SUBJECT_PUBLISHED,
    SUBJECT_ARCHIVED,
    COURSE_CREATED,
    COURSE_UPDATED,
    COURSE_PUBLISHED,
    COURSE_ARCHIVED,
    COURSE_TEACHER_ASSIGNED,
    COURSE_TEACHER_REMOVED,
    MODULE_CREATED,
    MODULE_UPDATED,
    MODULE_DELETED,
    LESSON_CREATED,
    LESSON_UPDATED,
    LESSON_DELETED,
    CONTENT_BLOCK_CHANGED,
    ACTIVITY_CREATED,
    ACTIVITY_UPDATED,
    ACTIVITY_DELETED,
    CONTENT_PUBLISHED,
    TRANSLATION_CHANGED,

    // --- Enrollment ---
    ENROLLMENT_CREATED,
    ENROLLMENT_STATUS_CHANGED,
    ENROLLMENT_WITHDRAWN,

    // --- Assessment / coding ---
    QUIZ_PUBLISHED,
    QUIZ_ATTEMPT_SUBMITTED,
    CODING_TASK_PUBLISHED,
    CODING_SUBMISSION_CREATED,
    EXECUTION_JOB_DEAD,
    QUIZ_CREATED,
    QUIZ_UPDATED,
    QUESTION_CREATED,
    QUESTION_UPDATED,
    QUESTION_DELETED,
    CODING_TASK_CREATED,
    CODING_TASK_UPDATED,
    TEST_CASE_CREATED,
    TEST_CASE_UPDATED,
    TEST_CASE_DELETED,

    // --- Grading ---
    GRADE_RECORDED,
    GRADE_REVISED,
    GRADE_PUBLISHED,
    GRADE_VOIDED,
    FEEDBACK_CREATED,
    FEEDBACK_UPDATED,

    // --- Files ---
    FILE_UPLOADED,
    FILE_DELETED,

    // --- Admin / system ---
    ADMIN_ACTION,
    CONFIGURATION_CHANGED,
    PERMISSION_DENIED;

    /** True for actions that represent a security-relevant failure worth alerting on. */
    public boolean isSecuritySignal() {
        return this == LOGIN_FAILURE
                || this == ACCOUNT_LOCKED
                || this == TOKEN_REUSE_DETECTED
                || this == PERMISSION_DENIED;
    }
}
