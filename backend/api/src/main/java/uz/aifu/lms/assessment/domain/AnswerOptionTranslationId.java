package uz.aifu.lms.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class AnswerOptionTranslationId implements Serializable {

    @Column(name = "option_id")
    private UUID optionId;

    @Column(name = "locale")
    private String locale;

    protected AnswerOptionTranslationId() {}

    public AnswerOptionTranslationId(UUID optionId, String locale) {
        this.optionId = optionId;
        this.locale = locale;
    }

    public UUID getOptionId() {
        return optionId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnswerOptionTranslationId that)) {
            return false;
        }
        return Objects.equals(optionId, that.optionId) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionId, locale);
    }
}
