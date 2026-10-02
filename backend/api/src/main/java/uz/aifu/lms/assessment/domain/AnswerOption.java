package uz.aifu.lms.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import uz.aifu.lms.common.util.Guards;

/**
 * Not a {@link uz.aifu.lms.common.domain.BaseEntity}: {@code answer_options} (V009) has no {@code
 * created_at}/{@code updated_at}/{@code version} columns.
 *
 * <p>{@link #correct} is NEVER serialized into a student-facing DTO before {@code
 * Quiz.revealPolicy} allows it — enforced by using a separate mapper for student vs teacher
 * responses (V009 column comment), never a per-field visibility flag here.
 */
@Entity
@Table(name = "answer_options")
public class AnswerOption {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, updatable = false)
    private Question question;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    /** Pairing key for MATCHING questions. */
    @Column(name = "match_key")
    private String matchKey;

    protected AnswerOption() {}

    public AnswerOption(Question question, int position, boolean correct, String matchKey) {
        this.question = Guards.required(question, "question");
        Guards.require(position >= 0, "position must be >= 0");
        this.position = position;
        this.correct = correct;
        this.matchKey = matchKey;
    }

    public void update(boolean correct, String matchKey) {
        this.correct = correct;
        this.matchKey = matchKey;
    }

    public void reorderTo(int newPosition) {
        Guards.require(newPosition >= 0, "position must be >= 0");
        this.position = newPosition;
    }

    public UUID getId() {
        return id;
    }

    public Question getQuestion() {
        return question;
    }

    public int getPosition() {
        return position;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getMatchKey() {
        return matchKey;
    }
}
