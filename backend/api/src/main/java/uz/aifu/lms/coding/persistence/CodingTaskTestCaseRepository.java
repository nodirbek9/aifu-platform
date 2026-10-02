package uz.aifu.lms.coding.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.coding.domain.CodingTaskTestCase;

/**
 * Unpaginated on purpose — test cases are always shown embedded in one task's own detail view,
 * matching {@code ContentBlockRepository}'s precedent (CLAUDE.md §9).
 */
public interface CodingTaskTestCaseRepository extends JpaRepository<CodingTaskTestCase, UUID> {

    List<CodingTaskTestCase> findByCodingTask_IdOrderByPosition(UUID codingTaskId);

    long countByCodingTask_Id(UUID codingTaskId);

    /**
     * Backs the controller's pre-mutation ownership check — see {@code
     * CodingTaskLanguageRepository#findCodingTaskIdById}'s javadoc for why this must be a plain
     * scalar query rather than a lazy field touched after the transactional call returns.
     */
    @Query("select t.codingTask.id from CodingTaskTestCase t where t.id = :id")
    Optional<UUID> findCodingTaskIdById(@Param("id") UUID id);
}
