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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.enrollment.domain.Enrollment;

/**
 * One student's attempt at a {@link Quiz} (V009, brief §35: repeated quiz submission). Not a {@link
 * uz.aifu.lms.common.domain.BaseEntity}/{@link uz.aifu.lms.common.domain.VersionedEntity}: {@code
 * quiz_attempts} has {@code version} but no {@code created_at}/{@code updated_at} at all — {@code
 * started_at} already records the moment that matters. Attempt history itself is append-only in
 * spirit: a graded attempt is never re-graded, a new attempt is a new row ({@code attempt_number +
 * 1}), matching {@code Enrollment}'s re-enrollment pattern.
 */
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false, updatable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @Column(name = "attempt_number", nullable = false, updatable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "max_score")
    private BigDecimal maxScore;

    @Column(name = "percent")
    private BigDecimal percent;

    @Column(name = "passed")
    private Boolean passed;

    @Column(name = "locale", updatable = false)
    private String locale;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "expires_at", updatable = false)
    private Instant expiresAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "graded_at")
    private Instant gradedAt;

    /** Set once, by {@link #submit}, never at construction — see its javadoc. */
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected QuizAttempt() {}

    public QuizAttempt(
            Quiz quiz,
            Enrollment enrollment,
            Activity activity,
            int attemptNumber,
            String locale,
            Instant startedAt,
            Instant expiresAt) {
        this.quiz = Guards.required(quiz, "quiz");
        this.enrollment = Guards.required(enrollment, "enrollment");
        this.activity = activity;
        Guards.require(attemptNumber >= 1, "attemptNumber must be >= 1");
        this.attemptNumber = attemptNumber;
        this.locale = locale;
        this.startedAt = Guards.required(startedAt, "startedAt");
        this.expiresAt = expiresAt;
    }

    /** True once the time limit (if any) has passed without a submission. */
    public boolean isExpired(Instant now) {
        return status == AttemptStatus.IN_PROGRESS && expiresAt != null && now.isAfter(expiresAt);
    }

    /**
     * The time ran out: the attempt is handed in as it stands, dated at the moment the time ended.
     * What the student had saved is then graded like any other submission — running out of time
     * costs the unanswered questions, not the answered ones.
     */
    public void submitAtDeadline() {
        requireStatus(AttemptStatus.IN_PROGRESS, AttemptStatus.SUBMITTED);
        this.status = AttemptStatus.SUBMITTED;
        this.submittedAt = Guards.required(expiresAt, "expiresAt");
    }

    /** True when the clock handed this attempt in, not the student. */
    public boolean wasClosedByTime() {
        return status != AttemptStatus.IN_PROGRESS
                && expiresAt != null
                && expiresAt.equals(submittedAt);
    }

    public void abandon() {
        requireStatus(AttemptStatus.IN_PROGRESS, AttemptStatus.ABANDONED);
        this.status = AttemptStatus.ABANDONED;
    }

    /**
     * {@code idempotencyKey}, if the caller sent one, is recorded here — not at construction — so
     * {@code AttemptService.submit} can recognise an exact retry of this same submit call later and
     * replay the result instead of raising {@code ATTEMPT_ALREADY_SUBMITTED} (CLAUDE.md §9).
     */
    public void submit(Instant now, String idempotencyKey) {
        requireStatus(AttemptStatus.IN_PROGRESS, AttemptStatus.SUBMITTED);
        this.status = AttemptStatus.SUBMITTED;
        this.submittedAt = Guards.required(now, "now");
        this.idempotencyKey = idempotencyKey;
    }

    /** Deterministic grading result — see {@code AttemptService}'s grader dispatch. */
    public void grade(
            BigDecimal score,
            BigDecimal maxScore,
            BigDecimal percent,
            boolean passed,
            Instant now) {
        requireStatus(AttemptStatus.SUBMITTED, AttemptStatus.GRADED);
        this.status = AttemptStatus.GRADED;
        this.score = Guards.required(score, "score");
        this.maxScore = Guards.required(maxScore, "maxScore");
        this.percent = Guards.required(percent, "percent");
        this.passed = passed;
        this.gradedAt = Guards.required(now, "now");
    }

    private void requireStatus(AttemptStatus expected, AttemptStatus target) {
        if (status != expected) {
            throw Exceptions.rule(
                            ErrorCode.INVALID_STATE_TRANSITION,
                            "Cannot move quiz attempt from " + status + " to " + target)
                    .with("from", status.name())
                    .with("to", target.name());
        }
    }

    public UUID getId() {
        return id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public Activity getActivity() {
        return activity;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public BigDecimal getScore() {
        return score;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public Boolean getPassed() {
        return passed;
    }

    public String getLocale() {
        return locale;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getGradedAt() {
        return gradedAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public long getVersion() {
        return version;
    }
}
