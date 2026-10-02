package uz.aifu.lms.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/**
 * Read-only mapping of {@code v_lesson_completion} (V015) — "which students completed lesson X".
 */
@Entity
@Immutable
@Table(name = "v_lesson_completion")
public class LessonCompletionView {

    @Id
    @Column(name = "lesson_id")
    private UUID lessonId;

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "tracked_students")
    private long trackedStudents;

    @Column(name = "completed_students")
    private long completedStudents;

    @Column(name = "avg_time_spent_seconds")
    private BigDecimal avgTimeSpentSeconds;

    protected LessonCompletionView() {}

    public UUID getLessonId() {
        return lessonId;
    }

    public UUID getModuleId() {
        return moduleId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public long getTrackedStudents() {
        return trackedStudents;
    }

    public long getCompletedStudents() {
        return completedStudents;
    }

    public BigDecimal getAvgTimeSpentSeconds() {
        return avgTimeSpentSeconds;
    }
}
