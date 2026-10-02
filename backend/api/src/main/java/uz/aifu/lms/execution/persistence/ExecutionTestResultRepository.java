package uz.aifu.lms.execution.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.execution.domain.ExecutionTestResult;

public interface ExecutionTestResultRepository extends JpaRepository<ExecutionTestResult, UUID> {

    @EntityGraph(attributePaths = "testCase")
    @Query(
            "select r from ExecutionTestResult r where r.executionResult.id = :executionResultId order by r.testCase.position")
    List<ExecutionTestResult> findByExecutionResultIdOrderByTestCasePosition(
            @Param("executionResultId") UUID executionResultId);
}
