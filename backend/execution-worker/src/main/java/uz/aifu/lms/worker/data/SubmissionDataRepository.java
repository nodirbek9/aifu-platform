package uz.aifu.lms.worker.data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.worker.domain.CodingTaskRecord;
import uz.aifu.lms.worker.domain.LanguageRecord;
import uz.aifu.lms.worker.domain.SubmissionContext;
import uz.aifu.lms.worker.domain.SubmissionRecord;
import uz.aifu.lms.worker.domain.TestCaseRecord;

/**
 * Loads everything needed to run and grade one submission — exactly the tables V014 grants {@code
 * SELECT} on: {@code submissions}, {@code coding_tasks}, {@code coding_task_test_cases}, {@code
 * coding_task_languages} + {@code programming_languages}. No enrollment, activity, or user data is
 * ever read here — the restricted role cannot see them anyway.
 */
@Component
public class SubmissionDataRepository {

    private final JdbcTemplate jdbc;

    public SubmissionDataRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Optional<SubmissionContext> load(UUID submissionId) {
        Optional<SubmissionRecord> submission = loadSubmission(submissionId);
        if (submission.isEmpty()) {
            return Optional.empty();
        }
        CodingTaskRecord task = loadTask(submission.get().codingTaskId());
        LanguageRecord language = loadLanguage(submission.get().languageCode());
        List<TestCaseRecord> testCases = loadTestCases(task.id());
        return Optional.of(new SubmissionContext(submission.get(), task, language, testCases));
    }

    private Optional<SubmissionRecord> loadSubmission(UUID submissionId) {
        try {
            return Optional.of(
                    jdbc.queryForObject(
                            """
                            SELECT id, coding_task_id, language_code, source_code, version
                            FROM submissions WHERE id = ?
                            """,
                            (rs, rowNum) ->
                                    new SubmissionRecord(
                                            (UUID) rs.getObject("id"),
                                            (UUID) rs.getObject("coding_task_id"),
                                            rs.getString("language_code"),
                                            rs.getString("source_code"),
                                            rs.getLong("version")),
                            submissionId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private CodingTaskRecord loadTask(UUID codingTaskId) {
        return jdbc.queryForObject(
                """
                SELECT id, scoring_strategy, max_points, cpu_time_limit_ms, wall_time_limit_ms,
                    memory_limit_kb
                FROM coding_tasks WHERE id = ?
                """,
                (rs, rowNum) ->
                        new CodingTaskRecord(
                                (UUID) rs.getObject("id"),
                                rs.getString("scoring_strategy"),
                                rs.getBigDecimal("max_points"),
                                (Integer) rs.getObject("cpu_time_limit_ms"),
                                (Integer) rs.getObject("wall_time_limit_ms"),
                                (Integer) rs.getObject("memory_limit_kb")),
                codingTaskId);
    }

    private LanguageRecord loadLanguage(String code) {
        return jdbc.queryForObject(
                "SELECT code, executor_key, compile_required, source_filename FROM programming_languages WHERE code = ?",
                (rs, rowNum) ->
                        new LanguageRecord(
                                rs.getString("code"),
                                rs.getString("executor_key"),
                                rs.getBoolean("compile_required"),
                                rs.getString("source_filename")),
                code);
    }

    private List<TestCaseRecord> loadTestCases(UUID codingTaskId) {
        return jdbc.query(
                """
                SELECT id, position, stdin, expected_stdout, comparison_mode, numeric_tolerance,
                    weight, cpu_time_limit_ms
                FROM coding_task_test_cases WHERE coding_task_id = ? ORDER BY position
                """,
                (rs, rowNum) ->
                        new TestCaseRecord(
                                (UUID) rs.getObject("id"),
                                rs.getInt("position"),
                                rs.getString("stdin"),
                                rs.getString("expected_stdout"),
                                rs.getString("comparison_mode"),
                                (BigDecimal) rs.getObject("numeric_tolerance"),
                                rs.getBigDecimal("weight"),
                                (Integer) rs.getObject("cpu_time_limit_ms")),
                codingTaskId);
    }
}
