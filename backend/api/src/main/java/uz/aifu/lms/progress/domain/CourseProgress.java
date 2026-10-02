package uz.aifu.lms.progress.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.enrollment.domain.Enrollment;

/**
 * The course-level roll-up (V008). Its primary key IS {@code enrollment_id} — a derived identity,
 * not a separately generated UUID like the other three progress tables — because a course has
 * exactly one progress row per enrollment (not per course+enrollment pair). {@link
 * uz.aifu.lms.common.domain.UpdatableEntity} does not fit for that reason; this entity supplies its
 * own id/version/updatedAt directly.
 *
 * <p>Deliberately NOT the only place completion lives: {@code lessons_total}/{@code
 * lessons_completed} are stored granular counts recomputed from {@code lesson_progress} on every
 * write, not derived-then-discarded (brief §15, CLAUDE.md Phase 6 DoD "no single-percentage-only
 * storage anywhere").
 */
@Entity
@Table(name = "course_progress")
public class CourseProgress {

    @Id
    @Column(name = "enrollment_id")
    private UUID enrollmentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(name = "completion_percent", nullable = false)
    private BigDecimal completionPercent = BigDecimal.ZERO;

    @Column(name = "lessons_total", nullable = false)
    private int lessonsTotal;

    @Column(name = "lessons_completed", nullable = false)
    private int lessonsCompleted;

    @Column(name = "activities_total", nullable = false)
    private int activitiesTotal;

    @Column(name = "activities_completed", nullable = false)
    private int activitiesCompleted;

    @Column(name = "time_spent_seconds", nullable = false)
    private long timeSpentSeconds;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected CourseProgress() {}

    public CourseProgress(Enrollment enrollment) {
        this.enrollment = Guards.required(enrollment, "enrollment");
    }

    /** Bumps {@code last_accessed_at}/{@code started_at}; does not touch the roll-up counts. */
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
     * Recomputed from source data on every lesson-progress write (never incremented), so a repeated
     * completion of the same lesson can never double-count here — the underlying COUNT query is
     * naturally idempotent.
     */
    public void applyRollup(
            int lessonsTotal,
            int lessonsCompleted,
            int activitiesTotal,
            int activitiesCompleted,
            Instant now) {
        this.lessonsTotal = lessonsTotal;
        this.lessonsCompleted = lessonsCompleted;
        this.activitiesTotal = activitiesTotal;
        this.activitiesCompleted = activitiesCompleted;
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

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public BigDecimal getCompletionPercent() {
        return completionPercent;
    }

    public int getLessonsTotal() {
        return lessonsTotal;
    }

    public int getLessonsCompleted() {
        return lessonsCompleted;
    }

    public int getActivitiesTotal() {
        return activitiesTotal;
    }

    public int getActivitiesCompleted() {
        return activitiesCompleted;
    }

    public long getTimeSpentSeconds() {
        return timeSpentSeconds;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        Class<?> thisType = effectiveClass(this);
        Class<?> otherType = effectiveClass(other);
        if (!thisType.equals(otherType)) {
            return false;
        }
        UUID otherId = ((CourseProgress) other).getEnrollmentId();
        return enrollmentId != null && enrollmentId.equals(otherId);
    }

    @Override
    public final int hashCode() {
        return effectiveClass(this).hashCode();
    }

    private static Class<?> effectiveClass(Object candidate) {
        return candidate instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : candidate.getClass();
    }
}
