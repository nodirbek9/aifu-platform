package uz.aifu.lms.worker.queue;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.worker.domain.ClaimedJob;

/**
 * {@code execution_jobs} claim/lease mechanics (ADR-007). Plain JDBC, no JPA — this module is a
 * physically separate process connecting with the restricted role from V014, which has no reason to
 * carry the API's full persistence stack.
 */
@Component
public class ExecutionJobQueue {

    private final JdbcTemplate jdbc;
    private final Clock clock;

    public ExecutionJobQueue(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * Claims up to {@code batchSize} PENDING jobs whose {@code available_at} has passed, oldest
     * highest-priority first, skipping rows another worker already holds ({@code FOR UPDATE SKIP
     * LOCKED} — V011's own documented claim statement).
     */
    @Transactional
    public List<ClaimedJob> claim(String workerId, int batchSize, Duration leaseDuration) {
        Timestamp now = Timestamp.from(clock.instant());
        Timestamp leaseExpiresAt = Timestamp.from(clock.instant().plus(leaseDuration));
        return jdbc.query(
                """
                UPDATE execution_jobs SET status = 'CLAIMED', claimed_by = ?, claimed_at = ?,
                    lease_expires_at = ?, attempt_count = attempt_count + 1
                WHERE id IN (
                    SELECT id FROM execution_jobs
                    WHERE status = 'PENDING' AND available_at <= ?
                    ORDER BY priority DESC, available_at
                    FOR UPDATE SKIP LOCKED
                    LIMIT ?)
                RETURNING id, submission_id, attempt_count, max_attempts
                """,
                (rs, rowNum) ->
                        new ClaimedJob(
                                (UUID) rs.getObject("id"),
                                (UUID) rs.getObject("submission_id"),
                                rs.getInt("attempt_count"),
                                rs.getInt("max_attempts")),
                workerId,
                now,
                leaseExpiresAt,
                now,
                batchSize);
    }

    @Transactional
    public void markRunning(UUID jobId) {
        jdbc.update(
                "UPDATE execution_jobs SET status = 'RUNNING', started_at = ? WHERE id = ?",
                Timestamp.from(clock.instant()),
                jobId);
    }

    /** Renews the lease so a slow-running job isn't reclaimed by the expired-lease sweeper. */
    @Transactional
    public void renewLease(UUID jobId, Duration leaseDuration) {
        jdbc.update(
                "UPDATE execution_jobs SET lease_expires_at = ? WHERE id = ? AND status IN ('CLAIMED', 'RUNNING')",
                Timestamp.from(clock.instant().plus(leaseDuration)),
                jobId);
    }

    @Transactional
    public void complete(UUID jobId) {
        jdbc.update(
                "UPDATE execution_jobs SET status = 'SUCCEEDED', finished_at = ? WHERE id = ?",
                Timestamp.from(clock.instant()),
                jobId);
    }

    /**
     * A run failed. Retries with backoff if attempts remain, otherwise marks the job DEAD (ADR-007:
     * {@code attempt_count >= max_attempts} is terminal, not deleted).
     */
    @Transactional
    public void fail(
            UUID jobId, int attemptCount, int maxAttempts, String error, Duration backoff) {
        Instant now = clock.instant();
        if (attemptCount >= maxAttempts) {
            jdbc.update(
                    "UPDATE execution_jobs SET status = 'DEAD', finished_at = ?, last_error = ? WHERE id = ?",
                    Timestamp.from(now),
                    truncate(error),
                    jobId);
        } else {
            jdbc.update(
                    "UPDATE execution_jobs SET status = 'PENDING', available_at = ?, last_error = ?,"
                            + " claimed_by = NULL, claimed_at = NULL, lease_expires_at = NULL WHERE id = ?",
                    Timestamp.from(now.plus(backoff)),
                    truncate(error),
                    jobId);
        }
    }

    /**
     * Reclaims jobs whose lease expired before the worker finished (crash, OOM-kill, network
     * partition) — never duplicated, since {@code uq_execution_job_submission} guarantees one job
     * per submission (V011).
     */
    @Transactional
    public int reclaimExpiredLeases() {
        Timestamp now = Timestamp.from(clock.instant());
        return jdbc.update(
                """
                UPDATE execution_jobs SET
                    status = CASE WHEN attempt_count >= max_attempts THEN 'DEAD' ELSE 'PENDING' END,
                    finished_at = CASE WHEN attempt_count >= max_attempts THEN ? ELSE finished_at END,
                    available_at = ?, claimed_by = NULL, claimed_at = NULL, lease_expires_at = NULL,
                    last_error = 'lease expired; reclaimed by sweeper'
                WHERE status IN ('CLAIMED', 'RUNNING') AND lease_expires_at < ?
                """,
                now,
                now,
                now);
    }

    private static String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() > 4000 ? error.substring(0, 4000) : error;
    }
}
