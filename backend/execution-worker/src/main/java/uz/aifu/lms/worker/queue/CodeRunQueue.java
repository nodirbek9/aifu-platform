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
import uz.aifu.lms.worker.domain.ClaimedRun;

/**
 * {@code code_runs} claim/lease mechanics (V022) — the same pattern as {@link ExecutionJobQueue},
 * on the run row itself: an ungraded run has no separate job row.
 */
@Component
public class CodeRunQueue {

    private final JdbcTemplate jdbc;
    private final Clock clock;

    public CodeRunQueue(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * Claims up to {@code batchSize} queued runs, oldest first, skipping rows another worker holds.
     */
    @Transactional
    public List<ClaimedRun> claim(String workerId, int batchSize, Duration leaseDuration) {
        Timestamp now = Timestamp.from(clock.instant());
        Timestamp leaseExpiresAt = Timestamp.from(clock.instant().plus(leaseDuration));
        return jdbc.query(
                """
                UPDATE code_runs SET status = 'RUNNING', claimed_by = ?, started_at = ?,
                    lease_expires_at = ?, attempt_count = attempt_count + 1
                WHERE id IN (
                    SELECT id FROM code_runs
                    WHERE status = 'QUEUED' AND available_at <= ?
                    ORDER BY available_at
                    FOR UPDATE SKIP LOCKED
                    LIMIT ?)
                RETURNING id, coding_task_id, language_code, source_code, mode, stdin, attempt_count
                """,
                (rs, rowNum) ->
                        new ClaimedRun(
                                (UUID) rs.getObject("id"),
                                (UUID) rs.getObject("coding_task_id"),
                                rs.getString("language_code"),
                                rs.getString("source_code"),
                                rs.getString("mode"),
                                rs.getString("stdin"),
                                rs.getInt("attempt_count")),
                workerId,
                now,
                leaseExpiresAt,
                now,
                batchSize);
    }

    /** Extends the lease while a many-test run is still going. */
    @Transactional
    public void renewLease(UUID runId, Duration leaseDuration) {
        jdbc.update(
                "UPDATE code_runs SET lease_expires_at = ? WHERE id = ? AND status = 'RUNNING'",
                Timestamp.from(clock.instant().plus(leaseDuration)),
                runId);
    }

    /**
     * The platform failed to run it (not the author's fault). Goes back to the queue while attempts
     * remain; otherwise the run is FAILED with the error, so its author stops waiting.
     */
    @Transactional
    public void fail(
            UUID runId, int attemptCount, int maxAttempts, String error, Duration backoff) {
        Instant now = clock.instant();
        if (attemptCount >= maxAttempts) {
            jdbc.update(
                    """
                    UPDATE code_runs SET status = 'FAILED', verdict = 'INTERNAL_ERROR',
                        internal_error = ?, completed_at = ?, claimed_by = NULL,
                        lease_expires_at = NULL
                    WHERE id = ? AND status = 'RUNNING'
                    """,
                    truncate(error),
                    Timestamp.from(now),
                    runId);
        } else {
            jdbc.update(
                    """
                    UPDATE code_runs SET status = 'QUEUED', available_at = ?, internal_error = ?,
                        claimed_by = NULL, lease_expires_at = NULL
                    WHERE id = ? AND status = 'RUNNING'
                    """,
                    Timestamp.from(now.plus(backoff)),
                    truncate(error),
                    runId);
        }
    }

    /** Puts runs whose worker died back in the queue, or fails them when attempts ran out. */
    @Transactional
    public int reclaimExpiredLeases(int maxAttempts) {
        Timestamp now = Timestamp.from(clock.instant());
        return jdbc.update(
                """
                UPDATE code_runs SET
                    status = CASE WHEN attempt_count >= ? THEN 'FAILED' ELSE 'QUEUED' END,
                    verdict = CASE WHEN attempt_count >= ? THEN 'INTERNAL_ERROR' ELSE verdict END,
                    completed_at = CASE WHEN attempt_count >= ? THEN ? ELSE completed_at END,
                    available_at = ?, claimed_by = NULL, lease_expires_at = NULL,
                    internal_error = 'lease expired; reclaimed by sweeper'
                WHERE status = 'RUNNING' AND lease_expires_at < ?
                """,
                maxAttempts,
                maxAttempts,
                maxAttempts,
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
