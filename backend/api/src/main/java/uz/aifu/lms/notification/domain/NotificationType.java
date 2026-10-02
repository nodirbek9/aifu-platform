package uz.aifu.lms.notification.domain;

/** Mirrors the {@code notifications.type} CHECK constraint (V013) exactly. */
public enum NotificationType {
    ENROLLMENT_CREATED,
    LESSON_COMPLETED,
    QUIZ_GRADED,
    SUBMISSION_GRADED,
    TEACHER_FEEDBACK,
    ANNOUNCEMENT,
    GRADE_PUBLISHED,
    DEADLINE_REMINDER,
    ACCOUNT_STATUS_CHANGED
}
