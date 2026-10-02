package uz.aifu.lms.iam.app.auth;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.config.SecurityProperties;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.iam.app.user.UserService;
import uz.aifu.lms.iam.domain.RefreshToken;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.iam.persistence.UserRepository;

/**
 * Login, refresh and logout (ADR-003).
 *
 * <p>Security properties enforced here, all of them deliberate:
 *
 * <ol>
 *   <li>Every login failure returns the SAME error ({@code AUTH_INVALID_CREDENTIALS}) — no account
 *       enumeration, no "your account is locked" disclosure to an unauthenticated caller.
 *   <li>A password hash is ALWAYS compared, even for a non-existent user, so response time does not
 *       reveal whether the account exists.
 *   <li>Account status is re-checked on refresh, so a suspension bites within one access-token TTL.
 *   <li>Refresh tokens are single-use and rotated; replaying one revokes the whole family.
 * </ol>
 */
@Service
public class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    /**
     * A valid BCrypt hash of a random value, used to burn the same CPU time when the account does
     * not exist. Without this, "user not found" returns measurably faster than "wrong password".
     */
    private static final String DUMMY_HASH =
            "$2a$12$abcdefghijklmnopqrstuv0123456789ABCDEFGHIJKLMNOPQRSTU";

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final SecurityProperties securityProperties;
    private final AuditService auditService;
    private final Clock clock;

    public AuthenticationService(
            UserRepository userRepository,
            UserService userService,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            RefreshTokenService refreshTokenService,
            SecurityProperties securityProperties,
            AuditService auditService,
            Clock clock) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.securityProperties = securityProperties;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public AuthTokens login(
            String identifier, String rawPassword, String userAgent, String ipAddress) {
        Instant now = clock.instant();
        Optional<User> candidate = userRepository.findByUsernameOrEmailWithAuthorities(identifier);

        if (candidate.isEmpty()) {
            // Burn equivalent time, then fail identically to a wrong password.
            passwordEncoder.matches(rawPassword, DUMMY_HASH);
            auditService.recordFailure(
                    AuditAction.LOGIN_FAILURE,
                    null,
                    "user",
                    null,
                    ipAddress,
                    userAgent,
                    "unknown-identifier");
            throw invalidCredentials();
        }

        User user = candidate.get();

        if (!user.canAuthenticate(now)) {
            // Covers PENDING / SUSPENDED / DISABLED and active lockouts. Same generic error.
            passwordEncoder.matches(rawPassword, DUMMY_HASH);
            auditService.recordFailure(
                    AuditAction.LOGIN_FAILURE,
                    user.getId(),
                    "user",
                    user.getId(),
                    ipAddress,
                    userAgent,
                    user.isLocked(now) ? "locked" : "status-" + user.getStatus());
            throw invalidCredentials();
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            // Committed in its own transaction (UserService.recordFailedLogin), independently of
            // this method's own transaction — see that method's javadoc for why.
            boolean lockedNow =
                    userService.recordFailedLogin(
                            user.getId(),
                            securityProperties.maxFailedLoginAttempts(),
                            securityProperties.lockoutDuration(),
                            now);
            auditService.recordFailure(
                    AuditAction.LOGIN_FAILURE,
                    user.getId(),
                    "user",
                    user.getId(),
                    ipAddress,
                    userAgent,
                    "bad-password");
            if (lockedNow) {
                LOG.warn(
                        "Account {} locked after {} failed attempts",
                        user.getId(),
                        securityProperties.maxFailedLoginAttempts());
                auditService.record(
                        AuditAction.ACCOUNT_LOCKED,
                        user.getId(),
                        "user",
                        user.getId(),
                        ipAddress,
                        userAgent);
            }
            throw invalidCredentials();
        }

        user.registerSuccessfulLogin(now);
        userRepository.save(user);
        auditService.record(
                AuditAction.LOGIN_SUCCESS,
                user.getId(),
                "user",
                user.getId(),
                ipAddress,
                userAgent);

        var access = tokenService.issueAccessToken(user);
        var refresh = refreshTokenService.issue(user, userAgent, ipAddress);
        return new AuthTokens(
                access.token(),
                refresh.rawValue(),
                access.expiresInSeconds(),
                access.expiresAt(),
                user.isMustChangePassword());
    }

    /** Single-use rotation with replay detection. */
    @Transactional
    public AuthTokens refresh(String rawRefreshToken, String userAgent, String ipAddress) {
        Instant now = clock.instant();
        RefreshToken existing =
                refreshTokenService.requireUsable(rawRefreshToken, ipAddress, userAgent);
        User user =
                userRepository
                        .findWithAuthoritiesById(existing.getUser().getId())
                        .orElseThrow(() -> invalidRefresh());

        if (!user.canAuthenticate(now)) {
            // Suspended between issue and refresh: kill the family, deny.
            refreshTokenService.revokeFamily(
                    existing.getFamilyId(), RefreshToken.RevocationReason.ADMIN_REVOKED);
            throw Exceptions.rule(ErrorCode.AUTH_ACCOUNT_NOT_ACTIVE, "Account is not active");
        }

        var rotated = refreshTokenService.rotate(existing, user, userAgent, ipAddress);
        var access = tokenService.issueAccessToken(user);
        auditService.record(
                AuditAction.TOKEN_REFRESH,
                user.getId(),
                "refresh_token",
                rotated.entityId(),
                ipAddress,
                userAgent);
        return new AuthTokens(
                access.token(),
                rotated.rawValue(),
                access.expiresInSeconds(),
                access.expiresAt(),
                user.isMustChangePassword());
    }

    @Transactional
    public void logout(String rawRefreshToken, UUID actorId, String ipAddress, String userAgent) {
        refreshTokenService.revokeByRawValue(rawRefreshToken, RefreshToken.RevocationReason.LOGOUT);
        auditService.record(AuditAction.LOGOUT, actorId, "user", actorId, ipAddress, userAgent);
    }

    /** Revokes every refresh token for the user. Access tokens still expire naturally (ADR-003). */
    @Transactional
    public void logoutAll(UUID userId, String ipAddress, String userAgent) {
        refreshTokenService.revokeAllForUser(userId, RefreshToken.RevocationReason.LOGOUT_ALL);
        auditService.record(AuditAction.LOGOUT_ALL, userId, "user", userId, ipAddress, userAgent);
    }

    @Transactional
    public void changePassword(
            UUID userId, String currentPassword, String newPassword, String ipAddress) {
        User user =
                userRepository
                        .findWithAuthoritiesById(userId)
                        .orElseThrow(() -> Exceptions.notFound("User", userId));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            auditService.recordFailure(
                    AuditAction.PASSWORD_CHANGED,
                    userId,
                    "user",
                    userId,
                    ipAddress,
                    null,
                    "bad-current-password");
            throw invalidCredentials();
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw Exceptions.validation("New password must differ from the current one");
        }
        user.changePassword(passwordEncoder.encode(newPassword), clock.instant());
        userRepository.save(user);
        // A password change invalidates every existing session: standard containment after a
        // suspected compromise.
        refreshTokenService.revokeAllForUser(
                userId, RefreshToken.RevocationReason.PASSWORD_CHANGED);
        auditService.record(AuditAction.PASSWORD_CHANGED, userId, "user", userId, ipAddress, null);
    }

    private DomainExceptionAlias invalidCredentials() {
        return new DomainExceptionAlias(
                ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid username or password");
    }

    private DomainExceptionAlias invalidRefresh() {
        return new DomainExceptionAlias(
                ErrorCode.AUTH_REFRESH_TOKEN_INVALID, "Refresh token is not valid");
    }

    /** Thin alias so the generic messages above are declared exactly once. */
    private static final class DomainExceptionAlias
            extends uz.aifu.lms.common.error.DomainException {
        DomainExceptionAlias(ErrorCode code, String message) {
            super(code, message);
        }
    }

    public record AuthTokens(
            String accessToken,
            String refreshToken,
            long expiresInSeconds,
            Instant expiresAt,
            boolean mustChangePassword) {}
}
