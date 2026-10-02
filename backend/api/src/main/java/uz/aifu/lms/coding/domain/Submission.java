package uz.aifu.lms.coding.domain;

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
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.enrollment.domain.Enrollment;

/**
 * One student's submission of source code for a {@link CodingTask} (V010, brief §17). Append-only:
 * a re-submission is a NEW row with the next {@code attempt_number}, never overwritten (V010 table
 * comment) — a submission is evidence of what the student wrote.
 *
 * <p>Not a {@link uz.aifu.lms.common.domain.BaseEntity}/{@link
 * uz.aifu.lms.common.domain.VersionedEntity}: {@code submissions} has {@code version} but no {@code
 * created_at}/{@code updated_at} at all — same shape as {@code QuizAttempt} (Phase 7).
 *
 * <p>{@link #status}/{@link #verdict}/{@link #score}/{@link #maxScore}/{@link #testsPassed}/ {@link
 * #testsTotal}/{@link #completedAt} are written by {@code execution-worker} — a <b>separate
 * process</b> connecting with the restricted {@code aifu_execution_worker} role (V014) via plain
 * JDBC, never through this entity. The API only ever reads them back; there are deliberately no
 * setters here for fields this process never writes.
 */
@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coding_task_id", nullable = false, updatable = false)
    private CodingTask codingTask;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_code", nullable = false, updatable = false)
    private ProgrammingLanguage language;

    @Column(name = "attempt_number", nullable = false, updatable = false)
    private int attemptNumber;

    @Column(name = "source_code", nullable = false, updatable = false)
    private String sourceCode;

    @Column(name = "source_size_bytes", nullable = false, updatable = false)
    private int sourceSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubmissionStatus status = SubmissionStatus.QUEUED;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict")
    private Verdict verdict;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "max_score")
    private BigDecimal maxScore;

    @Column(name = "tests_passed")
    private Integer testsPassed;

    @Column(name = "tests_total")
    private Integer testsTotal;

    @Column(name = "idempotency_key", updatable = false)
    private String idempotencyKey;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "progress_synced_at")
    private Instant progressSyncedAt;

    @Column(name = "grade_synced_at")
    private Instant gradeSyncedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Submission() {}

    public Submission(
            CodingTask codingTask,
            Enrollment enrollment,
            Activity activity,
            ProgrammingLanguage language,
            int attemptNumber,
            String sourceCode,
            String idempotencyKey,
            Instant submittedAt) {
        this.codingTask = Guards.required(codingTask, "codingTask");
        this.enrollment = Guards.required(enrollment, "enrollment");
        this.activity = activity;
        this.language = Guards.required(language, "language");
        Guards.require(attemptNumber >= 1, "attemptNumber must be >= 1");
        this.attemptNumber = attemptNumber;
        this.sourceCode = Guards.requiredText(sourceCode, "sourceCode");
        this.sourceSizeBytes =
                this.sourceCode.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        this.idempotencyKey = idempotencyKey;
        this.submittedAt = Guards.required(submittedAt, "submittedAt");
    }

    public UUID getId() {
        return id;
    }

    public CodingTask getCodingTask() {
        return codingTask;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public Activity getActivity() {
        return activity;
    }

    public ProgrammingLanguage getLanguage() {
        return language;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public int getSourceSizeBytes() {
        return sourceSizeBytes;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public BigDecimal getScore() {
        return score;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public Integer getTestsPassed() {
        return testsPassed;
    }

    public Integer getTestsTotal() {
        return testsTotal;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getProgressSyncedAt() {
        return progressSyncedAt;
    }

    /**
     * Marks this submission as processed by {@code progress.app.ProgressService}'s scheduled
     * sweeper (V016, ADR-007's "progress recompute" scheduled work) — the API-side bridge back from
     * {@code execution-worker}, a separate process with no shared Spring event bus.
     */
    public void markProgressSynced(Instant now) {
        this.progressSyncedAt = Guards.required(now, "now");
    }

    public Instant getGradeSyncedAt() {
        return gradeSyncedAt;
    }

    /**
     * Marks this submission as processed by {@code grading.app.GradeEntryService}'s scheduled
     * sweeper (V017) — same bridge shape as {@link #markProgressSynced}, a separate marker column
     * since grading and progress are independent consumers of the same terminal-submission signal.
     */
    public void markGradeSynced(Instant now) {
        this.gradeSyncedAt = Guards.required(now, "now");
    }

    public long getVersion() {
        return version;
    }
}
