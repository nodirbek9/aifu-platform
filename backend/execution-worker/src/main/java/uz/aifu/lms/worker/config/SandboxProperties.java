package uz.aifu.lms.worker.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * {@code aifu.sandbox.*} — the sandbox ceilings (ADR-006). A coding task's own limits (read from
 * {@code coding_tasks}) may tighten these; they can never exceed them. {@link #clampCpuTimeMs} and
 * friends are the enforcement point, mirroring {@code api}'s {@code ExecutionProperties} — this
 * module cannot depend on {@code api}, so the same clamp logic is deliberately duplicated here
 * rather than shared.
 */
@Validated
@ConfigurationProperties(prefix = "aifu.sandbox")
public record SandboxProperties(
        @NotBlank String engine,
        String judge0Url,
        String judge0Token,
        @Min(100) int defaultCpuTimeLimitMs,
        @Min(100) int defaultWallTimeLimitMs,
        @Min(16384) int defaultMemoryLimitKb,
        @Min(1) int maxProcesses,
        @Min(1024) int maxOutputBytes,
        boolean enableNetwork) {

    public int clampCpuTimeMs(Integer requested) {
        return requested == null
                ? defaultCpuTimeLimitMs
                : Math.min(requested, defaultCpuTimeLimitMs);
    }

    public int clampWallTimeMs(Integer requested) {
        return requested == null
                ? defaultWallTimeLimitMs
                : Math.min(requested, defaultWallTimeLimitMs);
    }

    public int clampMemoryKb(Integer requested) {
        return requested == null ? defaultMemoryLimitKb : Math.min(requested, defaultMemoryLimitKb);
    }
}
