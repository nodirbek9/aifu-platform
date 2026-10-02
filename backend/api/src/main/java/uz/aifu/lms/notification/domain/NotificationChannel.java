package uz.aifu.lms.notification.domain;

/**
 * Mirrors the {@code notifications.channel} CHECK constraint (V013). Only {@link #IN_APP} is
 * dispatched in v1 (brief §61) — {@code EMAIL}/{@code PUSH} exist so the column never needs a
 * migration when those channels are built, but nothing sends through them yet.
 */
public enum NotificationChannel {
    IN_APP,
    EMAIL,
    PUSH
}
