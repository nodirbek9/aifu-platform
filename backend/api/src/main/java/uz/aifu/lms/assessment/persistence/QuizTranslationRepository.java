package uz.aifu.lms.assessment.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.assessment.domain.QuizTranslation;
import uz.aifu.lms.assessment.domain.QuizTranslationId;

/**
 * Explicit {@code @Query} everywhere, never a bare derived method name: Spring Data cannot reliably
 * disambiguate {@code findByQuizId...} between the embedded id's {@code quizId} property and the
 * {@code quiz} association's own {@code id} — matches {@code LessonTranslationRepository}'s
 * precedent exactly.
 */
public interface QuizTranslationRepository
        extends JpaRepository<QuizTranslation, QuizTranslationId> {

    @Query("select t from QuizTranslation t where t.quiz.id = :quizId")
    List<QuizTranslation> findAllByQuizId(@Param("quizId") UUID quizId);

    @Query("select t from QuizTranslation t where t.quiz.id in :quizIds")
    List<QuizTranslation> findAllByQuizIds(@Param("quizIds") Collection<UUID> quizIds);

    @Query("select t from QuizTranslation t where t.quiz.id = :quizId and t.id.locale = :locale")
    Optional<QuizTranslation> findByQuizIdAndLocale(
            @Param("quizId") UUID quizId, @Param("locale") String locale);
}
