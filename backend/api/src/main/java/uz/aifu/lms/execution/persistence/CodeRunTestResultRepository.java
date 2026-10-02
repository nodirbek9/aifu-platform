package uz.aifu.lms.execution.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.execution.domain.CodeRunTestResult;

public interface CodeRunTestResultRepository extends JpaRepository<CodeRunTestResult, UUID> {

    List<CodeRunTestResult> findByRunIdOrderByPosition(UUID runId);

    List<CodeRunTestResult> findByRunIdInOrderByPosition(Collection<UUID> runIds);
}
