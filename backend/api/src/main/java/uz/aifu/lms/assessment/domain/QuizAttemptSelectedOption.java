package uz.aifu.lms.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import uz.aifu.lms.common.util.Guards;

/**
 * One selected/ranked option within a {@link QuizAttemptQuestion} — SINGLE_CHOICE/MULTIPLE_CHOICE/
 * TRUE_FALSE/MATCHING all reduce to "which options were picked"; {@link #ordinal} additionally
 * carries the student's chosen order for ORDERING questions.
 */
@Entity
@Table(name = "quiz_attempt_selected_options")
public class QuizAttemptSelectedOption {

    @EmbeddedId private QuizAttemptSelectedOptionId id;

    @MapsId("attemptQuestionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_question_id")
    private QuizAttemptQuestion attemptQuestion;

    @MapsId("optionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id")
    private AnswerOption option;

    @Column(name = "ordinal")
    private Integer ordinal;

    protected QuizAttemptSelectedOption() {}

    public QuizAttemptSelectedOption(
            QuizAttemptQuestion attemptQuestion, AnswerOption option, Integer ordinal) {
        this.attemptQuestion = Guards.required(attemptQuestion, "attemptQuestion");
        this.option = Guards.required(option, "option");
        this.id = new QuizAttemptSelectedOptionId(attemptQuestion.getId(), option.getId());
        this.ordinal = ordinal;
    }

    public QuizAttemptQuestion getAttemptQuestion() {
        return attemptQuestion;
    }

    public AnswerOption getOption() {
        return option;
    }

    public Integer getOrdinal() {
        return ordinal;
    }
}
