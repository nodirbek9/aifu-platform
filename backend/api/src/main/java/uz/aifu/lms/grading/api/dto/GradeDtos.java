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
import uz.aifu.lms.academics.domain.AssessmentComponentKind;
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
            Instant publishedAt,
            @Schema(
                            description =
                                    "The graded work's title in the reader's language, when the"
                                            + " entry came from an activity (student view only)")
                    String title,
            @Schema(
                            description =
                                    "The code of the component the entry counts towards (student"
                                            + " view only)")
                    String componentCode) {}

    @Schema(
            description =
                    "One component of the course's assessment scheme with what the student has"
                            + " earned in it. A component with children earns the sum of its"
                            + " children")
    public record ComponentGradeView(
            UUID id,
            UUID parentComponentId,
            String code,
            AssessmentComponentKind kind,
            BigDecimal maxPoints,
            BigDecimal earned,
            @Schema(
                            description =
                                    "Something in the component (or in one of its children) is graded")
                    boolean graded,
            BigDecimal minPassPercent,
            boolean blocksFinal,
            @Schema(
                            description =
                                    "Whether the earned share reaches minPassPercent; null when"
                                            + " there is no pass mark or nothing is graded yet")
                    Boolean gatePassed) {}

    @Schema(
            description =
                    "A student's grades in one course: the assessment scheme with earned points,"
                            + " the entries that count now, the history, and the derived final"
                            + " score")
    public record EnrollmentGradeSummary(
            UUID enrollmentId,
            UUID courseId,
            BigDecimal finalScore,
            String finalGradeSymbol,
            Boolean admittedToFinal,
            boolean academicDebt,
            Instant finalGradeAt,
            @Schema(
                            description =
                                    "Every top-level component is graded: the final score and its"
                                            + " grade are the result of the course. Until then they"
                                            + " are a running total")
                    boolean complete,
            List<ComponentGradeView> components,
            @Schema(description = "The entries that count now, newest first")
                    List<GradeEntryView> entries,
            @Schema(
                            description =
                                    "Every entry there ever was, newest first: grades are"
                                            + " append-only, a corrected (REVISED) or VOIDED grade"
                                            + " stays here")
                    List<GradeEntryView> history) {}

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
