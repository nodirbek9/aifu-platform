package uz.aifu.lms.common.events;

/**
 * The thing some notifications were about is gone (an announcement was deleted): every notification
 * written with this {@code dedupeKey}, whoever it was sent to, is withdrawn — a notification must
 * never lead to something that no longer exists. Published like {@link NotificationRequestedEvent},
 * through {@code common}, so the publisher needs no edge to {@code notification}.
 */
public record NotificationsWithdrawnEvent(String dedupeKey) {}
