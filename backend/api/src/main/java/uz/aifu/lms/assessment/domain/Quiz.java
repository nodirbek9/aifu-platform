package uz.aifu.lms.assessment.domain;

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

/**
 * A reusable quiz (V009, brief §16). {@code quizzes} has {@code created_by} but no {@code
 * updated_by} — extends {@link VersionedEntity} directly with a matching single field, same pattern
 * as {@code Enrollment} (V006).
 */
@Entity
@Table(name = "quizzes")
@EntityListeners(AuditingEntityListener.class)
public class Quiz extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false, updatable = false)
    private Course course;

    @Column(name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(name = "max_points", nullable = false)
    private BigDecimal maxPoints = BigDecimal.ZERO;

    @Column(name = "passing_percent", nullable = false)
    private BigDecimal passingPercent = BigDecimal.valueOf(60);

    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "question_pool_size")
    private Integer questionPoolSize;

    @Column(name = "shuffle_questions", nullable = false)
    private boolean shuffleQuestions;

    @Column(name = "shuffle_options", nullable = false)
    private boolean shuffleOptions;

    @Enumerated(EnumType.STRING)
    @Column(name = "reveal_policy", nullable = false)
    private RevealPolicy revealPolicy = RevealPolicy.AFTER_LAST_ATTEMPT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    protected Quiz() {}

    public Quiz(Course course, String code) {
        this.course = Guards.required(course, "course");
        this.code = code;
    }

    public void updateDetails(
            String code,
            BigDecimal maxPoints,
            BigDecimal passingPercent,
            Integer timeLimitSeconds,
            Integer maxAttempts,
            Integer questionPoolSize,
            boolean shuffleQuestions,
            boolean shuffleOptions,
            RevealPolicy revealPolicy) {
        this.code = code;
        this.maxPoints = maxPoints == null ? BigDecimal.ZERO : maxPoints;
        this.passingPercent = passingPercent == null ? BigDecimal.valueOf(60) : passingPercent;
        this.timeLimitSeconds = timeLimitSeconds;
        this.maxAttempts = maxAttempts;
        this.questionPoolSize = questionPoolSize;
        this.shuffleQuestions = shuffleQuestions;
        this.shuffleOptions = shuffleOptions;
        this.revealPolicy = revealPolicy == null ? RevealPolicy.AFTER_LAST_ATTEMPT : revealPolicy;
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
                    "Cannot move quiz from " + status + " to " + target);
        }
    }

    public Course getCourse() {
        return course;
    }

    public String getCode() {
        return code;
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public BigDecimal getMaxPoints() {
        return maxPoints;
    }

    public BigDecimal getPassingPercent() {
        return passingPercent;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public Integer getQuestionPoolSize() {
        return questionPoolSize;
    }

    public boolean isShuffleQuestions() {
        return shuffleQuestions;
    }

    public boolean isShuffleOptions() {
        return shuffleOptions;
    }

    public RevealPolicy getRevealPolicy() {
        return revealPolicy;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    /** Recomputed by {@code QuestionService} whenever a question's points change. */
    public void setMaxPoints(BigDecimal maxPoints) {
        this.maxPoints = maxPoints == null ? BigDecimal.ZERO : maxPoints;
    }
}
