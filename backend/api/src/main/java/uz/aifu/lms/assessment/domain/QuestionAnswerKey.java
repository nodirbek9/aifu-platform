package uz.aifu.lms.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * The expected answer for SHORT_ANSWER/NUMERIC (and MATCHING/ORDERING's per-key tolerance, where
 * applicable) questions. Not a {@link uz.aifu.lms.common.domain.BaseEntity}: {@code
 * question_answer_keys} (V009) has no audit columns. {@code locale = null} means
 * language-independent (e.g. a NUMERIC answer).
 */
@Entity
@Table(name = "question_answer_keys")
public class QuestionAnswerKey {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, updatable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_mode", nullable = false)
    private MatchMode matchMode;

    @Column(name = "expected_value", nullable = false)
    private String expectedValue;

    @Column(name = "tolerance")
    private BigDecimal tolerance;

    @Column(name = "locale")
    private String locale;

    protected QuestionAnswerKey() {}

    public QuestionAnswerKey(
            Question question,
            MatchMode matchMode,
            String expectedValue,
            BigDecimal tolerance,
            String locale) {
        this.question = Guards.required(question, "question");
        this.matchMode = Guards.required(matchMode, "matchMode");
        this.expectedValue = Guards.requiredText(expectedValue, "expectedValue");
        requireToleranceConsistency(matchMode, tolerance);
        this.tolerance = tolerance;
        this.locale = locale;
    }

    public void update(
            MatchMode matchMode, String expectedValue, BigDecimal tolerance, String locale) {
        Guards.required(matchMode, "matchMode");
        requireToleranceConsistency(matchMode, tolerance);
        this.matchMode = matchMode;
        this.expectedValue = Guards.requiredText(expectedValue, "expectedValue");
        this.tolerance = tolerance;
        this.locale = locale;
    }

    private static void requireToleranceConsistency(MatchMode matchMode, BigDecimal tolerance) {
        if (matchMode == MatchMode.NUMERIC_TOLERANCE && tolerance == null) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "NUMERIC_TOLERANCE requires a tolerance value");
        }
    }

    public UUID getId() {
        return id;
    }

    public Question getQuestion() {
        return question;
    }

    public MatchMode getMatchMode() {
        return matchMode;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public BigDecimal getTolerance() {
        return tolerance;
    }

    public String getLocale() {
        return locale;
    }
}
