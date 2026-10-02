package uz.aifu.lms.execution.app;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.execution.domain.ExecutionResult;
import uz.aifu.lms.execution.domain.ExecutionTestResult;
import uz.aifu.lms.execution.persistence.ExecutionResultRepository;
import uz.aifu.lms.execution.persistence.ExecutionTestResultRepository;

/**
 * Read-only access to a submission's sandbox result and per-test outcomes, both written by {@code
 * execution-worker} via the restricted DB role (V014). Controllers never inject a repository
 * directly (skills/architecture/SKILL.md rule 6) — this is the one place {@link
 * ExecutionResultRepository}/{@link ExecutionTestResultRepository} are queried from.
 */
@Service
public class ExecutionResultQueryService {

    private final ExecutionResultRepository results;
    private final ExecutionTestResultRepository testResults;

    public ExecutionResultQueryService(
            ExecutionResultRepository results, ExecutionTestResultRepository testResults) {
        this.results = results;
        this.testResults = testResults;
    }

    @Transactional(readOnly = true)
    public Optional<ExecutionResult> findResult(UUID submissionId) {
        return results.findBySubmission_Id(submissionId);
    }

    @Transactional(readOnly = true)
    public List<ExecutionTestResult> listTestResults(UUID executionResultId) {
        return testResults.findByExecutionResultIdOrderByTestCasePosition(executionResultId);
    }
}
