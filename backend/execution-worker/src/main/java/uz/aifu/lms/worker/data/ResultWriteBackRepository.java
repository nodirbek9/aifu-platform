package uz.aifu.lms.worker.data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.worker.grading.ScoringResult;
import uz.aifu.lms.worker.grading.TestOutcome;
import uz.aifu.lms.worker.sandbox.SandboxRunResult;

/**
 * Writes a graded submission back through the restricted role's exact column-level grant (V014):
 * full {@code SELECT/INSERT/UPDATE/DELETE} on {@code execution_results}/{@code
 * execution_test_results}, but only a narrow column list on {@code submissions} — {@code
 * source_code}, {@code enrollment_id} and {@code attempt_number} are physically ungrantable to this
 * role, so there is no code path here that could touch them even by mistake.
 */
@Component
public class ResultWriteBackRepository {

    private final JdbcTemplate jdbc;
    private final Clock clock;

    public ResultWriteBackRepository(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Transactional
    public void writeCompileFailure(
            UUID submissionId,
            UUID jobId,
            String engine,
            String sandboxProfile,
            SandboxRunResult compileResult,
            BigDecimal maxScore,
            long expectedVersion) {
        upsertExecutionResult(
                submissionId,
                jobId,
                engine,
                sandboxProfile,
                false,
                compileResult.compileOutput(),
                null,
                null,
                null,
                false,
                null);
        updateSubmission(
                submissionId,
                "COMPLETED",
                "COMPILATION_ERROR",
                BigDecimal.ZERO,
                maxScore,
                0,
                0,
                expectedVersion);
    }

    @Transactional
    public void writeGraded(
            UUID submissionId,
            UUID jobId,
            String engine,
            String sandboxProfile,
            List<TestOutcome> outcomes,
            ScoringResult scoring,
            long expectedVersion) {
        UUID resultId =
                upsertExecutionResult(
                        submissionId,
                        jobId,
                        engine,
                        sandboxProfile,
                        true,
                        null,
                        0,
                        null,
                        null,
                        false,
                        null);
        jdbc.update("DELETE FROM execution_test_results WHERE execution_result_id = ?", resultId);
        Map<UUID, BigDecimal> points = scoring.pointsPerTest();
        for (TestOutcome outcome : outcomes) {
            jdbc.update(
                    """
                    INSERT INTO execution_test_results
                        (execution_result_id, test_case_id, passed, verdict, time_ms, memory_kb, exit_code,
                         stdout_excerpt, stderr_excerpt, points_awarded)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    resultId,
                    outcome.testCaseId(),
                    outcome.passed(),
                    outcome.verdict(),
                    outcome.timeMs(),
                    outcome.memoryKb(),
                    outcome.exitCode(),
                    outcome.stdoutExcerpt(),
                    outcome.stderrExcerpt(),
                    points.getOrDefault(outcome.testCaseId(), BigDecimal.ZERO));
        }
        updateSubmission(
                submissionId,
                "COMPLETED",
                scoring.verdict(),
                scoring.score(),
                scoring.maxScore(),
                scoring.testsPassed(),
                scoring.testsTotal(),
                expectedVersion);
    }

    /**
     * An internal sandbox failure (not the student's fault) — recorded, submission marked FAILED.
     */
    @Transactional
    public void writeInternalFailure(
            UUID submissionId,
            UUID jobId,
            String engine,
            String sandboxProfile,
            String internalError,
            long expectedVersion) {
        upsertExecutionResult(
                submissionId,
                jobId,
                engine,
                sandboxProfile,
                true,
                null,
                null,
                null,
                null,
                false,
                internalError);
        updateSubmission(
                submissionId, "FAILED", "INTERNAL_ERROR", null, null, null, null, expectedVersion);
    }

    private UUID upsertExecutionResult(
            UUID submissionId,
            UUID jobId,
            String engine,
            String sandboxProfile,
            Boolean compileSuccess,
            String compileOutput,
            Integer exitCode,
            Integer totalTimeMs,
            Integer maxMemoryKb,
            boolean outputTruncated,
            String internalError) {
        return jdbc.queryForObject(
                """
                INSERT INTO execution_results
                    (id, submission_id, job_id, engine, sandbox_profile, compile_success, compile_output,
                     exit_code, total_time_ms, max_memory_kb, output_truncated, internal_error, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (submission_id) DO UPDATE SET
                    job_id = EXCLUDED.job_id, engine = EXCLUDED.engine,
                    sandbox_profile = EXCLUDED.sandbox_profile, compile_success = EXCLUDED.compile_success,
                    compile_output = EXCLUDED.compile_output, exit_code = EXCLUDED.exit_code,
                    total_time_ms = EXCLUDED.total_time_ms, max_memory_kb = EXCLUDED.max_memory_kb,
                    output_truncated = EXCLUDED.output_truncated, internal_error = EXCLUDED.internal_error
                RETURNING id
                """,
                UUID.class,
                UUID.randomUUID(),
                submissionId,
                jobId,
                engine,
                sandboxProfile,
                compileSuccess,
                compileOutput,
                exitCode,
                totalTimeMs,
                maxMemoryKb,
                outputTruncated,
                internalError,
                Timestamp.from(clock.instant()));
    }

    private void updateSubmission(
            UUID submissionId,
            String status,
            String verdict,
            BigDecimal score,
            BigDecimal maxScore,
            Integer testsPassed,
            Integer testsTotal,
            long expectedVersion) {
        int updated =
                jdbc.update(
                        """
                        UPDATE submissions SET status = ?, verdict = ?, score = ?, max_score = ?,
                            tests_passed = ?, tests_total = ?, completed_at = ?, version = version + 1
                        WHERE id = ? AND version = ?
                        """,
                        status,
                        verdict,
                        score,
                        maxScore,
                        testsPassed,
                        testsTotal,
                        Timestamp.from(clock.instant()),
                        submissionId,
                        expectedVersion);
        if (updated == 0) {
            throw new OptimisticLockingFailureException(
                    "Submission "
                            + submissionId
                            + " version changed since it was loaded (expected "
                            + expectedVersion
                            + ")");
        }
    }
}
