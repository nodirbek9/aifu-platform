package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import uz.aifu.lms.common.domain.BaseEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * Machine-computable "Talabalar bilimini baholash" rubric. Tree-shaped: {@code JN} has children
 * ({@code JN_ATTENDANCE}, {@code JN_PRACTICAL}, {@code JN_INDEPENDENT}). Top-level component
 * weights (those with no parent) must sum to the scheme total (100) — {@link
 * uz.aifu.lms.academics.app.AssessmentWeightValidator} enforces it.
 *
 * <p>{@code blocksFinal}: {@code IAKTT1105} — a student earning &lt;60% of JN or of ON is NOT
 * admitted to the final exam. {@code minPassPercent} + {@code blocksFinal} encode that gate.
 */
@Entity
@Table(
        name = "assessment_components",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "code"}))
public class AssessmentComponent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false, updatable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_component_id")
    private AssessmentComponent parent;

    @Column(name = "code", nullable = false, updatable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private AssessmentComponentKind kind;

    @Column(name = "max_points", nullable = false)
    private BigDecimal maxPoints;

    @Column(name = "min_pass_percent")
    private BigDecimal minPassPercent;

    @Column(name = "blocks_final", nullable = false)
    private boolean blocksFinal;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_form")
    private ComponentAssessmentForm assessmentForm;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected AssessmentComponent() {}

    public AssessmentComponent(
            Course course,
            AssessmentComponent parent,
            String code,
            AssessmentComponentKind kind,
            BigDecimal maxPoints,
            BigDecimal minPassPercent,
            boolean blocksFinal,
            ComponentAssessmentForm assessmentForm,
            int displayOrder) {
        this.course = Guards.required(course, "course");
        if (parent != null && !parent.course.equals(course)) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "parent component must belong to the same course");
        }
        this.parent = parent;
        this.code = Guards.requiredText(code, "code").toUpperCase();
        this.kind = Guards.required(kind, "kind");
        if (maxPoints == null || maxPoints.signum() <= 0) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "maxPoints must be > 0");
        }
        this.maxPoints = maxPoints;
        if (minPassPercent != null
                && (minPassPercent.signum() < 0
                        || minPassPercent.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "minPassPercent must be between 0 and 100");
        }
        this.minPassPercent = minPassPercent;
        this.blocksFinal = blocksFinal;
        this.assessmentForm = assessmentForm;
        this.displayOrder = displayOrder;
    }

    public void update(
            BigDecimal maxPoints,
            BigDecimal minPassPercent,
            boolean blocksFinal,
            ComponentAssessmentForm assessmentForm,
            int displayOrder) {
        if (maxPoints == null || maxPoints.signum() <= 0) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "maxPoints must be > 0");
        }
        this.maxPoints = maxPoints;
        this.minPassPercent = minPassPercent;
        this.blocksFinal = blocksFinal;
        this.assessmentForm = assessmentForm;
        this.displayOrder = displayOrder;
    }

    public boolean isTopLevel() {
        return parent == null;
    }

    public Course getCourse() {
        return course;
    }

    public AssessmentComponent getParent() {
        return parent;
    }

    public String getCode() {
        return code;
    }

    public AssessmentComponentKind getKind() {
        return kind;
    }

    public BigDecimal getMaxPoints() {
        return maxPoints;
    }

    public BigDecimal getMinPassPercent() {
        return minPassPercent;
    }

    public boolean isBlocksFinal() {
        return blocksFinal;
    }

    public ComponentAssessmentForm getAssessmentForm() {
        return assessmentForm;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
