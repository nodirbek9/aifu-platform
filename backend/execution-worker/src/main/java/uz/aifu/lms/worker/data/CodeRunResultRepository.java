package uz.aifu.lms.worker.data;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.worker.domain.TestCaseRecord;
import uz.aifu.lms.worker.grading.TestOutcome;
import uz.aifu.lms.worker.sandbox.SandboxRunResult;

/**
 * Writes the outcome of an ungraded run (V022). Every statement is guarded by {@code status =
 * 'RUNNING'}: a run the API has already given up on, or that another worker reclaimed, is left
 * alone. Nothing here touches submissions, scores or grades.
 */
@Component
public class CodeRunResultRepository {

    private final JdbcTemplate jdbc;
    private final Clock clock;

    public CodeRunResultRepository(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Transactional
    public void writeCompileFailure(UUID runId, String compileOutput) {
        jdbc.update(
                """
                UPDATE code_runs SET status = 'COMPLETED', verdict = 'COMPILATION_ERROR',
                    compile_output = ?, completed_at = ?, lease_expires_at = NULL
                WHERE id = ? AND status = 'RUNNING'
                """,
                clean(compileOutput),
                Timestamp.from(clock.instant()),
                runId);
    }

    /** A run with the author's own input: the whole output, nothing to compare it with. */
    @Transactional
    public void writeCustom(UUID runId, String verdict, SandboxRunResult result) {
        jdbc.update(
                """
                UPDATE code_runs SET status = 'COMPLETED', verdict = ?, stdout = ?, stderr = ?,
                    exit_code = ?, time_ms = ?, memory_kb = ?, completed_at = ?,
                    lease_expires_at = NULL
                WHERE id = ? AND status = 'RUNNING'
                """,
                verdict,
                clean(result.stdout()),
                clean(result.stderr()),
                result.exitCode(),
                result.timeMs(),
                result.memoryKb(),
                Timestamp.from(clock.instant()),
                runId);
    }

    /**
     * A run against test cases: ACCEPTED when every test passed, otherwise the verdict of the first
     * test that did not.
     */
    @Transactional
    public void writeTests(UUID runId, List<TestCaseRecord> testCases, List<TestOutcome> outcomes) {
        int passed = (int) outcomes.stream().filter(TestOutcome::passed).count();
        String verdict =
                outcomes.stream()
                        .filter(o -> !o.passed())
                        .map(TestOutcome::verdict)
                        .findFirst()
                        .orElse("ACCEPTED");
        int maxTimeMs =
                outcomes.stream()
                        .map(TestOutcome::timeMs)
                        .filter(java.util.Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .max()
                        .orElse(0);
        int updated =
                jdbc.update(
                        """
                        UPDATE code_runs SET status = 'COMPLETED', verdict = ?, tests_passed = ?,
                            tests_total = ?, time_ms = ?, completed_at = ?, lease_expires_at = NULL
                        WHERE id = ? AND status = 'RUNNING'
                        """,
                        verdict,
                        passed,
                        outcomes.size(),
                        maxTimeMs,
                        Timestamp.from(clock.instant()),
                        runId);
        if (updated == 0) {
            return;
        }
        jdbc.update("DELETE FROM code_run_test_results WHERE run_id = ?", runId);
        for (int i = 0; i < outcomes.size(); i++) {
            TestOutcome outcome = outcomes.get(i);
            jdbc.update(
                    """
                    INSERT INTO code_run_test_results
                        (run_id, test_case_id, position, passed, verdict, time_ms, memory_kb,
                         exit_code, stdout_excerpt, stderr_excerpt)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    runId,
                    outcome.testCaseId(),
                    testCases.get(i).position(),
                    outcome.passed(),
                    outcome.verdict(),
                    outcome.timeMs(),
                    outcome.memoryKb(),
                    outcome.exitCode(),
                    outcome.stdoutExcerpt(),
                    outcome.stderrExcerpt());
        }
    }

    /** PostgreSQL refuses NUL characters in {@code text}; a program may well print one. */
    private static String clean(String value) {
        return value == null || value.indexOf('\u0000') < 0 ? value : value.replace("\u0000", "");
    }
}
