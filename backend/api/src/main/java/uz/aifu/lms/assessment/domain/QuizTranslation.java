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
@Table(name = "quiz_translations")
public class QuizTranslation {

    @EmbeddedId private QuizTranslationId id;

    @MapsId("quizId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "instructions")
    private String instructions;

    protected QuizTranslation() {}

    public QuizTranslation(
            Quiz quiz, String locale, String title, String description, String instructions) {
        this.quiz = Guards.required(quiz, "quiz");
        this.id = new QuizTranslationId(quiz.getId(), Guards.requiredText(locale, "locale"));
        this.title = Guards.requiredText(title, "title");
        this.description = description;
        this.instructions = instructions;
    }

    public void update(String title, String description, String instructions) {
        this.title = Guards.requiredText(title, "title");
        this.description = description;
        this.instructions = instructions;
    }

    public String getLocale() {
        return id.getLocale();
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getInstructions() {
        return instructions;
    }
}
