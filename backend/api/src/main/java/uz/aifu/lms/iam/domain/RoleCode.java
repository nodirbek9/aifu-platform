package uz.aifu.lms.iam.domain;

/**
 * Convenience constants for the roles that exist today.
 *
 * <p>This is NOT the authorization model: authorization is permission-based (ADR-004), and new
 * roles such as {@code CURATOR} or {@code ASSISTANT_TEACHER} are added as DATA in the {@code roles}
 * table with no Java change. Never write {@code hasRole(...)} in a controller.
 */
public final class RoleCode {
    public static final String ADMIN = "ADMIN";
    public static final String TEACHER = "TEACHER";
    public static final String STUDENT = "STUDENT";

    private RoleCode() {}
}
