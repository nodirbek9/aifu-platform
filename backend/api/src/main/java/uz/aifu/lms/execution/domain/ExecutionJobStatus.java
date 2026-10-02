package uz.aifu.lms.execution.domain;

/**
 * ADR-007's queue lifecycle. The API only ever writes {@code PENDING}; the worker owns the rest.
 */
public enum ExecutionJobStatus {
    PENDING,
    CLAIMED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    DEAD,
    CANCELLED
}
