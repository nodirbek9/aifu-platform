package uz.aifu.lms.assessment.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.assessment.domain.QuizAttemptQuestion;

public interface QuizAttemptQuestionRepository extends JpaRepository<QuizAttemptQuestion, UUID> {

    /** {@code question} fetched eagerly — the grader dispatch and every mapper need it. */
    @EntityGraph(attributePaths = "question")
    List<QuizAttemptQuestion> findByAttempt_IdOrderByServedPosition(UUID attemptId);

    @EntityGraph(attributePaths = "question")
    Optional<QuizAttemptQuestion> findByAttempt_IdAndQuestion_Id(UUID attemptId, UUID questionId);
}
