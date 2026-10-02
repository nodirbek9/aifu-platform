package uz.aifu.lms.iam.app.access;

import java.util.Set;
import java.util.UUID;

/**
 * Port: "is this user a teacher of this course?".
 *
 * <p>Declared in {@code iam} and implemented in {@code academics} so the dependency points
 * academics → iam. The guard layer must never import an academics repository directly; that would
 * create the cycle {@code ArchitectureBoundaryTest} forbids.
 */
public interface CourseTeacherLookup {

    /**
     * @return true when {@code userId} is assigned to {@code courseId} in any teaching role.
     */
    boolean isTeacherOfCourse(UUID courseId, UUID userId);

    /**
     * @return every course id the user teaches. Empty for non-teachers.
     */
    Set<UUID> courseIdsTaughtBy(UUID userId);

    /**
     * @return true when the course exists and is not soft-deleted.
     */
    boolean courseExists(UUID courseId);

    /**
     * @return true when the course is PUBLISHED (students may only see published courses).
     */
    boolean courseIsPublished(UUID courseId);
}
