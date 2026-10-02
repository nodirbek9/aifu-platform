package uz.aifu.lms.progress.domain;

/**
 * {@code activity_progress}'s own status set (V008) — wider than {@link ProgressStatus} because an
 * activity attempt can be {@code SUBMITTED} awaiting grading, or {@code FAILED} outright, states
 * that course/module/lesson progress never reach.
 */
public enum ActivityProgressStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SUBMITTED,
    COMPLETED,
    FAILED
}
