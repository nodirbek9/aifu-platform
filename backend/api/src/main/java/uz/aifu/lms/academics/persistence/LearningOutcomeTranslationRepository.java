package uz.aifu.lms.academics.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.academics.domain.LearningOutcomeTranslation;
import uz.aifu.lms.academics.domain.LearningOutcomeTranslationId;

public interface LearningOutcomeTranslationRepository
        extends JpaRepository<LearningOutcomeTranslation, LearningOutcomeTranslationId> {

    @Query("select t from LearningOutcomeTranslation t where t.learningOutcome.id in :outcomeIds")
    List<LearningOutcomeTranslation> findAllByLearningOutcomeIds(
            @Param("outcomeIds") java.util.Collection<UUID> outcomeIds);

    @Query(
            "select t from LearningOutcomeTranslation t where t.learningOutcome.id = :outcomeId and t.id.locale = :locale")
    Optional<LearningOutcomeTranslation> findByOutcomeIdAndLocale(
            @Param("outcomeId") UUID outcomeId, @Param("locale") String locale);
}
