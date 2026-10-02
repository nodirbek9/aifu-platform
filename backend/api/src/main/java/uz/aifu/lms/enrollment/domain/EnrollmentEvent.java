package uz.aifu.lms.enrollment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.common.util.Guards;

/**
 * Append-only history of every {@link Enrollment} transition (CLAUDE.md §5: "Historical data is
 * append-only"). Never updated, never deleted — one row per transition, forever. Not a {@link
 * uz.aifu.lms.common.domain.BaseEntity} on purpose: no {@code updated_at}/{@code version} column
 * exists for a row that is never mutated, matching {@code audit_log}'s shape.
 */
@Entity
@Table(name = "enrollment_events")
public class EnrollmentEvent {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private Enrollment enrollment;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false)
    private EnrollmentEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", updatable = false)
    private EnrollmentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", updatable = false)
    private EnrollmentStatus toStatus;

    /**
     * Deliberately a bare UUID, not a {@code @ManyToOne} to {@code iam.domain.User} — matches
     * {@code audit_log}'s actor column; this history must remain readable even if a user account is
     * later removed, and must never gain a live FK that a REQUIRES_NEW-style writer could deadlock
     * against (see audit_log's V013 fix).
     */
    @Column(name = "actor_user_id", updatable = false)
    private UUID actorUserId;

    @Column(name = "reason", updatable = false)
    private String reason;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected EnrollmentEvent() {}

    public EnrollmentEvent(
            Enrollment enrollment,
            EnrollmentEventType eventType,
            EnrollmentStatus fromStatus,
            EnrollmentStatus toStatus,
            UUID actorUserId,
            String reason,
            Instant occurredAt) {
        this.enrollment = Guards.required(enrollment, "enrollment");
        this.eventType = Guards.required(eventType, "eventType");
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actorUserId = actorUserId;
        this.reason = reason;
        this.occurredAt = Guards.required(occurredAt, "occurredAt");
    }

    public UUID getId() {
        return id;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public EnrollmentEventType getEventType() {
        return eventType;
    }

    public EnrollmentStatus getFromStatus() {
        return fromStatus;
    }

    public EnrollmentStatus getToStatus() {
        return toStatus;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public String getReason() {
        return reason;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
