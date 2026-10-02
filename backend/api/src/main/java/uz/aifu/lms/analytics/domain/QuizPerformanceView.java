package uz.aifu.lms.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/** Read-only mapping of {@code v_quiz_performance} (V015) — pass rate and score spread per quiz. */
@Entity
@Immutable
@Table(name = "v_quiz_performance")
public class QuizPerformanceView {

    @Id
    @Column(name = "quiz_id")
    private UUID quizId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "attempts_total")
    private long attemptsTotal;

    @Column(name = "students_attempted")
    private long studentsAttempted;

    @Column(name = "attempts_passed")
    private long attemptsPassed;

    @Column(name = "attempts_failed")
    private long attemptsFailed;

    @Column(name = "avg_percent")
    private BigDecimal avgPercent;

    @Column(name = "min_percent")
    private BigDecimal minPercent;

    @Column(name = "max_percent")
    private BigDecimal maxPercent;

    protected QuizPerformanceView() {}

    public UUID getQuizId() {
        return quizId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public long getAttemptsTotal() {
        return attemptsTotal;
    }

    public long getStudentsAttempted() {
        return studentsAttempted;
    }

    public long getAttemptsPassed() {
        return attemptsPassed;
    }

    public long getAttemptsFailed() {
        return attemptsFailed;
    }

    public BigDecimal getAvgPercent() {
        return avgPercent;
    }

    public BigDecimal getMinPercent() {
        return minPercent;
    }

    public BigDecimal getMaxPercent() {
        return maxPercent;
    }
}
