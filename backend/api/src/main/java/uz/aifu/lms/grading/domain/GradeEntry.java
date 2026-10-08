package uz.aifu.lms.grading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.enrollment.domain.Enrollment;

/**
 * One append-only gradebook row (V012, ADR-011). A published entry is never {@code UPDATE}d for its
 * score/max/enrollment/component — {@code trg_grade_entry_immutable} enforces that in the database
 * regardless of what this class does; the methods here exist so the application never even attempts
 * the illegal transition, not because the trigger can be trusted alone (defense in depth, CLAUDE.md
 * §8).
 *
 * <p>{@link #assessmentComponentId}/{@link #activityId}/{@link #evaluatorUserId}/{@link
 * #sourceRefId}/{@link #supersedesId} are deliberately bare {@code UUID} columns, not
 * {@code @ManyToOne} associations — {@code academics}/{@code curriculum}/{@code iam} types have no
 * business appearing in this module's domain layer (only {@code grading.app} needs them, to run
 * {@code FinalScoreCalculator}), and this history must stay readable even if a referenced
 * assessment component or user account is later modified elsewhere. Only {@link #enrollment} is a
 * real association — same module, and every read of this table is enrollment-scoped anyway.
 *
 * <p>No {@link uz.aifu.lms.common.domain.BaseEntity}/{@code VersionedEntity}: the table has {@code
 * created_at} but no {@code updated_at} and no {@code version} — the DB trigger, not optimistic
 * locking, is what guards the one legal transition (DRAFT/PUBLISHED → REVISED/VOIDED) this entity
 * ever performs via {@code UPDATE}.
 */
@Entity
@Table(name = "grade_entries")
public class GradeEntry {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private Enrollment enrollment;

    @Column(name = "assessment_component_id", updatable = false)
    private UUID assessmentComponentId;

    @Column(name = "activity_id", updatable = false)
    private UUID activityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, updatable = false)
    private GradeSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_ref_type", updatable = false)
    private GradeSourceRefType sourceRefType;

    @Column(name = "source_ref_id", updatable = false)
    private UUID sourceRefId;

    @Column(name = "score", nullable = false, updatable = false)
    private BigDecimal score;

    @Column(name = "max_score", nullable = false, updatable = false)
    private BigDecimal maxScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GradeEntryStatus status = GradeEntryStatus.DRAFT;

    @Column(name = "revision", nullable = false, updatable = false)
    private int revision = 1;

    @Column(name = "supersedes_id", updatable = false)
    private UUID supersedesId;

    @Column(name = "void_reason")
    private String voidReason;

    @Column(name = "evaluator_user_id", updatable = false)
    private UUID evaluatorUserId;

    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private Instant evaluatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected GradeEntry() {}

    /** New DRAFT row — {@link #publish} moves it to PUBLISHED. */
    public GradeEntry(
            Enrollment enrollment,
            UUID assessmentComponentId,
            UUID activityId,
            GradeSource source,
            GradeSourceRefType sourceRefType,
            UUID sourceRefId,
            BigDecimal score,
            BigDecimal maxScore,
            UUID evaluatorUserId,
            Instant evaluatedAt) {
        this.enrollment = Guards.required(enrollment, "enrollment");
        this.assessmentComponentId = assessmentComponentId;
        this.activityId = activityId;
        this.source = Guards.required(source, "source");
        this.sourceRefType = sourceRefType;
        this.sourceRefId = sourceRefId;
        Guards.require(maxScore != null && maxScore.signum() > 0, "maxScore must be > 0");
        this.maxScore = maxScore;
        Guards.require(
                score != null && score.signum() >= 0 && score.compareTo(maxScore) <= 0,
                "score must be between 0 and maxScore");
        this.score = score;
        this.evaluatorUserId = evaluatorUserId;
        this.evaluatedAt = Guards.required(evaluatedAt, "evaluatedAt");
        this.createdAt = evaluatedAt;
    }

    /**
     * A superseding revision, created in one step (never left DRAFT) — the whole point of a
     * correction is that it immediately becomes the new current entry. {@code supersedesId} links
     * back to the row it replaces; the caller is responsible for calling {@link #markRevised()} on
     * that row in the same transaction (matches {@code trg_grade_entry_immutable}'s own "old row
     * becomes REVISED" expectation).
     */
    public static GradeEntry supersede(
            GradeEntry previous,
            BigDecimal score,
            BigDecimal maxScore,
            GradeSource source,
            UUID evaluatorUserId,
            Instant now) {
        return supersede(
                previous,
                score,
                maxScore,
                source,
                GradeSourceRefType.MANUAL,
                previous.sourceRefId,
                evaluatorUserId,
                now);
    }

    public static GradeEntry supersede(
            GradeEntry previous,
            BigDecimal score,
            BigDecimal maxScore,
            GradeSource source,
            GradeSourceRefType sourceRefType,
            UUID sourceRefId,
            UUID evaluatorUserId,
            Instant now) {
        GradeEntry next =
                new GradeEntry(
                        previous.enrollment,
                        previous.assessmentComponentId,
                        previous.activityId,
                        source,
                        sourceRefType,
                        sourceRefId,
                        score,
                        maxScore,
                        evaluatorUserId,
                        now);
        next.revision = previous.revision + 1;
        next.supersedesId = previous.id;
        next.status = GradeEntryStatus.PUBLISHED;
        next.publishedAt = now;
        return next;
    }

    public void publish(Instant now) {
        if (status != GradeEntryStatus.DRAFT) {
            throw Exceptions.rule(ErrorCode.GRADE_IMMUTABLE, "Only a DRAFT entry can be published");
        }
        this.status = GradeEntryStatus.PUBLISHED;
        this.publishedAt = Guards.required(now, "now");
    }

    /** Called on the OLD row when a new revision supersedes it — never on a fresh entry. */
    public void markRevised() {
        if (status != GradeEntryStatus.PUBLISHED) {
            throw Exceptions.rule(
                    ErrorCode.GRADE_IMMUTABLE, "Only a PUBLISHED entry can be revised");
        }
        this.status = GradeEntryStatus.REVISED;
    }

    public void voidEntry(String reason) {
        if (status == GradeEntryStatus.VOIDED) {
            throw Exceptions.rule(ErrorCode.GRADE_IMMUTABLE, "Entry is already VOIDED");
        }
        this.status = GradeEntryStatus.VOIDED;
        this.voidReason = Guards.requiredText(reason, "voidReason");
    }

    public UUID getId() {
        return id;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public UUID getAssessmentComponentId() {
        return assessmentComponentId;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public GradeSource getSource() {
        return source;
    }

    public GradeSourceRefType getSourceRefType() {
        return sourceRefType;
    }

    public UUID getSourceRefId() {
        return sourceRefId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public GradeEntryStatus getStatus() {
        return status;
    }

    public int getRevision() {
        return revision;
    }

    public UUID getSupersedesId() {
        return supersedesId;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public UUID getEvaluatorUserId() {
        return evaluatorUserId;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
