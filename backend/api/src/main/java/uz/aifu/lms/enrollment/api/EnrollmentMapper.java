package uz.aifu.lms.enrollment.api;

import java.util.List;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentDetail;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentEventView;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentSummary;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentEvent;

/** Shared entity-to-DTO mapping for the enrollment controllers. */
final class EnrollmentMapper {

    private EnrollmentMapper() {}

    static EnrollmentSummary toSummary(Enrollment e) {
        return new EnrollmentSummary(
                e.getId(),
                e.getCourse().getId(),
                e.getCourse().getSubject().getCode(),
                e.getCourse().getCohortCode(),
                e.getCourse().getTerm().getName(),
                e.getStudent().getId(),
                e.getStudent().getFullName(),
                e.getStatus(),
                e.getAttemptNo(),
                e.getEnrolledAt());
    }

    static EnrollmentDetail toDetail(Enrollment e, EnrollmentService enrollments) {
        List<EnrollmentEventView> events =
                enrollments.listEvents(e.getId()).stream()
                        .map(EnrollmentMapper::toEventView)
                        .toList();
        return new EnrollmentDetail(
                e.getId(),
                e.getCourse().getId(),
                e.getCourse().getSubject().getCode(),
                e.getStudent().getId(),
                e.getStudent().getFullName(),
                e.getStatus(),
                e.getAttemptNo(),
                e.getSource(),
                e.getEnrolledAt(),
                e.getStartsOn(),
                e.getCompletedAt(),
                e.getWithdrawnAt(),
                e.getFinalScore(),
                e.getFinalGradeSymbol(),
                e.getAdmittedToFinal(),
                e.isAcademicDebt(),
                events);
    }

    private static EnrollmentEventView toEventView(EnrollmentEvent event) {
        return new EnrollmentEventView(
                event.getEventType(),
                event.getFromStatus(),
                event.getToStatus(),
                event.getReason(),
                event.getOccurredAt());
    }
}
