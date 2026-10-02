package uz.aifu.lms.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/**
 * Read-only mapping of {@code v_coding_task_difficulty} (V015) — "which coding tasks are hardest".
 */
@Entity
@Immutable
@Table(name = "v_coding_task_difficulty")
public class CodingTaskDifficultyView {

    @Id
    @Column(name = "coding_task_id")
    private UUID codingTaskId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "submissions_total")
    private long submissionsTotal;

    @Column(name = "students_attempted")
    private long studentsAttempted;

    @Column(name = "accepted_total")
    private long acceptedTotal;

    @Column(name = "acceptance_percent")
    private BigDecimal acceptancePercent;

    @Column(name = "avg_score")
    private BigDecimal avgScore;

    @Column(name = "avg_attempts_used")
    private BigDecimal avgAttemptsUsed;

    protected CodingTaskDifficultyView() {}

    public UUID getCodingTaskId() {
        return codingTaskId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public long getSubmissionsTotal() {
        return submissionsTotal;
    }

    public long getStudentsAttempted() {
        return studentsAttempted;
    }

    public long getAcceptedTotal() {
        return acceptedTotal;
    }

    public BigDecimal getAcceptancePercent() {
        return acceptancePercent;
    }

    public BigDecimal getAvgScore() {
        return avgScore;
    }

    public BigDecimal getAvgAttemptsUsed() {
        return avgAttemptsUsed;
    }
}
