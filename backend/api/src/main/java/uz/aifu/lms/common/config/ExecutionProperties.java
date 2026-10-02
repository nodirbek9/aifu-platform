package uz.aifu.lms.common.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * API-side execution settings (ADR-006).
 *
 * <p>The API only ENQUEUES work; these are the ceilings it enforces when accepting a submission and
 * when clamping a task's own limits. A coding task may TIGHTEN a limit; it can never exceed the
 * ceiling. {@link #clampCpuTimeMs} and friends are the enforcement points — do not bypass them.
 */
@Validated
@ConfigurationProperties(prefix = "aifu.execution")
public record ExecutionProperties(
        @Min(1024) int maxSourceBytes,
        @Min(1024) int maxOutputBytes,
        @Min(100) int ceilingCpuTimeLimitMs,
        @Min(100) int ceilingWallTimeLimitMs,
        @Min(16384) int ceilingMemoryLimitKb,
        @Min(1) int maxConcurrentSubmissionsPerStudent,
        @Min(1) int submissionRateLimitPerMinute,
        @Min(1) int maxConcurrentRunsPerUser,
        @Min(1) int runRateLimitPerMinute,
        @Min(1024) int maxStdinBytes,
        @NotNull Duration runRetention,
        @NotNull Duration verifyRunRetention,
        @NotNull Duration runGiveUpAfter) {

    public int clampCpuTimeMs(Integer requested) {
        return requested == null
                ? ceilingCpuTimeLimitMs
                : Math.min(requested, ceilingCpuTimeLimitMs);
    }

    public int clampWallTimeMs(Integer requested) {
        return requested == null
                ? ceilingWallTimeLimitMs
                : Math.min(requested, ceilingWallTimeLimitMs);
    }

    public int clampMemoryKb(Integer requested) {
        return requested == null ? ceilingMemoryLimitKb : Math.min(requested, ceilingMemoryLimitKb);
    }
}
