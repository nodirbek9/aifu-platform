package uz.aifu.lms.enrollment.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentStatus;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    @EntityGraph(attributePaths = {"course", "course.subject", "student"})
    Optional<Enrollment> findWithDetailsById(UUID id);

    /**
     * At most one row can ever match (the partial unique index {@code uq_enrollment_active}).
     * {@code course} is fetched eagerly for {@code ProgressService}, which reads {@code
     * enrollment.getCourse().getId()} in the same transaction right after this lookup (progress
     * module, Phase 6) — same EntityGraph-discipline reasoning as {@code
     * CourseModuleRepository.findByIdAndDeletedAtIsNull}.
     */
    @EntityGraph(attributePaths = "course")
    Optional<Enrollment> findFirstByCourse_IdAndStudent_IdAndStatusIn(
            UUID courseId, UUID studentId, Collection<EnrollmentStatus> statuses);

    boolean existsByCourse_IdAndStudent_IdAndStatusIn(
            UUID courseId, UUID studentId, Collection<EnrollmentStatus> statuses);

    @EntityGraph(attributePaths = "course")
    Optional<Enrollment> findFirstByCourse_IdAndStudent_IdOrderByAttemptNoDesc(
            UUID courseId, UUID studentId);

    @Query(
            "select coalesce(max(e.attemptNo), 0) from Enrollment e where e.course.id = :courseId and e.student.id = :studentId")
    int findMaxAttemptNo(@Param("courseId") UUID courseId, @Param("studentId") UUID studentId);

    long countByCourse_IdAndStatusIn(UUID courseId, Collection<EnrollmentStatus> statuses);

    List<Enrollment> findByCourse_IdAndStatus(UUID courseId, EnrollmentStatus status);

    boolean existsByIdAndStudent_Id(UUID id, UUID studentId);

    @Query("select e.course.id from Enrollment e where e.id = :id")
    Optional<UUID> findCourseIdById(@Param("id") UUID id);

    /**
     * Backs {@code /teacher/courses/{id}/students} — the caller's ownership of the course is
     * already established by {@code AccessGuard.requireCourseEditor} before this is called. {@code
     * course.subject}/{@code course.term} are still fetched despite the query already filtering by
     * {@code course.id}: the {@code course} association itself stays LAZY unless named in the
     * graph.
     */
    @EntityGraph(attributePaths = {"student", "course.subject", "course.term"})
    Page<Enrollment> findByCourse_Id(UUID courseId, Pageable pageable);

    /** Backs {@code /enrollments/me}. */
    @EntityGraph(attributePaths = {"student", "course.subject", "course.term"})
    Page<Enrollment> findByStudent_Id(UUID studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "course.subject", "course.term"})
    Page<Enrollment> findByCourse_IdAndStatusIn(
            UUID courseId, Collection<EnrollmentStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "course.subject", "course.term"})
    Page<Enrollment> findByStudent_IdAndStatusIn(
            UUID studentId, Collection<EnrollmentStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "course.subject", "course.term"})
    Page<Enrollment> findByCourse_IdAndStudent_Id(UUID courseId, UUID studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "course.subject", "course.term"})
    Page<Enrollment> findByCourse_IdAndStudent_IdAndStatusIn(
            UUID courseId,
            UUID studentId,
            Collection<EnrollmentStatus> statuses,
            Pageable pageable);
}
