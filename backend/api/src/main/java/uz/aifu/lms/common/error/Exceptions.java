package uz.aifu.lms.common.error;

import java.util.UUID;

/**
 * Factories for the common failures, so throw sites stay one readable line.
 *
 * <p>Note {@link #notFound}: an unauthorized read of another user's resource must also use this,
 * returning 404 rather than 403, so the API does not leak the existence of other people's data
 * (ADR-004).
 */
public final class Exceptions {

    private Exceptions() {}

    public static DomainException notFound(String type, Object id) {
        return new DomainException(ErrorCode.RESOURCE_NOT_FOUND, type + " not found")
                .with("resourceType", type)
                .with("resourceId", String.valueOf(id));
    }

    public static DomainException forbidden(String message) {
        return new DomainException(ErrorCode.AUTH_FORBIDDEN, message);
    }

    public static DomainException notEnrolled(UUID courseId) {
        return new DomainException(ErrorCode.NOT_ENROLLED, "You are not enrolled in this course")
                .with("courseId", courseId.toString());
    }

    public static DomainException notCourseTeacher(UUID courseId) {
        return new DomainException(
                        ErrorCode.NOT_COURSE_TEACHER,
                        "You are not assigned as a teacher of this course")
                .with("courseId", courseId.toString());
    }

    public static DomainException conflict(ErrorCode code, String message) {
        return new DomainException(code, message);
    }

    public static DomainException rule(ErrorCode code, String message) {
        return new DomainException(code, message);
    }

    public static DomainException validation(String message) {
        return new DomainException(ErrorCode.VALIDATION_FAILED, message);
    }

    public static DomainException internal(String message, Throwable cause) {
        return new DomainException(ErrorCode.INTERNAL_ERROR, message, cause);
    }
}
