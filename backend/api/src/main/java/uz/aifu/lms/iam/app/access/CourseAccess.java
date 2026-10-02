package uz.aifu.lms.iam.app.access;

import java.util.Optional;
import java.util.UUID;
import uz.aifu.lms.common.security.AuthenticatedUser;

/**
 * The resolved answer to "how may this caller see this course?".
 *
 * <p>Passed down into services and mappers so that visibility decisions (draft content, hidden test
 * cases, reference solutions, other students' records) are made from one authoritative object
 * rather than re-derived ad hoc. {@link #enrollmentId()} is present only for students.
 */
public record CourseAccess(
        UUID courseId,
        ViewerRole viewerRole,
        UUID userId,
        UUID enrollmentId,
        boolean coursePublished) {

    public static CourseAccess admin(UUID courseId, AuthenticatedUser user, boolean published) {
        return new CourseAccess(courseId, ViewerRole.ADMIN, user.id(), null, published);
    }

    public static CourseAccess teacher(UUID courseId, AuthenticatedUser user, boolean published) {
        return new CourseAccess(courseId, ViewerRole.TEACHER, user.id(), null, published);
    }

    public static CourseAccess student(
            UUID courseId, AuthenticatedUser user, UUID enrollmentId, boolean published) {
        return new CourseAccess(courseId, ViewerRole.STUDENT, user.id(), enrollmentId, published);
    }

    /**
     * True for admins and assigned teachers: the only viewers allowed to see unpublished content.
     */
    public boolean canSeeDrafts() {
        return viewerRole != ViewerRole.STUDENT;
    }

    /**
     * True for admins and assigned teachers. Gates reference solutions, hidden test-case
     * expectations, internal feedback and other students' records.
     */
    public boolean canSeeAuthoringSecrets() {
        return viewerRole != ViewerRole.STUDENT;
    }

    public boolean isStudent() {
        return viewerRole == ViewerRole.STUDENT;
    }

    public Optional<UUID> enrollment() {
        return Optional.ofNullable(enrollmentId);
    }

    /** The caller's enrollment, or a failure when the caller is not a student. */
    public UUID requireEnrollment() {
        if (enrollmentId == null) {
            throw new IllegalStateException(
                    "requireEnrollment() called for a "
                            + viewerRole
                            + " viewer of course "
                            + courseId);
        }
        return enrollmentId;
    }
}
