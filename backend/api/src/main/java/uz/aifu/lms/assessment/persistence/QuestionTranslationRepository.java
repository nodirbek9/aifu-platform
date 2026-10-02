package uz.aifu.lms.assessment.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.assessment.domain.QuestionTranslation;
import uz.aifu.lms.assessment.domain.QuestionTranslationId;

/** Explicit {@code @Query} everywhere — see {@code QuizTranslationRepository}'s javadoc. */
public interface QuestionTranslationRepository
        extends JpaRepository<QuestionTranslation, QuestionTranslationId> {

    @Query("select t from QuestionTranslation t where t.question.id = :questionId")
    List<QuestionTranslation> findAllByQuestionId(@Param("questionId") UUID questionId);

    @Query("select t from QuestionTranslation t where t.question.id in :questionIds")
    List<QuestionTranslation> findAllByQuestionIds(
            @Param("questionIds") Collection<UUID> questionIds);

    @Query(
            "select t from QuestionTranslation t where t.question.id = :questionId and t.id.locale = :locale")
    Optional<QuestionTranslation> findByQuestionIdAndLocale(
            @Param("questionId") UUID questionId, @Param("locale") String locale);
}
