package uz.aifu.lms.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.common.util.Guards;

/**
 * A snapshot of one question actually served within one {@link QuizAttempt}, with the order used —
 * keeps an attempt reproducible even if the teacher later edits the quiz (V009 table comment). Not
 * a {@link uz.aifu.lms.common.domain.BaseEntity}: no audit columns on this table either.
 */
@Entity
@Table(name = "quiz_attempt_questions")
public class QuizAttemptQuestion {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false, updatable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, updatable = false)
    private Question question;

    @Column(name = "served_position", nullable = false, updatable = false)
    private int servedPosition;

    @Column(name = "points_possible", nullable = false, updatable = false)
    private BigDecimal pointsPossible;

    @Column(name = "points_awarded")
    private BigDecimal pointsAwarded;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "response_text")
    private String responseText;

    @Column(name = "response_number")
    private BigDecimal responseNumber;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "graded_at")
    private Instant gradedAt;

    @Column(name = "grader_note")
    private String graderNote;

    protected QuizAttemptQuestion() {}

    public QuizAttemptQuestion(
            QuizAttempt attempt, Question question, int servedPosition, BigDecimal pointsPossible) {
        this.attempt = Guards.required(attempt, "attempt");
        this.question = Guards.required(question, "question");
        Guards.require(servedPosition >= 0, "servedPosition must be >= 0");
        this.servedPosition = servedPosition;
        this.pointsPossible = Guards.required(pointsPossible, "pointsPossible");
    }

    /**
     * Overwritable while the attempt is still {@code IN_PROGRESS} — a student may change answers.
     */
    public void recordResponse(String responseText, BigDecimal responseNumber, Instant now) {
        this.responseText = responseText;
        this.responseNumber = responseNumber;
        this.respondedAt = Guards.required(now, "now");
    }

    public void grade(BigDecimal pointsAwarded, boolean correct, String graderNote, Instant now) {
        this.pointsAwarded = Guards.required(pointsAwarded, "pointsAwarded");
        this.correct = correct;
        this.graderNote = graderNote;
        this.gradedAt = Guards.required(now, "now");
    }

    public UUID getId() {
        return id;
    }

    public QuizAttempt getAttempt() {
        return attempt;
    }

    public Question getQuestion() {
        return question;
    }

    public int getServedPosition() {
        return servedPosition;
    }

    public BigDecimal getPointsPossible() {
        return pointsPossible;
    }

    public BigDecimal getPointsAwarded() {
        return pointsAwarded;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public String getResponseText() {
        return responseText;
    }

    public BigDecimal getResponseNumber() {
        return responseNumber;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public Instant getGradedAt() {
        return gradedAt;
    }

    public String getGraderNote() {
        return graderNote;
    }
}
