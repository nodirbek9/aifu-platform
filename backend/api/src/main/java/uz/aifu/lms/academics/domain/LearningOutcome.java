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
import uz.aifu.lms.common.domain.BaseEntity;
import uz.aifu.lms.common.util.Guards;

/** "Ta'lim natijalari" TN1..TNn. */
@Entity
@Table(
        name = "learning_outcomes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "code"}))
public class LearningOutcome extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "code", nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private LearningOutcomeCategory category;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected LearningOutcome() {}

    public LearningOutcome(
            Subject subject, String code, LearningOutcomeCategory category, int displayOrder) {
        this.subject = Guards.required(subject, "subject");
        this.code = Guards.requiredText(code, "code");
        this.category = Guards.required(category, "category");
        this.displayOrder = displayOrder;
    }

    public void update(String code, LearningOutcomeCategory category, int displayOrder) {
        this.code = Guards.requiredText(code, "code");
        this.category = Guards.required(category, "category");
        this.displayOrder = displayOrder;
    }

    public Subject getSubject() {
        return subject;
    }

    public String getCode() {
        return code;
    }

    public LearningOutcomeCategory getCategory() {
        return category;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
