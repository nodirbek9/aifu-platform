package uz.aifu.lms.enrollment.domain;

/**
 * {@code enrollments.status} lifecycle (docs/domain/domain-model.md §4):
 *
 * <pre>
 * PENDING ─► ACTIVE ─► COMPLETED | FAILED
 *             ├─► SUSPENDED ─► ACTIVE
 *             └─► WITHDRAWN            (terminal; history kept in enrollment_events)
 * </pre>
 *
 * {@code WITHDRAWN} and {@code FAILED} may additionally be {@code REINSTATED} back to {@code
 * ACTIVE} by an admin correcting a mistaken transition — distinct from a student re-enrolling,
 * which creates a new row ({@code attempt_no + 1}) rather than resurrecting this one. {@code
 * COMPLETED} has no outgoing transition: it implies grade finalization (ADR-011) and stays
 * genuinely terminal.
 */
public enum EnrollmentStatus {
    /**
     * No Phase 4 code path creates a row in this state yet — {@code EnrollmentService.enroll}
     * always creates {@code ACTIVE} directly, matching the DB column default. Kept (with {@link
     * Enrollment#activate()}) because it is part of the documented state machine and the schema's
     * legal value set; a future SELF/IMPORT registration flow is the intended producer.
     */
    PENDING,
    ACTIVE,
    SUSPENDED,
    COMPLETED,
    FAILED,
    WITHDRAWN;

    /**
     * Matches the partial unique index {@code uq_enrollment_active} (V006): at most one
     * non-terminal enrollment per (course, student).
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == WITHDRAWN;
    }
}
