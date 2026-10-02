package uz.aifu.lms.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class QuizTranslationId implements Serializable {

    @Column(name = "quiz_id")
    private UUID quizId;

    @Column(name = "locale")
    private String locale;

    protected QuizTranslationId() {}

    public QuizTranslationId(UUID quizId, String locale) {
        this.quizId = quizId;
        this.locale = locale;
    }

    public UUID getQuizId() {
        return quizId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuizTranslationId that)) {
            return false;
        }
        return Objects.equals(quizId, that.quizId) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quizId, locale);
    }
}
