package uz.aifu.lms.execution.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.execution.domain.ExecutionJob;

public interface ExecutionJobRepository extends JpaRepository<ExecutionJob, UUID> {

    boolean existsBySubmission_Id(UUID submissionId);
}
