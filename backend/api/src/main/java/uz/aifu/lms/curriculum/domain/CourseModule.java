package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.domain.VersionedEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * A grouping of {@link Lesson}s within a {@link Course}. No {@code created_by}/{@code updated_by}
 * (V007) — extends {@link VersionedEntity} directly, unlike {@code Course}'s {@code
 * AuditableEntity}.
 */
@Entity
@Table(name = "course_modules")
public class CourseModule extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false, updatable = false)
    private Course course;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "available_from")
    private Instant availableFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "grouping_source", nullable = false)
    private GroupingSource groupingSource = GroupingSource.PLATFORM_INFERRED;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected CourseModule() {}

    public CourseModule(Course course, int position, String code, GroupingSource groupingSource) {
        this.course = Guards.required(course, "course");
        Guards.require(position >= 0, "position must be >= 0");
        this.position = position;
        this.code = code;
        this.groupingSource =
                groupingSource == null ? GroupingSource.PLATFORM_INFERRED : groupingSource;
    }

    public void updateDetails(
            String code,
            boolean required,
            Integer estimatedMinutes,
            Instant availableFrom,
            GroupingSource groupingSource) {
        this.code = code;
        this.required = required;
        this.estimatedMinutes = estimatedMinutes;
        this.availableFrom = availableFrom;
        this.groupingSource =
                groupingSource == null ? GroupingSource.PLATFORM_INFERRED : groupingSource;
    }

    public void reorderTo(int newPosition) {
        Guards.require(newPosition >= 0, "position must be >= 0");
        this.position = newPosition;
    }

    public void publish(Instant now) {
        requireTransition(PublicationStatus.PUBLISHED);
        this.status = PublicationStatus.PUBLISHED;
        this.publishedAt = now;
    }

    public void archive() {
        requireTransition(PublicationStatus.ARCHIVED);
        this.status = PublicationStatus.ARCHIVED;
    }

    public void returnToDraft() {
        requireTransition(PublicationStatus.DRAFT);
        this.status = PublicationStatus.DRAFT;
    }

    private void requireTransition(PublicationStatus target) {
        if (!status.canTransitionTo(target)) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION,
                    "Cannot move module from " + status + " to " + target);
        }
    }

    public void softDelete(Instant now) {
        this.deletedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Course getCourse() {
        return course;
    }

    public int getPosition() {
        return position;
    }

    public String getCode() {
        return code;
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public boolean isRequired() {
        return required;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public Instant getAvailableFrom() {
        return availableFrom;
    }

    public GroupingSource getGroupingSource() {
        return groupingSource;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
