package uz.aifu.lms.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/** Read-only mapping of {@code v_question_difficulty} (V015) — item analysis per question. */
@Entity
@Immutable
@Table(name = "v_question_difficulty")
public class QuestionDifficultyView {

    @Id
    @Column(name = "question_id")
    private UUID questionId;

    @Column(name = "quiz_id")
    private UUID quizId;

    @Column(name = "responses")
    private long responses;

    @Column(name = "correct_responses")
    private long correctResponses;

    @Column(name = "correct_percent")
    private BigDecimal correctPercent;

    protected QuestionDifficultyView() {}

    public UUID getQuestionId() {
        return questionId;
    }

    public UUID getQuizId() {
        return quizId;
    }

    public long getResponses() {
        return responses;
    }

    public long getCorrectResponses() {
        return correctResponses;
    }

    public BigDecimal getCorrectPercent() {
        return correctPercent;
    }
}
