package uz.aifu.lms.notification.domain;

/** Mirrors the {@code notifications.status} CHECK constraint (V013). */
public enum NotificationStatus {
    PENDING,
    SENT,
    READ,
    FAILED,
    SUPPRESSED
}
