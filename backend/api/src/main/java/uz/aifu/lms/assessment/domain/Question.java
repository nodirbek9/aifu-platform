package uz.aifu.lms.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.aifu.lms.common.domain.VersionedEntity;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.curriculum.domain.Difficulty;

/**
 * THE multi-type extension point (V009) — {@link #questionType} is dispatched by {@code
 * assessment.app.grading.QuestionGraderRegistry}. {@code difficulty} reuses {@code
 * curriculum.domain.Difficulty} directly (identical value set, same reasoning as {@code
 * Lesson.sessionType} reusing {@code academics.domain.SessionType}).
 */
@Entity
@Table(name = "questions")
public class Question extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false, updatable = false)
    private Quiz quiz;

    @Column(name = "position", nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, updatable = false)
    private QuestionType questionType;

    @Column(name = "points", nullable = false)
    private BigDecimal points;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    /** Partial credit for MULTIPLE_CHOICE, tolerance for NUMERIC, etc. — grader-specific. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings", nullable = false)
    private String settings = "{}";

    protected Question() {}

    public Question(Quiz quiz, int position, QuestionType questionType, BigDecimal points) {
        this.quiz = Guards.required(quiz, "quiz");
        Guards.require(position >= 0, "position must be >= 0");
        this.position = position;
        this.questionType = Guards.required(questionType, "questionType");
        Guards.require(points != null && points.signum() > 0, "points must be > 0");
        this.points = points;
    }

    public void updateDetails(
            BigDecimal points, Difficulty difficulty, boolean required, String settingsJson) {
        Guards.require(points != null && points.signum() > 0, "points must be > 0");
        this.points = points;
        this.difficulty = difficulty;
        this.required = required;
        this.settings = settingsJson == null ? "{}" : settingsJson;
    }

    public void reorderTo(int newPosition) {
        Guards.require(newPosition >= 0, "position must be >= 0");
        this.position = newPosition;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public int getPosition() {
        return position;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isRequired() {
        return required;
    }

    public String getSettings() {
        return settings;
    }
}
