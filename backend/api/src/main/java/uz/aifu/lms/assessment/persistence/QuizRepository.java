package uz.aifu.lms.assessment.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.assessment.domain.Quiz;
import uz.aifu.lms.common.domain.PublicationStatus;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    /** {@code course} fetched eagerly — see {@code CourseModuleRepository}'s equivalent note. */
    @EntityGraph(attributePaths = "course")
    Optional<Quiz> findWithCourseById(UUID id);

    @EntityGraph(attributePaths = "course")
    Page<Quiz> findByCourse_Id(UUID courseId, Pageable pageable);

    @EntityGraph(attributePaths = "course")
    Page<Quiz> findByCourse_IdAndStatus(UUID courseId, PublicationStatus status, Pageable pageable);

    /** Backs {@code AssessmentLocatorAdapter.courseIdOfQuiz} — the {@code AccessGuard} port. */
    @Query("select q.course.id from Quiz q where q.id = :id")
    Optional<UUID> findCourseIdById(@Param("id") UUID id);
}
