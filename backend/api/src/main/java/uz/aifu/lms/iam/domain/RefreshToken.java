package uz.aifu.lms.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.ColumnTransformer;
import uz.aifu.lms.common.domain.BaseEntity;

/**
 * A single-use refresh token (ADR-003).
 *
 * <p>Only the SHA-256 hash is stored, so a database dump does not yield usable tokens. Tokens form
 * a rotation {@code family}: presenting an already-revoked token means someone replayed a stolen
 * one, so the whole family is revoked and the event is audited.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    public enum RevocationReason {
        ROTATED,
        LOGOUT,
        LOGOUT_ALL,
        REUSE_DETECTED,
        PASSWORD_CHANGED,
        ADMIN_REVOKED,
        ACCOUNT_DISABLED,
        ROLES_CHANGED,
        EXPIRED
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, updatable = false)
    private String tokenHash;

    @Column(name = "family_id", nullable = false, updatable = false)
    private UUID familyId;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_reason")
    private RevocationReason revokedReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_id")
    private RefreshToken replacedBy;

    @Column(name = "user_agent")
    private String userAgent;

    /**
     * {@code columnDefinition} alone only satisfies Hibernate's schema *validation*; the
     * transformer is what actually casts the bound parameter on write (without it every insert
     * fails: "column is of type inet but expression is of type character varying").
     */
    @Column(name = "ip_address", columnDefinition = "inet")
    @ColumnTransformer(write = "?::inet")
    private String ipAddress;

    protected RefreshToken() {}

    public RefreshToken(
            User user,
            String tokenHash,
            UUID familyId,
            Instant issuedAt,
            Instant expiresAt,
            String userAgent,
            String ipAddress) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.familyId = familyId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.userAgent = truncate(userAgent, 400);
        this.ipAddress = ipAddress;
    }

    public boolean isActive(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke(RevocationReason reason, Instant now) {
        if (revokedAt == null) {
            this.revokedAt = now;
            this.revokedReason = reason;
        }
    }

    public void replacedBy(RefreshToken replacement, Instant now) {
        revoke(RevocationReason.ROTATED, now);
        this.replacedBy = replacement;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public RevocationReason getRevokedReason() {
        return revokedReason;
    }
}
