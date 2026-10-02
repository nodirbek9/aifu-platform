package uz.aifu.lms.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/** Read-only mapping of {@code v_students_at_risk} (V015) — "which students are behind". */
@Entity
@Immutable
@Table(name = "v_students_at_risk")
public class StudentAtRiskView {

    @Id
    @Column(name = "enrollment_id")
    private UUID enrollmentId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "student_user_id")
    private UUID studentUserId;

    @Column(name = "completion_percent")
    private BigDecimal completionPercent;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    @Column(name = "final_score")
    private BigDecimal finalScore;

    @Column(name = "academic_debt")
    private boolean academicDebt;

    @Column(name = "failed_quiz_attempts")
    private long failedQuizAttempts;

    @Column(name = "unsuccessful_submissions")
    private long unsuccessfulSubmissions;

    protected StudentAtRiskView() {}

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public UUID getStudentUserId() {
        return studentUserId;
    }

    public BigDecimal getCompletionPercent() {
        return completionPercent;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public boolean isAcademicDebt() {
        return academicDebt;
    }

    public long getFailedQuizAttempts() {
        return failedQuizAttempts;
    }

    public long getUnsuccessfulSubmissions() {
        return unsuccessfulSubmissions;
    }
}
