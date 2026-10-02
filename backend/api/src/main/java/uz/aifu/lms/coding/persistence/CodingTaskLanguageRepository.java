package uz.aifu.lms.coding.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.coding.domain.CodingTaskLanguage;

public interface CodingTaskLanguageRepository extends JpaRepository<CodingTaskLanguage, UUID> {

    @EntityGraph(attributePaths = "language")
    List<CodingTaskLanguage> findByCodingTask_Id(UUID codingTaskId);

    Optional<CodingTaskLanguage> findByCodingTask_IdAndLanguage_Code(
            UUID codingTaskId, String languageCode);

    /**
     * Backs the controller's pre-mutation ownership check ({@code
     * AccessGuard#requireCodingTaskEditor}) with a plain scalar query — {@code
     * spring.jpa.open-in-view: false} means a lazy {@code codingTaskLanguage.getCodingTask()}
     * touched AFTER the transactional service call returns throws {@code
     * LazyInitializationException} (found by {@code CodingSubmissionIT}, Phase 8).
     */
    @Query("select l.codingTask.id from CodingTaskLanguage l where l.id = :id")
    Optional<UUID> findCodingTaskIdById(@Param("id") UUID id);
}
