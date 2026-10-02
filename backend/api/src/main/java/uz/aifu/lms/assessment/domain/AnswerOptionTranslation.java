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

@Entity
@Table(name = "answer_option_translations")
public class AnswerOptionTranslation {

    @EmbeddedId private AnswerOptionTranslationId id;

    @MapsId("optionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id")
    private AnswerOption option;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "feedback")
    private String feedback;

    protected AnswerOptionTranslation() {}

    public AnswerOptionTranslation(
            AnswerOption option, String locale, String text, String feedback) {
        this.option = Guards.required(option, "option");
        this.id =
                new AnswerOptionTranslationId(
                        option.getId(), Guards.requiredText(locale, "locale"));
        this.text = Guards.requiredText(text, "text");
        this.feedback = feedback;
    }

    public void update(String text, String feedback) {
        this.text = Guards.requiredText(text, "text");
        this.feedback = feedback;
    }

    public String getLocale() {
        return id.getLocale();
    }

    public AnswerOption getOption() {
        return option;
    }

    public String getText() {
        return text;
    }

    public String getFeedback() {
        return feedback;
    }
}
