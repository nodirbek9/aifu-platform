package uz.aifu.lms.grading.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.grading.domain.GradeEntry;
import uz.aifu.lms.grading.domain.GradeEntryStatus;
import uz.aifu.lms.grading.domain.GradeSourceRefType;

public interface GradeEntryRepository extends JpaRepository<GradeEntry, UUID> {

    /**
     * Backs the student/teacher "current gradebook" read — {@code v_current_grade_entries}'s own
     * filter (status = PUBLISHED), expressed against the entity since JPA cannot map a plain view
     * without its own id column here.
     */
    List<GradeEntry> findByEnrollment_IdAndStatusOrderByEvaluatedAtDesc(
            UUID enrollmentId, GradeEntryStatus status);

    /** Every entry of an enrollment in the given states, newest first — the grade history. */
    List<GradeEntry> findByEnrollment_IdAndStatusInOrderByEvaluatedAtDesc(
            UUID enrollmentId, Collection<GradeEntryStatus> statuses);

    /** Backs the teacher-facing course roster — join through {@code enrollment.course}. */
    @Query(
            "select g from GradeEntry g where g.enrollment.course.id = :courseId and g.status = :status")
    Page<GradeEntry> findByCourseIdAndStatus(
            @Param("courseId") UUID courseId,
            @Param("status") GradeEntryStatus status,
            Pageable pageable);

    /**
     * The one current entry for (enrollment, component, source ref) — mirrors {@code
     * uq_grade_entry_current}'s own partial-unique-index shape exactly, so this query and the DB
     * constraint can never disagree about what "current" means.
     */
    Optional<GradeEntry>
            findByEnrollment_IdAndAssessmentComponentIdAndSourceRefTypeAndSourceRefIdAndStatus(
                    UUID enrollmentId,
                    UUID assessmentComponentId,
                    GradeSourceRefType sourceRefType,
                    UUID sourceRefId,
                    GradeEntryStatus status);
}
