package uz.aifu.lms.assessment.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.assessment.domain.QuizAttemptQuestion;

public interface QuizAttemptQuestionRepository extends JpaRepository<QuizAttemptQuestion, UUID> {

    /** {@code question} fetched eagerly — the grader dispatch and every mapper need it. */
    @EntityGraph(attributePaths = "question")
    List<QuizAttemptQuestion> findByAttempt_IdOrderByServedPosition(UUID attemptId);

    @EntityGraph(attributePaths = "question")
    Optional<QuizAttemptQuestion> findByAttempt_IdAndQuestion_Id(UUID attemptId, UUID questionId);

    public interface ServedQuestionHistory {
        UUID getQuestionId();

        Instant getLastServedAt();
    }

    @Query(
            """
            select qaq.question.id as questionId, max(a.startedAt) as lastServedAt
            from QuizAttemptQuestion qaq
            join qaq.attempt a
            where a.quiz.id = :quizId and a.enrollment.id = :enrollmentId
            group by qaq.question.id
            """)
    List<ServedQuestionHistory> servedHistory(
            @Param("quizId") UUID quizId, @Param("enrollmentId") UUID enrollmentId);
}
