package uz.aifu.lms.worker.domain;

import java.util.UUID;

/**
 * One claimed {@code code_runs} row (V022): an ungraded run. {@code mode} is CUSTOM (run with
 * {@code stdin}), EXAMPLES (the task's example tests) or VERIFY (all of the task's tests).
 */
public record ClaimedRun(
        UUID id,
        UUID codingTaskId,
        String languageCode,
        String sourceCode,
        String mode,
        String stdin,
        int attemptCount) {}
