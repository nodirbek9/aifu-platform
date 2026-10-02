package uz.aifu.lms.academics.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import uz.aifu.lms.academics.domain.AssessmentComponentKind;
import uz.aifu.lms.academics.domain.ComponentAssessmentForm;
import uz.aifu.lms.academics.domain.CourseFinalAssessmentForm;
import uz.aifu.lms.academics.domain.CourseStatus;
import uz.aifu.lms.academics.domain.DeliveryMode;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.academics.domain.TeacherRole;

public final class CourseDtos {

    private CourseDtos() {}

    @Schema(description = "Create one delivery of a subject in a term. Admin only.")
    public record CreateCourseRequest(
            @NotNull UUID subjectId,
            @NotNull UUID termId,
            @Size(max = 32) String cohortCode,
            @NotNull @DecimalMin("0.1") @DecimalMax("60") BigDecimal credits,
            @Min(1) int totalHours,
            @Schema(example = "uz") String instructionLanguage,
            String gradeScaleCode) {}

    public record UpdateCourseDeliveryRequest(
            @Size(max = 32) String cohortCode,
            BigDecimal weeklyContactHours,
            DeliveryMode deliveryMode,
            CourseFinalAssessmentForm assessmentForm,
            UUID programmeId) {}

    public record UpdateRequiredLocalesRequest(@NotEmpty Set<String> locales) {}

    public record UpdateEnrollmentWindowRequest(
            Instant enrollmentOpensAt, Instant enrollmentClosesAt, Integer maxEnrollment) {}

    public record UpsertCourseTranslationRequest(
            @NotBlank @Size(max = 500) String title, String description) {}

    public record AssignTeacherRequest(@NotNull UUID teacherUserId, TeacherRole teacherRole) {}

    public record SetSessionAllocationRequest(
            @NotNull SessionType sessionType, @Min(0) int allocatedHours) {}

    public record CreateAssessmentComponentRequest(
            UUID parentComponentId,
            @NotBlank @Size(max = 32) String code,
            @NotNull AssessmentComponentKind kind,
            @NotNull @DecimalMin("0.01") BigDecimal maxPoints,
            @DecimalMin("0") @DecimalMax("100") BigDecimal minPassPercent,
            boolean blocksFinal,
            ComponentAssessmentForm assessmentForm,
            int displayOrder) {}

    @Schema(description = "Course summary for catalogue lists")
    public record CourseSummary(
            UUID id,
            String subjectCode,
            String title,
            String termName,
            String cohortCode,
            BigDecimal credits,
            int totalHours,
            CourseStatus status) {}

    @Schema(description = "Full course detail")
    public record CourseDetail(
            UUID id,
            UUID subjectId,
            String subjectCode,
            UUID termId,
            String termName,
            String cohortCode,
            BigDecimal credits,
            int totalHours,
            BigDecimal weeklyContactHours,
            String instructionLanguage,
            Set<String> requiredLocales,
            DeliveryMode deliveryMode,
            CourseFinalAssessmentForm assessmentForm,
            CourseStatus status,
            Instant enrollmentOpensAt,
            Instant enrollmentClosesAt,
            Integer maxEnrollment,
            Instant publishedAt,
            List<CourseTranslationView> translations,
            List<TeacherView> teachers,
            List<SessionAllocationView> sessionAllocations,
            List<AssessmentComponentView> assessmentComponents) {}

    public record CourseTranslationView(String locale, String title, String description) {}

    public record TeacherView(UUID userId, String fullName, TeacherRole teacherRole) {}

    public record SessionAllocationView(SessionType sessionType, int allocatedHours) {}

    public record AssessmentComponentView(
            UUID id,
            UUID parentComponentId,
            String code,
            AssessmentComponentKind kind,
            BigDecimal maxPoints,
            BigDecimal minPassPercent,
            boolean blocksFinal,
            ComponentAssessmentForm assessmentForm,
            int displayOrder) {}
}
