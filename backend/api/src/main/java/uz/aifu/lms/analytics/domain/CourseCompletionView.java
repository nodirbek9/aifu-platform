package uz.aifu.lms.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/**
 * Read-only mapping of {@code v_course_completion} (V015). One row per course — "how many students
 * completed the course" (brief §26). Never written from Java; the view IS the query.
 */
@Entity
@Immutable
@Table(name = "v_course_completion")
public class CourseCompletionView {

    @Id
    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "enrolled_total")
    private long enrolledTotal;

    @Column(name = "completed_total")
    private long completedTotal;

    @Column(name = "in_progress_total")
    private long inProgressTotal;

    @Column(name = "not_started_total")
    private long notStartedTotal;

    @Column(name = "avg_completion_percent")
    private BigDecimal avgCompletionPercent;

    @Column(name = "avg_final_score")
    private BigDecimal avgFinalScore;

    protected CourseCompletionView() {}

    public UUID getCourseId() {
        return courseId;
    }

    public long getEnrolledTotal() {
        return enrolledTotal;
    }

    public long getCompletedTotal() {
        return completedTotal;
    }

    public long getInProgressTotal() {
        return inProgressTotal;
    }

    public long getNotStartedTotal() {
        return notStartedTotal;
    }

    public BigDecimal getAvgCompletionPercent() {
        return avgCompletionPercent;
    }

    public BigDecimal getAvgFinalScore() {
        return avgFinalScore;
    }
}
