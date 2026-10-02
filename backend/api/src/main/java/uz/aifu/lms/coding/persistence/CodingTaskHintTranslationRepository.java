package uz.aifu.lms.coding.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.coding.domain.CodingTaskHintTranslation;
import uz.aifu.lms.coding.domain.CodingTaskHintTranslationId;

/** Explicit {@code @Query} everywhere — see {@code CodingTaskTranslationRepository}'s javadoc. */
public interface CodingTaskHintTranslationRepository
        extends JpaRepository<CodingTaskHintTranslation, CodingTaskHintTranslationId> {

    @Query("select t from CodingTaskHintTranslation t where t.hint.id = :hintId")
    List<CodingTaskHintTranslation> findAllByHintId(@Param("hintId") UUID hintId);

    @Query("select t from CodingTaskHintTranslation t where t.hint.id in :hintIds")
    List<CodingTaskHintTranslation> findAllByHintIds(@Param("hintIds") Collection<UUID> hintIds);

    @Query(
            "select t from CodingTaskHintTranslation t where t.hint.id = :hintId and t.id.locale = :locale")
    Optional<CodingTaskHintTranslation> findByHintIdAndLocale(
            @Param("hintId") UUID hintId, @Param("locale") String locale);
}
