package uz.aifu.lms.notification.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.events.NotificationRequestedEvent;
import uz.aifu.lms.notification.domain.Notification;
import uz.aifu.lms.notification.domain.NotificationChannel;
import uz.aifu.lms.notification.domain.NotificationStatus;
import uz.aifu.lms.notification.domain.NotificationType;
import uz.aifu.lms.notification.persistence.NotificationRepository;

/**
 * In-app notifications (V013, brief §61). {@code IN_APP} is the only dispatched channel in v1, so
 * {@link Notification}'s constructor marks a row {@code SENT} the moment it is created — there is
 * no separate dispatch job to build here.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final NotificationService self;

    public NotificationService(
            NotificationRepository notifications,
            ObjectMapper objectMapper,
            Clock clock,
            @Lazy NotificationService self) {
        this.notifications = notifications;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.self = self;
    }

    /**
     * Any module that already holds a real recipient {@code UUID} can publish {@link
     * NotificationRequestedEvent} directly (see that record's own javadoc for why this needed no
     * new module-map edge). {@code AFTER_COMMIT}, routed through {@code self} — the same self-proxy
     * reasoning {@code grading.app.GradeEntryService}'s listener documents: a listener invoked from
     * {@code triggerAfterCompletion} has no active transaction, and a direct {@code
     * this.notify(...)} call would bypass the proxy that applies {@link Transactional}. {@link
     * #notify} itself must be {@code REQUIRES_NEW}, not plain {@code @Transactional} (confirmed by
     * a real {@code TransactionRequiredException} the first time this listener actually ran, see
     * {@code docs/BUILD-STATUS.md}) — the same stale-thread-bound-resource trap {@code
     * GradeEntryService.recordAutoGrade}'s own javadoc already warns about.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationRequested(NotificationRequestedEvent event) {
        self.notify(
                event.recipientUserId(),
                NotificationType.valueOf(event.type()),
                event.payload(),
                event.dedupeKey(),
                event.occurredAt());
    }

    /**
     * Creates one notification. A repeat call with the same {@code dedupeKey} for the same
     * recipient is a silent no-op ({@code uq_notification_dedupe}, V013) — the unique-violation
     * path, not an app-level pre-check, is the actual guarantee under concurrency (the same pattern
     * {@code EnrollmentService.enroll} uses for {@code uq_enrollment_active}).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notify(
            UUID recipientUserId,
            NotificationType type,
            Map<String, ?> payload,
            String dedupeKey,
            Instant now) {
        Notification entry =
                new Notification(
                        recipientUserId,
                        type,
                        NotificationChannel.IN_APP,
                        toJson(payload),
                        dedupeKey,
                        now == null ? clock.instant() : now);
        try {
            notifications.save(entry);
            notifications.flush();
        } catch (DataIntegrityViolationException e) {
            log.debug(
                    "notification.dedupe_skip recipient={} type={} dedupeKey={}",
                    recipientUserId,
                    type,
                    dedupeKey);
        }
    }

    @Transactional(readOnly = true)
    public Page<Notification> listOwn(UUID recipientUserId, boolean unreadOnly, Pageable pageable) {
        return unreadOnly
                ? notifications.findByRecipientUserIdAndStatusOrderByCreatedAtDesc(
                        recipientUserId, NotificationStatus.SENT, pageable)
                : notifications.findByRecipientUserIdOrderByCreatedAtDesc(
                        recipientUserId, pageable);
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID recipientUserId) {
        return notifications.countByRecipientUserIdAndStatus(
                recipientUserId, NotificationStatus.SENT);
    }

    @Transactional
    public Notification markRead(UUID id, UUID callerId) {
        Notification entry =
                notifications
                        .findByIdAndRecipientUserId(id, callerId)
                        .orElseThrow(() -> Exceptions.notFound("Notification", id));
        entry.markRead(clock.instant());
        return entry;
    }

    private String toJson(Map<String, ?> payload) {
        if (payload == null || payload.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("notification.payload.serialization_failed", e);
            return "{}";
        }
    }
}
