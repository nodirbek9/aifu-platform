package uz.aifu.lms.enrollment.domain;

/** How an enrollment came to exist. Matches {@code enrollments.source} (V006). */
public enum EnrollmentSource {
    ADMIN,
    SELF,
    IMPORT,
    AUTOMATIC
}
