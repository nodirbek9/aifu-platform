package uz.aifu.lms.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class QuizAttemptSelectedOptionId implements Serializable {

    @Column(name = "attempt_question_id")
    private UUID attemptQuestionId;

    @Column(name = "option_id")
    private UUID optionId;

    protected QuizAttemptSelectedOptionId() {}

    public QuizAttemptSelectedOptionId(UUID attemptQuestionId, UUID optionId) {
        this.attemptQuestionId = attemptQuestionId;
        this.optionId = optionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuizAttemptSelectedOptionId that)) {
            return false;
        }
        return Objects.equals(attemptQuestionId, that.attemptQuestionId)
                && Objects.equals(optionId, that.optionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attemptQuestionId, optionId);
    }
}
