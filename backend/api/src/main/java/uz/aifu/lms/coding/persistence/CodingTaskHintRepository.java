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

    /**
     * The position after the last item: positions may have gaps (a deleted item, a reorder), so
     * counting the items would hand out a position already taken.
     */
    @Query(
            "select coalesce(max(x.position), -1) + 1 from CodingTaskHint x where x.codingTask.id = :parentId")
    int nextPosition(@Param("parentId") UUID parentId);
}
