package uz.aifu.lms.enrollment.app;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentStatus;
import uz.aifu.lms.enrollment.persistence.EnrollmentRepository;
import uz.aifu.lms.iam.app.access.EnrollmentLookup;

/**
 * Real implementation of {@link EnrollmentLookup} — until now an {@code Optional.empty()} in {@code
 * AccessGuard} because {@code enrollment} did not exist. Every "is this caller enrolled in this
 * course" decision in the platform is a SQL-backed check through this adapter (see {@link
 * uz.aifu.lms.academics.app.CourseTeacherLookupAdapter} for the analogous {@code academics} port).
 */
@Component
public class EnrollmentLookupAdapter implements EnrollmentLookup {

    private static final java.util.Set<EnrollmentStatus> ACTIVE_ONLY =
            java.util.Set.of(EnrollmentStatus.ACTIVE);

    private final EnrollmentRepository enrollments;

    public EnrollmentLookupAdapter(EnrollmentRepository enrollments) {
        this.enrollments = enrollments;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> activeEnrollmentId(UUID courseId, UUID studentUserId) {
        return enrollments
                .findFirstByCourse_IdAndStudent_IdAndStatusIn(courseId, studentUserId, ACTIVE_ONLY)
                .map(Enrollment::getId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> latestEnrollmentId(UUID courseId, UUID studentUserId) {
        return enrollments
                .findFirstByCourse_IdAndStudent_IdOrderByAttemptNoDesc(courseId, studentUserId)
                .map(Enrollment::getId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(UUID enrollmentId, UUID studentUserId) {
        return enrollments.existsByIdAndStudent_Id(enrollmentId, studentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> courseIdOf(UUID enrollmentId) {
        return enrollments.findCourseIdById(enrollmentId);
    }
}
