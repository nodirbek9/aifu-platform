package uz.aifu.lms.coding.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.coding.domain.CodingTaskTranslation;
import uz.aifu.lms.coding.domain.CodingTaskTranslationId;

/**
 * Explicit {@code @Query} everywhere — see {@code
 * assessment.persistence.QuizTranslationRepository}'s javadoc (Phase 7 defect #3).
 */
public interface CodingTaskTranslationRepository
        extends JpaRepository<CodingTaskTranslation, CodingTaskTranslationId> {

    @Query("select t from CodingTaskTranslation t where t.codingTask.id = :codingTaskId")
    List<CodingTaskTranslation> findAllByCodingTaskId(@Param("codingTaskId") UUID codingTaskId);

    @Query("select t from CodingTaskTranslation t where t.codingTask.id in :codingTaskIds")
    List<CodingTaskTranslation> findAllByCodingTaskIds(
            @Param("codingTaskIds") Collection<UUID> codingTaskIds);

    @Query(
            "select t from CodingTaskTranslation t where t.codingTask.id = :codingTaskId and t.id.locale = :locale")
    Optional<CodingTaskTranslation> findByCodingTaskIdAndLocale(
            @Param("codingTaskId") UUID codingTaskId, @Param("locale") String locale);
}
