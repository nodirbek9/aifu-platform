package uz.aifu.lms.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.aifu.lms.common.util.Guards;

/**
 * One in-app notification (V013). {@code recipientUserId} is a bare {@code UUID}, not a
 * {@code @ManyToOne User} — {@code notification} may depend on {@code iam} (module map), but
 * nothing here ever needs to navigate to the recipient's other fields, only compare an id, so
 * keeping the domain layer free of another module's entity graph stays the simpler choice (the same
 * reasoning {@code grading.domain.GradeEntry} used for every id it holds besides its own module's
 * {@code enrollment}).
 *
 * <p>{@code notifications} (V013) has {@code created_at} only, no {@code updated_at}/{@code
 * version} — a third shape none of {@code common.domain}'s base classes fit (see {@code
 * Enrollment}/{@code QuizAttempt} for the same reasoning), so identity/timestamp fields are
 * declared directly rather than force-fitting one.
 *
 * <p>Only {@link NotificationChannel#IN_APP} is dispatched in v1 (brief §61): the constructor marks
 * every new row {@link NotificationStatus#SENT} immediately — there is no separate async dispatch
 * job, because an in-app row IS its own delivery. {@code EMAIL}/{@code PUSH} would need one; they
 * are out of scope here (see {@code docs/BUILD-STATUS.md}'s "What remains").
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "recipient_user_id", nullable = false, updatable = false)
    private UUID recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, updatable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, updatable = false)
    private String payload;

    @Column(name = "dedupe_key", updatable = false)
    private String dedupeKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "read_at")
    private Instant readAt;

    protected Notification() {}

    public Notification(
            UUID recipientUserId,
            NotificationType type,
            NotificationChannel channel,
            String payloadJson,
            String dedupeKey,
            Instant now) {
        this.recipientUserId = Guards.required(recipientUserId, "recipientUserId");
        this.type = Guards.required(type, "type");
        this.channel = channel == null ? NotificationChannel.IN_APP : channel;
        this.payload = payloadJson == null ? "{}" : payloadJson;
        this.dedupeKey = dedupeKey;
        this.createdAt = Guards.required(now, "now");
        if (this.channel == NotificationChannel.IN_APP) {
            this.status = NotificationStatus.SENT;
            this.sentAt = now;
        } else {
            this.status = NotificationStatus.PENDING;
        }
    }

    public void markRead(Instant now) {
        if (status == NotificationStatus.READ) {
            return;
        }
        this.status = NotificationStatus.READ;
        this.readAt = Guards.required(now, "now");
    }

    public UUID getId() {
        return id;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }

    public String getDedupeKey() {
        return dedupeKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getReadAt() {
        return readAt;
    }
}
