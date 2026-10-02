package uz.aifu.lms.coding.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.domain.VersionedEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.curriculum.domain.Difficulty;

/**
 * A coding exercise (V010, brief §17). {@code coding_tasks} has {@code created_by} but no {@code
 * updated_by} — extends {@link VersionedEntity} directly with a matching single field, same pattern
 * as {@code Quiz}/{@code Enrollment}.
 *
 * <p>{@code cpu_time_limit_ms}/{@code wall_time_limit_ms}/{@code memory_limit_kb} may only
 * *tighten* the platform ceiling, never exceed it — {@code execution-worker}'s {@code
 * ExecutionProperties} clamps these at run time (ADR-006); this entity stores the task-author's
 * request, not the enforced value.
 */
@Entity
@Table(name = "coding_tasks")
@EntityListeners(AuditingEntityListener.class)
public class CodingTask extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false, updatable = false)
    private Course course;

    @Column(name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(name = "max_points", nullable = false)
    private BigDecimal maxPoints = BigDecimal.valueOf(100);

    @Enumerated(EnumType.STRING)
    @Column(name = "scoring_strategy", nullable = false)
    private ScoringStrategy scoringStrategy = ScoringStrategy.PER_TEST_WEIGHTED;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "cpu_time_limit_ms")
    private Integer cpuTimeLimitMs;

    @Column(name = "wall_time_limit_ms")
    private Integer wallTimeLimitMs;

    @Column(name = "memory_limit_kb")
    private Integer memoryLimitKb;

    @Column(name = "published_at")
    private Instant publishedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    protected CodingTask() {}

    public CodingTask(Course course, String code) {
        this.course = Guards.required(course, "course");
        this.code = code;
    }

    public void updateDetails(
            Difficulty difficulty,
            BigDecimal maxPoints,
            ScoringStrategy scoringStrategy,
            Integer maxAttempts,
            Integer cpuTimeLimitMs,
            Integer wallTimeLimitMs,
            Integer memoryLimitKb) {
        this.difficulty = difficulty;
        Guards.require(maxPoints != null && maxPoints.signum() > 0, "maxPoints must be > 0");
        this.maxPoints = maxPoints;
        this.scoringStrategy =
                scoringStrategy == null ? ScoringStrategy.PER_TEST_WEIGHTED : scoringStrategy;
        this.maxAttempts = maxAttempts;
        this.cpuTimeLimitMs = cpuTimeLimitMs;
        this.wallTimeLimitMs = wallTimeLimitMs;
        this.memoryLimitKb = memoryLimitKb;
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
                    "Cannot move coding task from " + status + " to " + target);
        }
    }

    public Course getCourse() {
        return course;
    }

    public String getCode() {
        return code;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public BigDecimal getMaxPoints() {
        return maxPoints;
    }

    public ScoringStrategy getScoringStrategy() {
        return scoringStrategy;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public Integer getCpuTimeLimitMs() {
        return cpuTimeLimitMs;
    }

    public Integer getWallTimeLimitMs() {
        return wallTimeLimitMs;
    }

    public Integer getMemoryLimitKb() {
        return memoryLimitKb;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }
}
