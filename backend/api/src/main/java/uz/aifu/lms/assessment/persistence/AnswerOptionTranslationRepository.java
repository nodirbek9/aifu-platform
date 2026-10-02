package uz.aifu.lms.assessment.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.assessment.domain.AnswerOptionTranslation;
import uz.aifu.lms.assessment.domain.AnswerOptionTranslationId;

/** Explicit {@code @Query} everywhere — see {@code QuizTranslationRepository}'s javadoc. */
public interface AnswerOptionTranslationRepository
        extends JpaRepository<AnswerOptionTranslation, AnswerOptionTranslationId> {

    @Query("select t from AnswerOptionTranslation t where t.option.id = :optionId")
    List<AnswerOptionTranslation> findAllByOptionId(@Param("optionId") UUID optionId);

    @Query("select t from AnswerOptionTranslation t where t.option.id in :optionIds")
    List<AnswerOptionTranslation> findAllByOptionIds(
            @Param("optionIds") Collection<UUID> optionIds);

    @Query(
            "select t from AnswerOptionTranslation t where t.option.id = :optionId and t.id.locale = :locale")
    Optional<AnswerOptionTranslation> findByOptionIdAndLocale(
            @Param("optionId") UUID optionId, @Param("locale") String locale);
}
