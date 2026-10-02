package uz.aifu.lms.audit.domain;

/**
 * Outcome of an audited attempt.
 *
 * <p>Mirrors the CHECK constraint on {@code audit_log.outcome} (V013). Keep the two in sync.
 */
public enum AuditOutcome {
    /** The action completed. */
    SUCCESS,
    /** The action was attempted and failed for a non-authorization reason. */
    FAILURE,
    /** The action was refused by authorization. */
    DENIED
}
