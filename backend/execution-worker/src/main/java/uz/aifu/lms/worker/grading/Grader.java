package uz.aifu.lms.worker.grading;

import java.nio.charset.StandardCharsets;
import uz.aifu.lms.worker.domain.TestCaseRecord;
import uz.aifu.lms.worker.sandbox.SandboxRunResult;

/**
 * Turns one {@link SandboxRunResult} for one test case into a {@link TestOutcome} (execution_test_
 * results verdict values, V011). Checks are ordered so an infrastructure/limit failure always takes
 * priority over an output mismatch: a timed-out or truncated run is never scored as WRONG_ANSWER.
 */
public final class Grader {

    private static final int EXCERPT_MAX_BYTES = 4096;

    /**
     * SIGKILL (9), reported by the shell as 128+9 — see {@link #grade} for why this needs a guess.
     */
    private static final int SIGKILL_EXIT_CODE = 137;

    private Grader() {}

    /**
     * {@code effectiveCpuTimeLimitMs} is the CPU-time ceiling actually enforced for this run (the
     * same value baked into the sandbox's own {@code ulimit -t}), used only to disambiguate a bare
     * SIGKILL (exit 137) — a well-behaved but resource-hungry sandbox can't always tell TLE from
     * MLE apart, since {@link SandboxRunResult#memoryKb()} is null for engines (docker-run) that
     * don't parse peak memory back out of the container. A SIGKILL far short of the CPU ceiling is
     * classified MLE (the cgroup's own {@code --memory} OOM-killed it); a SIGKILL near or past the
     * ceiling is classified TLE ({@code ulimit -t}'s SIGXCPU-then-SIGKILL). Verified empirically
     * against real docker-run runs (Phase 8): an infinite loop was SIGKILLed within ~300ms of its
     * 2000ms CPU limit; a memory bomb was SIGKILLed in 400-1200ms against that same 2000ms limit.
     */
    public static TestOutcome grade(
            TestCaseRecord testCase,
            int effectiveMemoryLimitKb,
            int effectiveCpuTimeLimitMs,
            SandboxRunResult result) {
        String stdoutExcerpt = excerpt(result.stdout());
        String stderrExcerpt = excerpt(result.stderr());

        if (result.internalError() != null) {
            return new TestOutcome(
                    testCase.id(),
                    false,
                    "INTERNAL_ERROR",
                    result.timeMs(),
                    result.memoryKb(),
                    result.exitCode(),
                    stdoutExcerpt,
                    stderrExcerpt);
        }
        if (result.timedOut()) {
            return new TestOutcome(
                    testCase.id(),
                    false,
                    "TIME_LIMIT_EXCEEDED",
                    result.timeMs(),
                    result.memoryKb(),
                    result.exitCode(),
                    stdoutExcerpt,
                    stderrExcerpt);
        }
        if (result.outputTruncated()) {
            return new TestOutcome(
                    testCase.id(),
                    false,
                    "OUTPUT_LIMIT_EXCEEDED",
                    result.timeMs(),
                    result.memoryKb(),
                    result.exitCode(),
                    stdoutExcerpt,
                    stderrExcerpt);
        }
        if (result.memoryKb() != null && result.memoryKb() > effectiveMemoryLimitKb) {
            return new TestOutcome(
                    testCase.id(),
                    false,
                    "MEMORY_LIMIT_EXCEEDED",
                    result.timeMs(),
                    result.memoryKb(),
                    result.exitCode(),
                    stdoutExcerpt,
                    stderrExcerpt);
        }
        if (result.exitCode() != null && result.exitCode() == SIGKILL_EXIT_CODE) {
            // Bare SIGKILL with neither the Java-side wall-clock timeout nor a reported memoryKb
            // over limit — must have been the sandbox's OWN ulimit/cgroup killing it. Guess from
            // timing: close to the CPU ceiling => ulimit -t (TLE); well short of it => the
            // --memory cgroup OOM-killer (MLE).
            boolean nearCpuCeiling = result.timeMs() >= effectiveCpuTimeLimitMs - 300;
            return new TestOutcome(
                    testCase.id(),
                    false,
                    nearCpuCeiling ? "TIME_LIMIT_EXCEEDED" : "MEMORY_LIMIT_EXCEEDED",
                    result.timeMs(),
                    result.memoryKb(),
                    result.exitCode(),
                    stdoutExcerpt,
                    stderrExcerpt);
        }
        if (result.exitCode() == null || result.exitCode() != 0) {
            return new TestOutcome(
                    testCase.id(),
                    false,
                    "RUNTIME_ERROR",
                    result.timeMs(),
                    result.memoryKb(),
                    result.exitCode(),
                    stdoutExcerpt,
                    stderrExcerpt);
        }

        boolean matches =
                OutputComparator.matches(
                        result.stdout(),
                        testCase.expectedStdout(),
                        testCase.comparisonMode(),
                        testCase.numericTolerance());
        return new TestOutcome(
                testCase.id(),
                matches,
                matches ? "ACCEPTED" : "WRONG_ANSWER",
                result.timeMs(),
                result.memoryKb(),
                result.exitCode(),
                stdoutExcerpt,
                stderrExcerpt);
    }

    private static String excerpt(String value) {
        if (value == null) {
            return null;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= EXCERPT_MAX_BYTES) {
            return value;
        }
        return new String(bytes, 0, EXCERPT_MAX_BYTES, StandardCharsets.UTF_8);
    }
}
