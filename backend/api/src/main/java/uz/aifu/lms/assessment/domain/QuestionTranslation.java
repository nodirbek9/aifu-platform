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

/** {@code explanation} is revealed only per {@code quizzes.reveal_policy} (V009 column comment). */
@Entity
@Table(name = "question_translations")
public class QuestionTranslation {

    @EmbeddedId private QuestionTranslationId id;

    @MapsId("questionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "prompt", nullable = false)
    private String prompt;

    @Column(name = "explanation")
    private String explanation;

    @Column(name = "hint")
    private String hint;

    protected QuestionTranslation() {}

    public QuestionTranslation(
            Question question, String locale, String prompt, String explanation, String hint) {
        this.question = Guards.required(question, "question");
        this.id =
                new QuestionTranslationId(question.getId(), Guards.requiredText(locale, "locale"));
        this.prompt = Guards.requiredText(prompt, "prompt");
        this.explanation = explanation;
        this.hint = hint;
    }

    public void update(String prompt, String explanation, String hint) {
        this.prompt = Guards.requiredText(prompt, "prompt");
        this.explanation = explanation;
        this.hint = hint;
    }

    public String getLocale() {
        return id.getLocale();
    }

    public Question getQuestion() {
        return question;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getHint() {
        return hint;
    }
}
