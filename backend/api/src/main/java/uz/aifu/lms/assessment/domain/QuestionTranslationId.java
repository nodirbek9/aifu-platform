package uz.aifu.lms.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class QuestionTranslationId implements Serializable {

    @Column(name = "question_id")
    private UUID questionId;

    @Column(name = "locale")
    private String locale;

    protected QuestionTranslationId() {}

    public QuestionTranslationId(UUID questionId, String locale) {
        this.questionId = questionId;
        this.locale = locale;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuestionTranslationId that)) {
            return false;
        }
        return Objects.equals(questionId, that.questionId) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, locale);
    }
}
