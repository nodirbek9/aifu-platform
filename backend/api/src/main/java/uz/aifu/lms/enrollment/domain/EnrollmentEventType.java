package uz.aifu.lms.enrollment.domain;

/** Matches {@code enrollment_events.event_type} CHECK constraint (V006). */
public enum EnrollmentEventType {
    CREATED,
    ACTIVATED,
    SUSPENDED,
    RESUMED,
    COMPLETED,
    FAILED,
    WITHDRAWN,
    GRADE_FINALIZED,
    REINSTATED
}
