package uz.aifu.lms.coding.domain;

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
 * {@code explanation} is a reference explanation — teacher-visible only until the task is solved
 * (V010 column comment); the student mapper omits it until then, matching {@code
 * coding_task_languages.solution_code}'s "never before solved" spirit.
 */
@Entity
@Table(name = "coding_task_translations")
public class CodingTaskTranslation {

    @EmbeddedId private CodingTaskTranslationId id;

    @MapsId("codingTaskId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coding_task_id")
    private CodingTask codingTask;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "instructions")
    private String instructions;

    @Column(name = "constraints_text")
    private String constraintsText;

    @Column(name = "explanation")
    private String explanation;

    protected CodingTaskTranslation() {}

    public CodingTaskTranslation(
            CodingTask codingTask,
            String locale,
            String title,
            String description,
            String instructions,
            String constraintsText,
            String explanation) {
        this.codingTask = Guards.required(codingTask, "codingTask");
        this.id =
                new CodingTaskTranslationId(
                        codingTask.getId(), Guards.requiredText(locale, "locale"));
        this.title = Guards.requiredText(title, "title");
        this.description = Guards.requiredText(description, "description");
        this.instructions = instructions;
        this.constraintsText = constraintsText;
        this.explanation = explanation;
    }

    public void update(
            String title,
            String description,
            String instructions,
            String constraintsText,
            String explanation) {
        this.title = Guards.requiredText(title, "title");
        this.description = Guards.requiredText(description, "description");
        this.instructions = instructions;
        this.constraintsText = constraintsText;
        this.explanation = explanation;
    }

    public String getLocale() {
        return id.getLocale();
    }

    public CodingTask getCodingTask() {
        return codingTask;
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

    public String getConstraintsText() {
        return constraintsText;
    }

    public String getExplanation() {
        return explanation;
    }
}
