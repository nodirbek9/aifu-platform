package uz.aifu.lms.common.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT settings (ADR-003).
 *
 * <p>A missing or weak secret FAILS STARTUP rather than warning. A platform that boots with a
 * guessable signing key is worse than one that refuses to boot.
 */
@Validated
@ConfigurationProperties(prefix = "aifu.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @NotBlank String issuer,
        @NotNull Duration accessTokenTtl,
        @NotNull Duration refreshTokenTtl) {

    private static final int MIN_SECRET_BYTES = 32; // HS256 requirement

    @PostConstruct
    void validate() {
        int length = secret == null ? 0 : secret.getBytes(StandardCharsets.UTF_8).length;
        if (length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least "
                            + MIN_SECRET_BYTES
                            + " bytes (got "
                            + length
                            + "). Generate one with: openssl rand -base64 48");
        }
        if (accessTokenTtl.compareTo(Duration.ofHours(2)) > 0) {
            throw new IllegalStateException(
                    "Access token TTL "
                            + accessTokenTtl
                            + " is too long. Revocation relies on a short access lifetime (ADR-003); keep it <= 2h.");
        }
        if (refreshTokenTtl.compareTo(accessTokenTtl) <= 0) {
            throw new IllegalStateException("Refresh token TTL must exceed the access token TTL");
        }
    }
}
