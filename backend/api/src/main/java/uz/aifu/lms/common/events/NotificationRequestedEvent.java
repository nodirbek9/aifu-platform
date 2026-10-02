package uz.aifu.lms.common.events;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * A plain event record in {@code common}, the same pattern {@link QuizAttemptGradedEvent}
 * established (ADR-014): any module that already knows a real recipient {@code UUID} can trigger a
 * notification without adding a new dependency edge to {@code notification} — the module map never
 * needed to grow, since everyone may already depend on {@code common}. {@code notification} listens
 * with {@code @TransactionalEventListener(phase = AFTER_COMMIT)} so it never notifies about
 * something the publisher's own transaction later rolls back.
 *
 * <p>{@code type} is the raw {@code notifications.type} CHECK-constraint value (e.g. {@code
 * "GRADE_PUBLISHED"}), not the {@code notification.domain.NotificationType} enum itself — {@code
 * common} depends on nothing, so it cannot reference a type owned by another module.
 */
public record NotificationRequestedEvent(
        UUID recipientUserId,
        String type,
        Map<String, Object> payload,
        String dedupeKey,
        Instant occurredAt) {}
