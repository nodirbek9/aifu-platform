package uz.aifu.lms.coding.persistence;

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
import uz.aifu.lms.coding.domain.Submission;
import uz.aifu.lms.coding.domain.SubmissionStatus;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    @EntityGraph(
            attributePaths = {
                "codingTask",
                "codingTask.course",
                "enrollment",
                "enrollment.student",
                "language"
            })
    Optional<Submission> findWithDetailsById(UUID id);

    @Query(
            "select coalesce(max(s.attemptNumber), 0) from Submission s where s.codingTask.id = :codingTaskId and s.enrollment.id = :enrollmentId")
    int findMaxAttemptNumber(
            @Param("codingTaskId") UUID codingTaskId, @Param("enrollmentId") UUID enrollmentId);

    Optional<Submission> findByEnrollment_IdAndIdempotencyKey(
            UUID enrollmentId, String idempotencyKey);

    long countByEnrollment_IdAndStatusIn(UUID enrollmentId, Collection<SubmissionStatus> statuses);

    /** Backs {@code /coding-tasks/{id}/submissions/me}. */
    @EntityGraph(attributePaths = "language")
    Page<Submission> findByEnrollment_IdAndCodingTask_IdOrderByAttemptNumberDesc(
            UUID enrollmentId, UUID codingTaskId, Pageable pageable);

    /** Backs the teacher-facing submission roster for one task. */
    @EntityGraph(attributePaths = {"enrollment", "enrollment.student", "language"})
    Page<Submission> findByCodingTask_Id(UUID codingTaskId, Pageable pageable);

    /**
     * Backs {@code progress.app.ProgressService}'s scheduled sweeper (V016) — terminal submissions
     * tied to a gradable activity that the sweeper has not yet processed.
     */
    @EntityGraph(attributePaths = {"enrollment", "activity"})
    List<Submission> findByStatusInAndActivityIsNotNullAndProgressSyncedAtIsNull(
            Collection<SubmissionStatus> statuses, Pageable pageable);

    /**
     * Backs {@code grading.app.GradeEntryService}'s scheduled sweeper (V017) — same shape as {@link
     * #findByStatusInAndActivityIsNotNullAndProgressSyncedAtIsNull}, independent marker column
     * since grading and progress are independent consumers.
     */
    @EntityGraph(attributePaths = {"enrollment", "activity"})
    List<Submission> findByStatusInAndActivityIsNotNullAndGradeSyncedAtIsNull(
            Collection<SubmissionStatus> statuses, Pageable pageable);
}
