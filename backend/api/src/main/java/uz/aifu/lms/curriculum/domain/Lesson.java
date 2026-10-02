package uz.aifu.lms.curriculum.domain;

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
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.common.domain.AuditableEntity;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * One teaching session's worth of content within a {@link CourseModule}. {@code session_type}
 * mirrors {@link SessionType} from {@code academics} (V007) — reused directly rather than
 * duplicated, since the two must always agree.
 *
 * <p>The same topic legitimately appears in two tracks (e.g. Big Data in both a lecture and an
 * independent-study track): lessons are NOT deduplicated by title.
 */
@Entity
@Table(name = "lessons")
public class Lesson extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false, updatable = false)
    private CourseModule module;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Column(name = "allocated_hours")
    private BigDecimal allocatedHours;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(name = "available_from")
    private Instant availableFrom;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Lesson() {}

    public Lesson(CourseModule module, int position, String code, SessionType sessionType) {
        this.module = Guards.required(module, "module");
        Guards.require(position >= 0, "position must be >= 0");
        this.position = position;
        this.code = code;
        this.sessionType = Guards.required(sessionType, "sessionType");
    }

    public void updateDetails(
            String code,
            SessionType sessionType,
            BigDecimal allocatedHours,
            Integer estimatedMinutes,
            Difficulty difficulty,
            boolean required,
            Instant availableFrom) {
        this.code = code;
        this.sessionType = Guards.required(sessionType, "sessionType");
        this.allocatedHours = allocatedHours;
        this.estimatedMinutes = estimatedMinutes;
        this.difficulty = difficulty;
        this.required = required;
        this.availableFrom = availableFrom;
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
                    "Cannot move lesson from " + status + " to " + target);
        }
    }

    public void softDelete(Instant now) {
        this.deletedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public CourseModule getModule() {
        return module;
    }

    public int getPosition() {
        return position;
    }

    public String getCode() {
        return code;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public BigDecimal getAllocatedHours() {
        return allocatedHours;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isRequired() {
        return required;
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public Instant getAvailableFrom() {
        return availableFrom;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
