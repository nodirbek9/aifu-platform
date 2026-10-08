package uz.aifu.lms.progress.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.domain.Persistable;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.iam.domain.User;

/**
 * Per-course configuration for the server-side learning path gate (Phase 5, ADR-015, ADR-018).
 * Keyed by {@code course_id} (derived identity via {@link MapsId}).
 */
@Entity
@Table(name = "course_learning_paths")
public class CourseLearningPath implements Persistable<UUID> {

    @Id
    @Column(name = "course_id")
    private UUID courseId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "course_id", nullable = false, updatable = false)
    private Course course;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @Column(name = "enabled_at")
    private Instant enabledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enabled_by")
    private User enabledBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "midterm_activity_id")
    private Activity midtermActivity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "midterm_after_lesson_id")
    private Lesson midtermAfterLesson;

    @Column(name = "midterm_opens_at")
    private Instant midtermOpensAt;

    @Column(name = "min_reading_fraction", nullable = false)
    private BigDecimal minReadingFraction = new BigDecimal("0.50");

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    /** Spring Data calls persist (not merge) for a row that was never saved. */
    @Transient private boolean isNewRow = false;

    protected CourseLearningPath() {}

    public CourseLearningPath(Course course) {
        this.course = Guards.required(course, "course");
        this.courseId = course.getId();
        this.isNewRow = true;
    }

    @Override
    public UUID getId() {
        return courseId;
    }

    @Override
    public boolean isNew() {
        return isNewRow;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNewRow = false;
    }

    public CourseLearningPath(Course course, BigDecimal minReadingFraction) {
        this.course = Guards.required(course, "course");
        this.courseId = course.getId();
        if (minReadingFraction != null) {
            this.minReadingFraction = minReadingFraction;
        }
    }

    public void enable(User enabledBy, Instant now) {
        this.enabled = true;
        this.enabledBy = Guards.required(enabledBy, "enabledBy");
        if (this.enabledAt == null) {
            this.enabledAt = Guards.required(now, "now");
        }
    }

    public void disable() {
        this.enabled = false;
    }

    public void configureMidterm(
            Activity midtermActivity, Lesson midtermAfterLesson, Instant midtermOpensAt) {
        if ((midtermActivity == null && midtermAfterLesson != null)
                || (midtermActivity != null && midtermAfterLesson == null)) {
            throw new IllegalArgumentException(
                    "midtermActivity and midtermAfterLesson must both be set or both null");
        }
        this.midtermActivity = midtermActivity;
        this.midtermAfterLesson = midtermAfterLesson;
        this.midtermOpensAt = midtermOpensAt;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public Course getCourse() {
        return course;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getEnabledAt() {
        return enabledAt;
    }

    public User getEnabledBy() {
        return enabledBy;
    }

    public Activity getMidtermActivity() {
        return midtermActivity;
    }

    public Lesson getMidtermAfterLesson() {
        return midtermAfterLesson;
    }

    public Instant getMidtermOpensAt() {
        return midtermOpensAt;
    }

    public BigDecimal getMinReadingFraction() {
        return minReadingFraction;
    }

    public void setMinReadingFraction(BigDecimal minReadingFraction) {
        this.minReadingFraction = Guards.required(minReadingFraction, "minReadingFraction");
    }

    public Instant getCreatedAt() {
        return createdAt;
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
        UUID otherId = ((CourseLearningPath) other).getCourseId();
        return courseId != null && courseId.equals(otherId);
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{courseId=" + courseId + ", enabled=" + enabled + "}";
    }
}
