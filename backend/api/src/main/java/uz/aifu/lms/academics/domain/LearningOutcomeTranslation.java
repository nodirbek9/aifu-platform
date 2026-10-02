package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import uz.aifu.lms.common.util.Guards;

@Entity
@Table(name = "learning_outcome_translations")
public class LearningOutcomeTranslation {

    @EmbeddedId private LearningOutcomeTranslationId id;

    @MapsId("learningOutcomeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_outcome_id")
    private LearningOutcome learningOutcome;

    @Column(name = "statement", nullable = false)
    private String statement;

    protected LearningOutcomeTranslation() {}

    public LearningOutcomeTranslation(
            LearningOutcome learningOutcome, String locale, String statement) {
        this.learningOutcome = Guards.required(learningOutcome, "learningOutcome");
        this.id =
                new LearningOutcomeTranslationId(
                        learningOutcome.getId(), Guards.requiredText(locale, "locale"));
        this.statement = Guards.requiredText(statement, "statement");
    }

    public void update(String statement) {
        this.statement = Guards.requiredText(statement, "statement");
    }

    public String getLocale() {
        return id.getLocale();
    }

    public LearningOutcome getLearningOutcome() {
        return learningOutcome;
    }

    public String getStatement() {
        return statement;
    }
}
