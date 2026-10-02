package uz.aifu.lms.progress.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uz.aifu.lms.common.domain.UpdatableEntity;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.enrollment.domain.Enrollment;

/**
 * The finest grain of progress (V008, {@code uq_lesson_progress}): "answers 'which students
 * completed lesson X' with one indexed query" (table comment). Completion is idempotent by
 * construction — {@link #recordAccess} only moves {@code completedAt}/{@code status} on the FIRST
 * call that asks to complete; a repeat is a no-op for those two fields (view_count still
 * accumulates, since a repeat view is a real view).
 */
@Entity
@Table(name = "lesson_progress")
public class LessonProgress extends UpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false, updatable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "time_spent_seconds", nullable = false)
    private long timeSpentSeconds;

    @Column(name = "first_accessed_at")
    private Instant firstAccessedAt;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected LessonProgress() {}

    public LessonProgress(Enrollment enrollment, Lesson lesson) {
        this.enrollment = Guards.required(enrollment, "enrollment");
        this.lesson = Guards.required(lesson, "lesson");
    }

    public void recordAccess(Instant now, boolean markCompleted, long additionalTimeSpentSeconds) {
        viewCount++;
        if (additionalTimeSpentSeconds > 0) {
            timeSpentSeconds += additionalTimeSpentSeconds;
        }
        if (firstAccessedAt == null) {
            firstAccessedAt = now;
        }
        lastAccessedAt = now;
        if (status == ProgressStatus.NOT_STARTED) {
            status = ProgressStatus.IN_PROGRESS;
        }
        if (markCompleted && status != ProgressStatus.COMPLETED) {
            status = ProgressStatus.COMPLETED;
            completedAt = now;
        }
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public int getViewCount() {
        return viewCount;
    }

    public long getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public Instant getFirstAccessedAt() {
        return firstAccessedAt;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
