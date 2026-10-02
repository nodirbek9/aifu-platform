package uz.aifu.lms.common.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aifu.security")
public record SecurityProperties(
        /** Explicit allowlist. Never "*": credentials would be exposed to any origin. */
        List<String> corsAllowedOrigins,
        @Min(1) int maxFailedLoginAttempts,
        @NotNull Duration lockoutDuration,
        @Min(1) int authRateLimitPerMinute) {}
