package uz.aifu.lms.worker.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** {@code aifu.worker.*} — polling/claim/lease settings for this process (ADR-007). */
@Validated
@ConfigurationProperties(prefix = "aifu.worker")
public record WorkerProperties(
        @NotBlank String id,
        @Min(1) int concurrency,
        Duration pollInterval,
        @Min(1) int claimBatchSize,
        Duration leaseDuration,
        @Min(1) int maxAttempts) {}
