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
import java.math.RoundingMode;
import java.time.Instant;
import uz.aifu.lms.common.domain.UpdatableEntity;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.enrollment.domain.Enrollment;

/**
 * Module-level roll-up (V008, {@code uq_module_progress}). Unlike {@link CourseProgress}, the
 * schema keeps only {@code status}/{@code completion_percent} here — no raw lesson counts — since
 * {@link LessonProgress} is the granular source of truth this rolls up from; recomputed fresh on
 * every write, never incremented.
 */
@Entity
@Table(name = "module_progress")
public class ModuleProgress extends UpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false, updatable = false)
    private CourseModule module;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(name = "completion_percent", nullable = false)
    private BigDecimal completionPercent = BigDecimal.ZERO;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ModuleProgress() {}

    public ModuleProgress(Enrollment enrollment, CourseModule module) {
        this.enrollment = Guards.required(enrollment, "enrollment");
        this.module = Guards.required(module, "module");
    }

    public void recordAccess(Instant now) {
        if (startedAt == null) {
            startedAt = now;
        }
        lastAccessedAt = now;
        if (status == ProgressStatus.NOT_STARTED) {
            status = ProgressStatus.IN_PROGRESS;
        }
    }

    /**
     * Self-sufficient regardless of whether {@link #recordAccess} ran first: a partial completion
     * always reaches at least {@code IN_PROGRESS} here, not only via {@code recordAccess}'s own
     * NOT_STARTED-to-IN_PROGRESS bump.
     */
    public void applyRollup(int lessonsTotal, int lessonsCompleted, Instant now) {
        this.completionPercent = percentOf(lessonsCompleted, lessonsTotal);
        boolean allRequiredLessonsDone = lessonsTotal > 0 && lessonsCompleted >= lessonsTotal;
        if (allRequiredLessonsDone) {
            if (status != ProgressStatus.COMPLETED) {
                status = ProgressStatus.COMPLETED;
                completedAt = now;
            }
        } else {
            if (status == ProgressStatus.COMPLETED) {
                completedAt = null;
            }
            if (lessonsCompleted > 0) {
                status = ProgressStatus.IN_PROGRESS;
            } else if (status == ProgressStatus.COMPLETED) {
                status = ProgressStatus.NOT_STARTED;
            }
        }
    }

    private static BigDecimal percentOf(int completed, int total) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public CourseModule getModule() {
        return module;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public BigDecimal getCompletionPercent() {
        return completionPercent;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
