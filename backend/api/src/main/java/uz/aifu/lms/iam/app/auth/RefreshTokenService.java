package uz.aifu.lms.iam.app.auth;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.config.JwtProperties;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.iam.domain.RefreshToken;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.iam.persistence.RefreshTokenRepository;

/**
 * Refresh-token lifecycle: issue, rotate, revoke, and detect replay (ADR-003).
 *
 * <p>The replay rule is the important one. If a token that has already been rotated is presented
 * again, either the client is buggy or a token was stolen. We cannot tell which, so we assume theft
 * and revoke the entire rotation family. That converts a silent compromise into a visible, audited
 * event and forces re-authentication.
 */
@Service
public class RefreshTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository repository;
    private final TokenService tokenService;
    private final JwtProperties properties;
    private final AuditService auditService;
    private final Clock clock;

    public RefreshTokenService(
            RefreshTokenRepository repository,
            TokenService tokenService,
            JwtProperties properties,
            AuditService auditService,
            Clock clock) {
        this.repository = repository;
        this.tokenService = tokenService;
        this.properties = properties;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public IssuedRefreshToken issue(User user, String userAgent, String ipAddress) {
        return persist(user, UUID.randomUUID(), userAgent, ipAddress);
    }

    /**
     * Validates a presented refresh token.
     *
     * @throws uz.aifu.lms.common.error.DomainException with {@code AUTH_REFRESH_TOKEN_REUSED} when
     *     a revoked token is replayed (after revoking the family), or {@code
     *     AUTH_REFRESH_TOKEN_INVALID} when it is unknown or expired.
     */
    @Transactional
    public RefreshToken requireUsable(String rawToken, String ipAddress, String userAgent) {
        if (rawToken == null || rawToken.isBlank()) {
            throw Exceptions.rule(
                    ErrorCode.AUTH_REFRESH_TOKEN_INVALID, "Refresh token is required");
        }
        String hash = tokenService.hashRefreshToken(rawToken);
        RefreshToken token =
                repository
                        .findByTokenHash(hash)
                        .orElseThrow(
                                () ->
                                        Exceptions.rule(
                                                ErrorCode.AUTH_REFRESH_TOKEN_INVALID,
                                                "Refresh token is not valid"));

        Instant now = clock.instant();
        if (token.isRevoked()) {
            UUID userId = token.getUser().getId();
            LOG.warn(
                    "Refresh token REUSE detected for user {} (family {}). Revoking family.",
                    userId,
                    token.getFamilyId());
            repository.revokeFamily(
                    token.getFamilyId(), RefreshToken.RevocationReason.REUSE_DETECTED, now);
            auditService.recordFailure(
                    AuditAction.TOKEN_REUSE_DETECTED,
                    userId,
                    "refresh_token",
                    token.getId(),
                    ipAddress,
                    userAgent,
                    "family-revoked");
            throw Exceptions.rule(
                    ErrorCode.AUTH_REFRESH_TOKEN_REUSED,
                    "This session has been invalidated. Please sign in again.");
        }
        if (!token.isActive(now)) {
            throw Exceptions.rule(
                    ErrorCode.AUTH_REFRESH_TOKEN_INVALID, "Refresh token has expired");
        }
        return token;
    }

    /** Single-use rotation: the presented token dies, a successor in the same family is issued. */
    @Transactional
    public IssuedRefreshToken rotate(
            RefreshToken current, User user, String userAgent, String ipAddress) {
        IssuedRefreshToken replacement = persist(user, current.getFamilyId(), userAgent, ipAddress);
        current.replacedBy(repository.getReferenceById(replacement.entityId()), clock.instant());
        repository.save(current);
        return replacement;
    }

    @Transactional
    public void revokeByRawValue(String rawToken, RefreshToken.RevocationReason reason) {
        if (rawToken == null || rawToken.isBlank()) {
            return; // Logout is idempotent: nothing to revoke is a success, not an error.
        }
        repository
                .findByTokenHash(tokenService.hashRefreshToken(rawToken))
                .ifPresent(
                        token -> {
                            token.revoke(reason, clock.instant());
                            repository.save(token);
                        });
    }

    @Transactional
    public void revokeFamily(UUID familyId, RefreshToken.RevocationReason reason) {
        repository.revokeFamily(familyId, reason, clock.instant());
    }

    @Transactional
    public void revokeAllForUser(UUID userId, RefreshToken.RevocationReason reason) {
        int revoked = repository.revokeAllForUser(userId, reason, clock.instant());
        LOG.info("Revoked {} refresh token(s) for user {} ({})", revoked, userId, reason);
    }

    private IssuedRefreshToken persist(
            User user, UUID familyId, String userAgent, String ipAddress) {
        String raw = tokenService.generateRefreshTokenValue();
        Instant now = clock.instant();
        RefreshToken token =
                new RefreshToken(
                        user,
                        tokenService.hashRefreshToken(raw),
                        familyId,
                        now,
                        now.plus(properties.refreshTokenTtl()),
                        userAgent,
                        ipAddress);
        RefreshToken saved = repository.save(token);
        return new IssuedRefreshToken(raw, saved.getId(), saved.getExpiresAt());
    }

    /** {@code rawValue} is returned to the client once; only its hash is stored. */
    public record IssuedRefreshToken(String rawValue, UUID entityId, Instant expiresAt) {}
}
