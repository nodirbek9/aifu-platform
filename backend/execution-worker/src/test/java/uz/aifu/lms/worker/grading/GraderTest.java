package uz.aifu.lms.worker.grading;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.worker.domain.TestCaseRecord;
import uz.aifu.lms.worker.sandbox.SandboxRunResult;

class GraderTest {

    private static final int MEMORY_LIMIT_KB = 131072;
    private static final int CPU_TIME_LIMIT_MS = 2000;

    private static TestCaseRecord testCase(String expectedStdout) {
        return new TestCaseRecord(
                UUID.randomUUID(),
                0,
                "3\n1 2 3",
                expectedStdout,
                "TRIMMED",
                null,
                BigDecimal.ONE,
                null);
    }

    @Test
    void acceptsMatchingOutput() {
        SandboxRunResult result =
                new SandboxRunResult(true, null, 0, "6", "", 10, null, false, false, null);
        TestOutcome outcome =
                Grader.grade(testCase("6"), MEMORY_LIMIT_KB, CPU_TIME_LIMIT_MS, result);
        assertThat(outcome.passed()).isTrue();
        assertThat(outcome.verdict()).isEqualTo("ACCEPTED");
    }

    @Test
    void rejectsMismatchedOutputAsWrongAnswer() {
        SandboxRunResult result =
                new SandboxRunResult(true, null, 0, "7", "", 10, null, false, false, null);
        TestOutcome outcome =
                Grader.grade(testCase("6"), MEMORY_LIMIT_KB, CPU_TIME_LIMIT_MS, result);
        assertThat(outcome.passed()).isFalse();
        assertThat(outcome.verdict()).isEqualTo("WRONG_ANSWER");
    }

    @Test
    void nonZeroExitIsRuntimeError() {
        SandboxRunResult result =
                new SandboxRunResult(true, null, 1, "", "Traceback", 5, null, false, false, null);
        TestOutcome outcome =
                Grader.grade(testCase("6"), MEMORY_LIMIT_KB, CPU_TIME_LIMIT_MS, result);
        assertThat(outcome.verdict()).isEqualTo("RUNTIME_ERROR");
    }

    @Test
    void javaSideWallClockTimeoutIsTimeLimitExceeded() {
        SandboxRunResult result =
                new SandboxRunResult(true, null, null, "", "", 15000, null, true, false, null);
        TestOutcome outcome =
                Grader.grade(testCase("6"), MEMORY_LIMIT_KB, CPU_TIME_LIMIT_MS, result);
        assertThat(outcome.verdict()).isEqualTo("TIME_LIMIT_EXCEEDED");
    }

    @Test
    void truncatedOutputIsOutputLimitExceeded() {
        SandboxRunResult result =
                new SandboxRunResult(
                        true, null, 0, "x".repeat(100), "", 10, null, false, true, null);
        TestOutcome outcome =
                Grader.grade(testCase("6"), MEMORY_LIMIT_KB, CPU_TIME_LIMIT_MS, result);
        assertThat(outcome.verdict()).isEqualTo("OUTPUT_LIMIT_EXCEEDED");
    }

    @Test
    void reportedMemoryOverLimitIsMemoryLimitExceeded() {
        SandboxRunResult result =
                new SandboxRunResult(
                        true, null, 0, "6", "", 10, MEMORY_LIMIT_KB + 1, false, false, null);
        TestOutcome outcome =
                Grader.grade(testCase("6"), MEMORY_LIMIT_KB, CPU_TIME_LIMIT_MS, result);
        assertThat(outcome.verdict()).isEqualTo("MEMORY_LIMIT_EXCEEDED");
    }

    @Test
    void sandboxInternalErrorTakesPriorityOverEverything() {
        SandboxRunResult result = SandboxRunResult.internalFailure("docker daemon unreachable");
        TestOutcome outcome =
                Grader.grade(testCase("6"), MEMORY_LIMIT_KB, CPU_TIME_LIMIT_MS, result);
        assertThat(outcome.verdict()).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void bareSigkillNearCpuCeilingIsClassifiedTimeLimitExceeded() {
        // No Java-side timedOut flag, no reported memoryKb — exactly docker-run's ulimit -t path.
        SandboxRunResult result =
                new SandboxRunResult(true, null, 137, "", "", 1950, null, false, false, null);
        TestOutcome outcome =
                Grader.grade(testCase("6"), MEMORY_LIMIT_KB, CPU_TIME_LIMIT_MS, result);
        assertThat(outcome.verdict()).isEqualTo("TIME_LIMIT_EXCEEDED");
    }

    @Test
    void bareSigkillWellShortOfCpuCeilingIsClassifiedMemoryLimitExceeded() {
        // Killed at 400ms against a 2000ms CPU ceiling — must have been the --memory cgroup OOM.
        SandboxRunResult result =
                new SandboxRunResult(true, null, 137, "", "", 400, null, false, false, null);
        TestOutcome outcome =
                Grader.grade(testCase("6"), MEMORY_LIMIT_KB, CPU_TIME_LIMIT_MS, result);
        assertThat(outcome.verdict()).isEqualTo("MEMORY_LIMIT_EXCEEDED");
    }
}
