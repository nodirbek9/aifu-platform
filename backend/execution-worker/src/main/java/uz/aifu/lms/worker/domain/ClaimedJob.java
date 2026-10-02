package uz.aifu.lms.worker.domain;

import java.util.UUID;

/** One row claimed off {@code execution_jobs} (ADR-007). */
public record ClaimedJob(UUID jobId, UUID submissionId, int attemptCount, int maxAttempts) {}
