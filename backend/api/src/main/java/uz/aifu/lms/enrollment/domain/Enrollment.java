package uz.aifu.lms.enrollment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.common.domain.VersionedEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.iam.domain.User;

/**
 * "The spine of all student-owned data" (docs/domain/domain-model.md §3). One row per (course,
 * student, attempt). Withdrawing then re-enrolling creates a NEW row ({@code attemptNo + 1}) so
 * history is never lost — see {@link EnrollmentStatus}.
 *
 * <p>{@code enrollments} has {@code created_by} but no {@code updated_by} (V006), unlike {@code
 * AuditableEntity}'s pair — extends {@link VersionedEntity} directly with a matching single field
 * rather than force-fitting the two-column base class.
 */
@Entity
@Table(name = "enrollments")
@EntityListeners(AuditingEntityListener.class)
public class Enrollment extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false, updatable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false, updatable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "attempt_no", nullable = false, updatable = false)
    private int attemptNo = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, updatable = false)
    private EnrollmentSource source = EnrollmentSource.ADMIN;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt;

    @Column(name = "starts_on")
    private LocalDate startsOn;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;

    /**
     * Derived by {@code grading}'s FinalScoreCalculator (Phase 9, ADR-011). Never hand-edited here.
     */
    @Column(name = "final_score")
    private BigDecimal finalScore;

    @Column(name = "final_grade_symbol")
    private String finalGradeSymbol;

    @Column(name = "final_grade_at")
    private Instant finalGradeAt;

    @Column(name = "admitted_to_final")
    private Boolean admittedToFinal;

    @Column(name = "academic_debt", nullable = false)
    private boolean academicDebt = false;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    protected Enrollment() {}

    public Enrollment(
            Course course,
            User student,
            int attemptNo,
            EnrollmentSource source,
            Instant enrolledAt) {
        this.course = Guards.required(course, "course");
        this.student = Guards.required(student, "student");
        Guards.require(attemptNo >= 1, "attemptNo must be >= 1");
        this.attemptNo = attemptNo;
        this.source = source == null ? EnrollmentSource.ADMIN : source;
        this.enrolledAt = Guards.required(enrolledAt, "enrolledAt");
        this.status = EnrollmentStatus.ACTIVE;
    }

    // ---------------------------------------------------------------- transitions

    public void activate() {
        requireStatus(EnrollmentStatus.PENDING, EnrollmentStatus.ACTIVE);
        this.status = EnrollmentStatus.ACTIVE;
    }

    public void suspend() {
        requireStatus(EnrollmentStatus.ACTIVE, EnrollmentStatus.SUSPENDED);
        this.status = EnrollmentStatus.SUSPENDED;
    }

    public void resume() {
        requireStatus(EnrollmentStatus.SUSPENDED, EnrollmentStatus.ACTIVE);
        this.status = EnrollmentStatus.ACTIVE;
    }

    public void complete(Instant now) {
        requireStatus(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED);
        this.status = EnrollmentStatus.COMPLETED;
        this.completedAt = Guards.required(now, "now");
    }

    public void fail() {
        requireStatus(EnrollmentStatus.ACTIVE, EnrollmentStatus.FAILED);
        this.status = EnrollmentStatus.FAILED;
    }

    /** Reachable from any non-terminal status: a pending or suspended student may also withdraw. */
    public void withdraw(Instant now) {
        if (status.isTerminal()) {
            throw invalidTransition(EnrollmentStatus.WITHDRAWN);
        }
        this.status = EnrollmentStatus.WITHDRAWN;
        this.withdrawnAt = Guards.required(now, "now");
    }

    /**
     * Admin correction of a mistaken WITHDRAWN/FAILED transition — resurrects THIS row, unlike a
     * fresh {@link #Enrollment(Course, User, int, EnrollmentSource, Instant) re-enrollment}, which
     * creates a new attempt. COMPLETED is never reinstated (ADR-011: implies grade finalization).
     */
    public void reinstate() {
        if (status != EnrollmentStatus.WITHDRAWN && status != EnrollmentStatus.FAILED) {
            throw invalidTransition(EnrollmentStatus.ACTIVE);
        }
        this.status = EnrollmentStatus.ACTIVE;
        this.withdrawnAt = null;
    }

    /**
     * The derived grading snapshot ADR-011 describes — recomputed by {@code
     * grading.app.FinalScoreCalculator} from published {@code grade_entries} whenever they change,
     * never hand-edited directly. Not tied to any {@link EnrollmentStatus} transition: admission
     * and academic-debt status are meaningful mid-course, before the enrollment ever reaches {@link
     * #complete}.
     */
    public void recordFinalGrade(
            BigDecimal finalScore,
            String finalGradeSymbol,
            boolean admittedToFinal,
            boolean academicDebt,
            Instant now) {
        this.finalScore = finalScore;
        this.finalGradeSymbol = finalGradeSymbol;
        this.admittedToFinal = admittedToFinal;
        this.academicDebt = academicDebt;
        this.finalGradeAt = Guards.required(now, "now");
    }

    private void requireStatus(EnrollmentStatus expected, EnrollmentStatus target) {
        if (status != expected) {
            throw invalidTransition(target);
        }
    }

    private uz.aifu.lms.common.error.DomainException invalidTransition(EnrollmentStatus target) {
        return Exceptions.rule(
                        ErrorCode.ENROLLMENT_INVALID_TRANSITION,
                        "Cannot move enrollment from " + status + " to " + target)
                .with("from", status.name())
                .with("to", target.name());
    }

    // ---------------------------------------------------------------- accessors

    public Course getCourse() {
        return course;
    }

    public User getStudent() {
        return student;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public int getAttemptNo() {
        return attemptNo;
    }

    public EnrollmentSource getSource() {
        return source;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getWithdrawnAt() {
        return withdrawnAt;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public String getFinalGradeSymbol() {
        return finalGradeSymbol;
    }

    public Instant getFinalGradeAt() {
        return finalGradeAt;
    }

    public Boolean getAdmittedToFinal() {
        return admittedToFinal;
    }

    public boolean isAcademicDebt() {
        return academicDebt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }
}
