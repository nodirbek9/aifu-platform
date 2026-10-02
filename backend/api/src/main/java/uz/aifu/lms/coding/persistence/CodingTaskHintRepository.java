package uz.aifu.lms.coding.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.coding.domain.CodingTaskHint;

public interface CodingTaskHintRepository extends JpaRepository<CodingTaskHint, UUID> {

    List<CodingTaskHint> findByCodingTask_IdOrderByPosition(UUID codingTaskId);

    long countByCodingTask_Id(UUID codingTaskId);

    /**
     * Backs the controller's pre-mutation ownership check — see {@code
     * CodingTaskLanguageRepository#findCodingTaskIdById}'s javadoc for why this must be a plain
     * scalar query rather than a lazy field touched after the transactional call returns.
     */
    @Query("select h.codingTask.id from CodingTaskHint h where h.id = :id")
    Optional<UUID> findCodingTaskIdById(@Param("id") UUID id);
}
