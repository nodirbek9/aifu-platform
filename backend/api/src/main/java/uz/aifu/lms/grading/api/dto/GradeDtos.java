package uz.aifu.lms.grading.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.grading.domain.GradeEntryStatus;
import uz.aifu.lms.grading.domain.GradeSource;
import uz.aifu.lms.grading.domain.GradeSourceRefType;

public final class GradeDtos {

    private GradeDtos() {}

    public record RecordManualGradeRequest(
            @NotNull UUID assessmentComponentId,
            @NotNull @PositiveOrZero BigDecimal score,
            @NotNull @Positive BigDecimal maxScore,
            UUID evaluatorUserId) {}

    public record VoidGradeRequest(@NotBlank String reason) {}

    @Schema(description = "One append-only gradebook row")
    public record GradeEntryView(
            UUID id,
            UUID assessmentComponentId,
            UUID activityId,
            GradeSource source,
            GradeSourceRefType sourceRefType,
            UUID sourceRefId,
            BigDecimal score,
            BigDecimal maxScore,
            GradeEntryStatus status,
            int revision,
            UUID supersedesId,
            String voidReason,
            Instant evaluatedAt,
            Instant publishedAt) {}

    @Schema(description = "One enrollment's current published grade entries + derived final score")
    public record EnrollmentGradeSummary(
            UUID enrollmentId,
            BigDecimal finalScore,
            String finalGradeSymbol,
            Boolean admittedToFinal,
            boolean academicDebt,
            Instant finalGradeAt,
            List<GradeEntryView> entries) {}

    @Schema(description = "One student's grade summary, for a teacher's course roster")
    public record StudentGradeSummary(
            UUID enrollmentId,
            UUID studentUserId,
            String studentFullName,
            BigDecimal finalScore,
            String finalGradeSymbol,
            Boolean admittedToFinal,
            boolean academicDebt) {}
}
