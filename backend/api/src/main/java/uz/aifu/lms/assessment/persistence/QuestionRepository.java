package uz.aifu.lms.assessment.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.assessment.domain.Question;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    /**
     * {@code quiz} fetched eagerly: {@code QuestionService.require} is used inside {@code
     * AnswerOptionService}/{@code AttemptService}, whose mappers read {@code question.getQuiz()}
     * after this lookup's own transaction may have closed — same EntityGraph discipline as {@code
     * CourseModuleRepository}.
     */
    @EntityGraph(attributePaths = "quiz")
    Optional<Question> findWithQuizById(UUID id);

    /** Unpaginated on purpose — internal use (reorder validation, attempt serving). */
    List<Question> findByQuiz_IdOrderByPosition(UUID quizId);

    Page<Question> findByQuiz_Id(UUID quizId, Pageable pageable);

    long countByQuiz_Id(UUID quizId);
}
