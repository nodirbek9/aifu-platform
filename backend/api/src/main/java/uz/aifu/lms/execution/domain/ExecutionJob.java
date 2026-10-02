package uz.aifu.lms.execution.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.coding.domain.Submission;
import uz.aifu.lms.common.util.Guards;

/**
 * The execution queue (V011, ADR-007). The API only ever INSERTs a {@code PENDING} row, in the same
 * transaction as its {@link Submission} (see {@code execution.app.SubmissionSubmitService});
 * everything past that — claiming, leasing, retrying, marking {@code DEAD} — is {@code
 * execution-worker}'s job, done with plain JDBC through the restricted {@code
 * aifu_execution_worker} role (V014), never through this JPA entity. Not a {@link
 * uz.aifu.lms.common.domain.BaseEntity}: {@code execution_jobs} has {@code created_at} but no
 * {@code updated_at}/{@code version} — a queue row's history is its own status timestamps ({@code
 * claimed_at}, {@code started_at}, {@code finished_at}), not a generic audit pair.
 */
@Entity
@Table(name = "execution_jobs")
public class ExecutionJob {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, updatable = false)
    private Submission submission;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExecutionJobStatus status = ExecutionJobStatus.PENDING;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts = 3;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(name = "claimed_by")
    private String claimedBy;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "lease_expires_at")
    private Instant leaseExpiresAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "external_ref")
    private String externalRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ExecutionJob() {}

    public ExecutionJob(Submission submission, int priority, Instant now) {
        this.submission = Guards.required(submission, "submission");
        this.priority = priority;
        this.availableAt = Guards.required(now, "now");
        this.createdAt = now;
    }

    public UUID getId() {
        return id;
    }

    public Submission getSubmission() {
        return submission;
    }

    public ExecutionJobStatus getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Instant getAvailableAt() {
        return availableAt;
    }

    public String getClaimedBy() {
        return claimedBy;
    }

    public Instant getClaimedAt() {
        return claimedAt;
    }

    public Instant getLeaseExpiresAt() {
        return leaseExpiresAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
