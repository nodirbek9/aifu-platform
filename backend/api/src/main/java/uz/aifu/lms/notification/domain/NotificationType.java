package uz.aifu.lms.notification.domain;

/** Mirrors the {@code notifications.type} CHECK constraint (V013, extended in V019) exactly. */
public enum NotificationType {
    ENROLLMENT_CREATED,
    LESSON_COMPLETED,
    QUIZ_GRADED,
    SUBMISSION_GRADED,
    TEACHER_FEEDBACK,
    ANNOUNCEMENT,
    GRADE_PUBLISHED,
    DEADLINE_REMINDER,
    ACCOUNT_STATUS_CHANGED,
    TIMETABLE_CHANGED
}
