package uz.aifu.lms.execution.domain;

/** Lifecycle of an ungraded run (V022). */
public enum CodeRunStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    /** The platform could not run it (sandbox trouble, or no worker picked it up). */
    FAILED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
