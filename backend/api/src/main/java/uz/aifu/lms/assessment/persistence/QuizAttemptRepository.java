package uz.aifu.lms.assessment.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.assessment.domain.AttemptStatus;
import uz.aifu.lms.assessment.domain.QuizAttempt;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    /**
     * {@code quiz}/{@code quiz.course}/{@code enrollment}/{@code enrollment.student} fetched
     * eagerly for the single-attempt detail view (student + teacher mappers both need every one of
     * these fields, and this lookup's transaction closes before either mapper runs).
     */
    @EntityGraph(attributePaths = {"quiz", "quiz.course", "enrollment", "enrollment.student"})
    Optional<QuizAttempt> findWithDetailsById(UUID id);

    /** At most one row can ever match ({@code uq_quiz_attempt_open}). */
    Optional<QuizAttempt> findFirstByQuiz_IdAndEnrollment_IdAndStatus(
            UUID quizId, UUID enrollmentId, AttemptStatus status);

    @Query(
            "select coalesce(max(a.attemptNumber), 0) from QuizAttempt a where a.quiz.id = :quizId and a.enrollment.id = :enrollmentId")
    int findMaxAttemptNumber(
            @Param("quizId") UUID quizId, @Param("enrollmentId") UUID enrollmentId);

    /**
     * True when the student already passed the quiz or still has a usable attempt (open and not
     * timed out, or waiting for grading). Same rule as the MIDTERM_ATTEMPTS_EXHAUSTED problem.
     */
    @Query(
            """
            select count(a) > 0 from QuizAttempt a
            where a.quiz.id = :quizId and a.enrollment.id = :enrollmentId
              and ((a.status = uz.aifu.lms.assessment.domain.AttemptStatus.GRADED and a.passed = true)
                or a.status = uz.aifu.lms.assessment.domain.AttemptStatus.SUBMITTED
                or (a.status = uz.aifu.lms.assessment.domain.AttemptStatus.IN_PROGRESS
                    and (a.expiresAt is null or a.expiresAt > :now)))
            """)
    boolean existsPassedOrUsable(
            @Param("quizId") UUID quizId,
            @Param("enrollmentId") UUID enrollmentId,
            @Param("now") Instant now);

    /** Backs {@code /quizzes/{id}/attempts/me}. */
    @EntityGraph(attributePaths = "quiz")
    Page<QuizAttempt> findByEnrollment_IdAndQuiz_IdOrderByAttemptNumberDesc(
            UUID enrollmentId, UUID quizId, Pageable pageable);

    /** Attempts of one quiz still open although their time has run out. */
    List<QuizAttempt> findByQuiz_IdAndStatusAndExpiresAtBefore(
            UUID quizId, AttemptStatus status, Instant moment);

    /** Backs the teacher-facing attempt roster for one quiz. */
    @EntityGraph(attributePaths = {"enrollment", "enrollment.student"})
    Page<QuizAttempt> findByQuiz_Id(UUID quizId, Pageable pageable);
}
