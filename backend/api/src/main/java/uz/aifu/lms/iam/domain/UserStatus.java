package uz.aifu.lms.iam.domain;

/** Account lifecycle. Checked on login AND on refresh, so a suspension takes effect quickly. */
public enum UserStatus {
    /** Created but not yet activated; cannot authenticate. */
    PENDING,
    ACTIVE,
    /** Temporarily blocked by an administrator. */
    SUSPENDED,
    /** Permanently deactivated; kept for historical integrity (grades, submissions). */
    DISABLED;

    public boolean canAuthenticate() {
        return this == ACTIVE;
    }
}
