package uz.aifu.lms.enrollment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.enrollment.domain.EnrollmentEventType;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.enrollment.domain.EnrollmentStatus;

public final class EnrollmentDtos {

    private EnrollmentDtos() {}

    @Schema(description = "Enroll one student in a course. Admin only.")
    public record CreateEnrollmentRequest(
            @NotNull UUID courseId, @NotNull UUID studentUserId, EnrollmentSource source) {}

    @Schema(
            description =
                    "Enroll a batch of students; each student's outcome is reported independently.")
    public record BulkEnrollRequest(
            @NotNull UUID courseId, @NotEmpty List<UUID> studentUserIds, EnrollmentSource source) {}

    @Schema(description = "Enroll every student of a group into a course (product spec A-06).")
    public record GroupEnrollRequest(
            @NotNull UUID courseId,
            @Schema(example = "BK-DI26/1") @NotBlank String groupCode,
            EnrollmentSource source) {}

    public record BulkEnrollResultView(
            UUID studentUserId,
            boolean success,
            UUID enrollmentId,
            String errorCode,
            String errorMessage) {}

    @Schema(
            description =
                    "One row of a CSV roster import — identifier is whatever the registrar's export"
                            + " put on that line (a student number).")
    public record CsvImportRowView(
            String identifier,
            boolean success,
            UUID studentUserId,
            UUID enrollmentId,
            String errorCode,
            String errorMessage) {}

    @Schema(description = "Enrollment summary for roster/list views")
    public record EnrollmentSummary(
            UUID id,
            UUID courseId,
            String subjectCode,
            String cohortCode,
            String termName,
            UUID studentUserId,
            String studentFullName,
            EnrollmentStatus status,
            int attemptNo,
            Instant enrolledAt) {}

    @Schema(description = "Full enrollment detail, including transition history")
    public record EnrollmentDetail(
            UUID id,
            UUID courseId,
            String subjectCode,
            UUID studentUserId,
            String studentFullName,
            EnrollmentStatus status,
            int attemptNo,
            EnrollmentSource source,
            Instant enrolledAt,
            LocalDate startsOn,
            Instant completedAt,
            Instant withdrawnAt,
            BigDecimal finalScore,
            String finalGradeSymbol,
            Boolean admittedToFinal,
            boolean academicDebt,
            List<EnrollmentEventView> events) {}

    public record EnrollmentEventView(
            EnrollmentEventType eventType,
            EnrollmentStatus fromStatus,
            EnrollmentStatus toStatus,
            String reason,
            Instant occurredAt) {}
}
