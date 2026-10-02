package uz.aifu.lms.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One immutable audit record.
 *
 * <p>The table is append-only and enforced as such by database triggers ({@code
 * trg_audit_log_no_update} / {@code trg_audit_log_no_delete}). This entity therefore has no setters
 * and must never be modified after {@link #persist} time: attempting an UPDATE raises a database
 * exception by design.
 *
 * <p>SECURITY: {@link #details} must never contain a password, password hash, JWT, refresh token,
 * or student source code. See {@link uz.aifu.lms.audit.app.AuditService}.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    /**
     * Actor as a plain UUID, deliberately not a {@code @ManyToOne}: the audit module must not
     * depend on the iam entity graph, and the row survives {@code ON DELETE SET NULL}.
     */
    @Column(name = "actor_user_id", updatable = false)
    private UUID actorUserId;

    /** Human-readable actor snapshot, retained even if the user row is later removed. */
    @Column(name = "actor_label", updatable = false)
    private String actorLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, updatable = false)
    private AuditAction action;

    @Column(name = "entity_type", updatable = false)
    private String entityType;

    @Column(name = "entity_id", updatable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, updatable = false)
    private AuditOutcome outcome;

    @Column(name = "request_id", updatable = false)
    private String requestId;

    /** Mapped as text with an explicit write-cast so JDBC does not fight the {@code inet} type. */
    @Column(name = "ip_address", updatable = false, columnDefinition = "inet")
    @ColumnTransformer(write = "?::inet")
    private String ipAddress;

    @Column(name = "user_agent", updatable = false)
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", nullable = false, updatable = false)
    private String details;

    protected AuditLog() {
        // JPA
    }

    private AuditLog(Builder b) {
        this.occurredAt = b.occurredAt == null ? Instant.now() : b.occurredAt;
        this.actorUserId = b.actorUserId;
        this.actorLabel = b.actorLabel;
        this.action = b.action;
        this.entityType = b.entityType;
        this.entityId = b.entityId;
        this.outcome = b.outcome == null ? AuditOutcome.SUCCESS : b.outcome;
        this.requestId = b.requestId;
        this.ipAddress = b.ipAddress;
        this.userAgent = b.userAgent;
        this.details = b.details == null ? "{}" : b.details;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public String getActorLabel() {
        return actorLabel;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public AuditOutcome getOutcome() {
        return outcome;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getDetails() {
        return details;
    }

    /** Mutable builder; the built entity is immutable. */
    public static final class Builder {
        private Instant occurredAt;
        private UUID actorUserId;
        private String actorLabel;
        private AuditAction action;
        private String entityType;
        private UUID entityId;
        private AuditOutcome outcome;
        private String requestId;
        private String ipAddress;
        private String userAgent;
        private String details;

        public Builder occurredAt(Instant v) {
            this.occurredAt = v;
            return this;
        }

        public Builder actorUserId(UUID v) {
            this.actorUserId = v;
            return this;
        }

        public Builder actorLabel(String v) {
            this.actorLabel = v;
            return this;
        }

        public Builder action(AuditAction v) {
            this.action = v;
            return this;
        }

        public Builder entityType(String v) {
            this.entityType = v;
            return this;
        }

        public Builder entityId(UUID v) {
            this.entityId = v;
            return this;
        }

        public Builder outcome(AuditOutcome v) {
            this.outcome = v;
            return this;
        }

        public Builder requestId(String v) {
            this.requestId = v;
            return this;
        }

        public Builder ipAddress(String v) {
            this.ipAddress = v;
            return this;
        }

        public Builder userAgent(String v) {
            this.userAgent = v;
            return this;
        }

        public Builder details(String json) {
            this.details = json;
            return this;
        }

        public AuditLog build() {
            if (action == null) {
                throw new IllegalStateException("AuditLog.action is required");
            }
            return new AuditLog(this);
        }
    }
}
