package uz.aifu.lms.execution.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.execution.domain.ExecutionResult;

public interface ExecutionResultRepository extends JpaRepository<ExecutionResult, UUID> {

    Optional<ExecutionResult> findBySubmission_Id(UUID submissionId);
}
