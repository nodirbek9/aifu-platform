package uz.aifu.lms.worker.grading;

import java.util.UUID;

/** One test case's graded outcome, before scoring is applied. */
public record TestOutcome(
        UUID testCaseId,
        boolean passed,
        String verdict,
        Integer timeMs,
        Integer memoryKb,
        Integer exitCode,
        String stdoutExcerpt,
        String stderrExcerpt) {}
