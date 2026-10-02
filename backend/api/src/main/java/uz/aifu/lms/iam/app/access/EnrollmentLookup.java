package uz.aifu.lms.iam.app.access;

import java.util.Optional;
import java.util.UUID;

/**
 * Port: enrollment membership questions, implemented in {@code enrollment}.
 *
 * <p>Every student-owned row in the platform hangs off an {@code enrollment_id}. Resolving the
 * caller's enrollment here, once, is what makes cross-student access structurally impossible
 * downstream: services receive an enrollment id that was derived from the authenticated principal,
 * never one supplied by the client.
 */
public interface EnrollmentLookup {

    /** The caller's single ACTIVE enrollment in a course, if any. */
    Optional<UUID> activeEnrollmentId(UUID courseId, UUID studentUserId);

    /** Any enrollment (including completed/withdrawn) the student holds for the course. */
    Optional<UUID> latestEnrollmentId(UUID courseId, UUID studentUserId);

    /**
     * @return true when the enrollment belongs to this student.
     */
    boolean isOwner(UUID enrollmentId, UUID studentUserId);

    /** The course an enrollment belongs to, or empty when the enrollment does not exist. */
    Optional<UUID> courseIdOf(UUID enrollmentId);
}
