package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class LearningOutcomeTranslationId implements Serializable {

    @Column(name = "learning_outcome_id")
    private UUID learningOutcomeId;

    @Column(name = "locale")
    private String locale;

    protected LearningOutcomeTranslationId() {}

    public LearningOutcomeTranslationId(UUID learningOutcomeId, String locale) {
        this.learningOutcomeId = learningOutcomeId;
        this.locale = locale;
    }

    public UUID getLearningOutcomeId() {
        return learningOutcomeId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearningOutcomeTranslationId that)) {
            return false;
        }
        return Objects.equals(learningOutcomeId, that.learningOutcomeId)
                && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(learningOutcomeId, locale);
    }
}
