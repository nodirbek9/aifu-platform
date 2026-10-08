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
import java.util.UUID;
import uz.aifu.lms.academics.domain.AssessmentComponent;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.domain.VersionedEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * "Anything a student DOES." THE multi-subject extension point (V007) — {@link #referenceId} points
 * at a quiz/coding-task/etc, resolved by whatever handler is registered for {@link #activityType}.
 * No code anywhere may branch on a subject code or name (CLAUDE.md §11).
 */
@Entity
@Table(name = "activities")
public class Activity extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false, updatable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private CourseModule module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(name = "position", nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, updatable = false)
    private ActivityType activityType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_component_id")
    private AssessmentComponent assessmentComponent;

    @Column(name = "is_graded", nullable = false)
    private boolean graded = true;

    @Column(name = "max_points")
    private BigDecimal maxPoints;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "attempt_limit")
    private Integer attemptLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "collaboration_mode", nullable = false)
    private CollaborationMode collaborationMode = CollaborationMode.INDIVIDUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "variant_strategy", nullable = false)
    private VariantStrategy variantStrategy = VariantStrategy.NONE;

    @Column(name = "available_from")
    private Instant availableFrom;

    @Column(name = "due_at")
    private Instant dueAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "path_step")
    private PathStep pathStep;

    protected Activity() {}

    public Activity(
            Course course,
            CourseModule module,
            Lesson lesson,
            ActivityType activityType,
            int position,
            boolean graded,
            AssessmentComponent assessmentComponent,
            BigDecimal maxPoints) {
        this.course = Guards.required(course, "course");
        if (module == null && lesson == null) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED,
                    "An activity must be placed under a module or a lesson");
        }
        this.module = module;
        this.lesson = lesson;
        this.activityType = Guards.required(activityType, "activityType");
        Guards.require(position >= 0, "position must be >= 0");
        this.position = position;
        setGrading(graded, assessmentComponent, maxPoints);
    }

    /** Brief §60: a graded activity must know where its points go. */
    public void setGrading(
            boolean graded, AssessmentComponent assessmentComponent, BigDecimal maxPoints) {
        if (graded && assessmentComponent == null) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED,
                    "A graded activity requires an assessment component");
        }
        if (graded && maxPoints == null) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "A graded activity requires maxPoints");
        }
        this.graded = graded;
        this.assessmentComponent = assessmentComponent;
        this.maxPoints = maxPoints;
    }

    public void updateDetails(
            UUID referenceId,
            BigDecimal weight,
            Integer attemptLimit,
            CollaborationMode collaborationMode,
            VariantStrategy variantStrategy,
            Instant availableFrom,
            Instant dueAt) {
        this.referenceId = referenceId;
        this.weight = weight;
        this.attemptLimit = attemptLimit;
        this.collaborationMode =
                collaborationMode == null ? CollaborationMode.INDIVIDUAL : collaborationMode;
        this.variantStrategy = variantStrategy == null ? VariantStrategy.NONE : variantStrategy;
        this.availableFrom = availableFrom;
        this.dueAt = dueAt;
    }

    public void reorderTo(int newPosition) {
        Guards.require(newPosition >= 0, "position must be >= 0");
        this.position = newPosition;
    }

    public void publish(Instant now) {
        requireTransition(PublicationStatus.PUBLISHED);
        this.status = PublicationStatus.PUBLISHED;
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
                    "Cannot move activity from " + status + " to " + target);
        }
    }

    public Course getCourse() {
        return course;
    }

    public CourseModule getModule() {
        return module;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public int getPosition() {
        return position;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public AssessmentComponent getAssessmentComponent() {
        return assessmentComponent;
    }

    public boolean isGraded() {
        return graded;
    }

    public BigDecimal getMaxPoints() {
        return maxPoints;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public Integer getAttemptLimit() {
        return attemptLimit;
    }

    public CollaborationMode getCollaborationMode() {
        return collaborationMode;
    }

    public VariantStrategy getVariantStrategy() {
        return variantStrategy;
    }

    public Instant getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(Instant availableFrom) {
        this.availableFrom = availableFrom;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public PathStep getPathStep() {
        return pathStep;
    }

    public void setPathStep(PathStep pathStep) {
        this.pathStep = pathStep;
    }
}
