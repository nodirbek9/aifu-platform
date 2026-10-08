package uz.aifu.lms.assessment.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.assessment.domain.QuizAttemptGrant;

public interface QuizAttemptGrantRepository extends JpaRepository<QuizAttemptGrant, UUID> {

    long countByQuiz_IdAndEnrollment_Id(UUID quizId, UUID enrollmentId);

    List<QuizAttemptGrant> findByQuiz_IdAndEnrollment_Id(UUID quizId, UUID enrollmentId);

    @EntityGraph(attributePaths = {"grantedBy", "enrollment", "enrollment.student"})
    Page<QuizAttemptGrant> findByQuiz_Id(UUID quizId, Pageable pageable);

    @EntityGraph(attributePaths = {"grantedBy", "enrollment", "enrollment.student"})
    Page<QuizAttemptGrant> findByQuiz_Course_Id(UUID courseId, Pageable pageable);

    @EntityGraph(attributePaths = {"grantedBy", "enrollment", "enrollment.student"})
    Page<QuizAttemptGrant> findAll(Pageable pageable);
}
