package uz.aifu.lms.common.idempotency;

/**
 * Shared constant for the {@code Idempotency-Key} request header (CLAUDE.md §9: honoured on {@code
 * POST /submissions} and {@code POST /quiz-attempts/*}/submit`). Each resource stores the key on
 * its own row (e.g. {@code quiz_attempts.idempotency_key}) and compares it on a retry — a generic
 * dedup service is not needed since "was this exact mutation already applied" is always a question
 * about that resource's own state, not a cross-cutting one.
 */
public final class IdempotencyKeys {

    public static final String HEADER = "Idempotency-Key";

    private IdempotencyKeys() {}
}
