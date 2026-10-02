package uz.aifu.lms.files.domain;

/** Mirrors the {@code files.status} CHECK constraint (V013). */
public enum FileStatus {
    ACTIVE,
    DELETED,
    QUARANTINED
}
