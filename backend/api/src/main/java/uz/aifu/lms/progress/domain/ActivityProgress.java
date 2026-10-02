package uz.aifu.lms.progress.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import uz.aifu.lms.common.domain.UpdatableEntity;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.enrollment.domain.Enrollment;

/**
 * Per-activity progress (V008, {@code uq_activity_progress}). Written by {@link #recordAttempt}
 * from {@code progress.app.ProgressService}'s {@code QuizAttemptGradedEvent} listener (Phase 7,
 * ADR-014) — {@code assessment} cannot call here directly (module map: {@code progress ->
 * assessment}, not the reverse), so it publishes an event instead and this module reacts to it.
 * {@code coding} (Phase 8) reaches this the other way: {@code execution-worker} is a separate
 * process with no shared event bus, so {@code ProgressService} polls terminal submissions instead
 * of listening for one (V016, ADR-007's scheduled "progress recompute").
 */
@Entity
@Table(name = "activity_progress")
public class ActivityProgress extends UpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false, updatable = false)
    private Activity activity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ActivityProgressStatus status = ActivityProgressStatus.NOT_STARTED;

    @Column(name = "attempts_used", nullable = false)
    private int attemptsUsed;

    @Column(name = "best_score")
    private BigDecimal bestScore;

    @Column(name = "last_score")
    private BigDecimal lastScore;

    @Column(name = "assigned_variant_no")
    private Integer assignedVariantNo;

    @Column(name = "first_attempt_at")
    private Instant firstAttemptAt;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ActivityProgress() {}

    public ActivityProgress(Enrollment enrollment, Activity activity) {
        this.enrollment = Guards.required(enrollment, "enrollment");
        this.activity = Guards.required(activity, "activity");
    }

    /**
     * One graded attempt is recorded. {@code COMPLETED} is sticky — once achieved it is never
     * regressed by a later failing attempt (a student who already passed stays passed); a
     * not-yet-passing result short of that leaves status at {@code SUBMITTED} rather than a
     * terminal {@code FAILED}, since attempts may still remain.
     */
    public void recordAttempt(BigDecimal percentScore, boolean passed, Instant now) {
        attemptsUsed++;
        lastScore = percentScore;
        bestScore = bestScore == null ? percentScore : bestScore.max(percentScore);
        if (firstAttemptAt == null) {
            firstAttemptAt = now;
        }
        lastAttemptAt = now;
        if (passed && status != ActivityProgressStatus.COMPLETED) {
            status = ActivityProgressStatus.COMPLETED;
            completedAt = now;
        } else if (!passed && status != ActivityProgressStatus.COMPLETED) {
            status = ActivityProgressStatus.SUBMITTED;
        }
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public Activity getActivity() {
        return activity;
    }

    public ActivityProgressStatus getStatus() {
        return status;
    }

    public int getAttemptsUsed() {
        return attemptsUsed;
    }

    public BigDecimal getBestScore() {
        return bestScore;
    }

    public BigDecimal getLastScore() {
        return lastScore;
    }

    public Integer getAssignedVariantNo() {
        return assignedVariantNo;
    }

    public Instant getFirstAttemptAt() {
        return firstAttemptAt;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
