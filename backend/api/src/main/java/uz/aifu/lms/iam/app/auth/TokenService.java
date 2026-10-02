package uz.aifu.lms.iam.app.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.aifu.lms.common.config.JwtProperties;
import uz.aifu.lms.common.security.AccessTokenAuthenticator;
import uz.aifu.lms.common.security.AuthenticatedUser;
import uz.aifu.lms.iam.domain.User;

/**
 * Mints and parses tokens (ADR-003).
 *
 * <ul>
 *   <li><b>Access token</b>: HS256 JWT, short TTL, carries roles + permissions so authorization
 *       needs no database round trip.
 *   <li><b>Refresh token</b>: 256 bits from {@link SecureRandom}, returned to the client once and
 *       stored only as a SHA-256 hash.
 * </ul>
 *
 * <p>Never log a token, whole or partial.
 */
@Service
public class TokenService implements AccessTokenAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "perms";
    private static final String CLAIM_USERNAME = "username";

    private final JwtProperties properties;
    private final Clock clock;
    private final SecretKey signingKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public IssuedAccessToken issueAccessToken(User user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.accessTokenTtl());
        String jwt =
                Jwts.builder()
                        .issuer(properties.issuer())
                        .subject(user.getId().toString())
                        .id(UUID.randomUUID().toString())
                        .issuedAt(Date.from(now))
                        .expiration(Date.from(expiresAt))
                        .claim(CLAIM_USERNAME, user.getUsername())
                        .claim(CLAIM_ROLES, List.copyOf(user.roleCodes()))
                        .claim(CLAIM_PERMISSIONS, List.copyOf(user.permissionCodes()))
                        .signWith(signingKey)
                        .compact();
        return new IssuedAccessToken(jwt, expiresAt, properties.accessTokenTtl().toSeconds());
    }

    /**
     * Parses and validates an access token.
     *
     * @return the caller, or empty when the token is missing, malformed, expired or badly signed.
     *     The reason is intentionally not returned: a client learns only "unauthenticated".
     */
    @Override
    public Optional<AuthenticatedUser> parseAccessToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims =
                    Jwts.parser()
                            .verifyWith(signingKey)
                            .requireIssuer(properties.issuer())
                            .clock(() -> Date.from(clock.instant()))
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
            UUID userId = UUID.fromString(claims.getSubject());
            return Optional.of(
                    new AuthenticatedUser(
                            userId,
                            claims.get(CLAIM_USERNAME, String.class),
                            toStringSet(claims.get(CLAIM_ROLES)),
                            toStringSet(claims.get(CLAIM_PERMISSIONS))));
        } catch (ExpiredJwtException ex) {
            LOG.debug("Rejected expired access token");
            return Optional.empty();
        } catch (JwtException | IllegalArgumentException ex) {
            // Includes bad signature, malformed token and unsupported algorithm.
            LOG.debug("Rejected invalid access token: {}", ex.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    /**
     * @return the raw refresh token. Returned to the client exactly once; never persisted raw.
     */
    public String generateRefreshTokenValue() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * SHA-256, not BCrypt: these are 256-bit random values, so we need lookup speed, not key
     * stretching.
     */
    public String hashRefreshToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder()
                    .encodeToString(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> toStringSet(Object claim) {
        if (claim instanceof List<?> list) {
            Set<String> values = new HashSet<>();
            list.forEach(item -> values.add(String.valueOf(item)));
            return values;
        }
        return Set.of();
    }

    public record IssuedAccessToken(String token, Instant expiresAt, long expiresInSeconds) {}
}
