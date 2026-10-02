package uz.aifu.lms.coding.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.coding.domain.ComparisonMode;
import uz.aifu.lms.coding.domain.ScoringStrategy;
import uz.aifu.lms.coding.domain.TestCaseVisibility;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.curriculum.domain.Difficulty;

public final class CodingTaskDtos {

    private CodingTaskDtos() {}

    public record CreateCodingTaskRequest(String code) {}

    public record UpdateCodingTaskRequest(
            Difficulty difficulty,
            @NotNull @Positive BigDecimal maxPoints,
            ScoringStrategy scoringStrategy,
            Integer maxAttempts,
            Integer cpuTimeLimitMs,
            Integer wallTimeLimitMs,
            Integer memoryLimitKb) {}

    public record ChangeCodingTaskStatusRequest(@NotNull PublicationStatus status) {}

    public record UpsertCodingTaskTranslationRequest(
            String title,
            String description,
            String instructions,
            String constraintsText,
            String explanation) {}

    public record UpsertLanguageRequest(String starterCode, String solutionCode) {}

    public record CreateHintRequest(BigDecimal penaltyPercent) {}

    public record UpsertHintTranslationRequest(String body) {}

    public record CreateTestCaseRequest(String stdin, String expectedStdout) {}

    public record UpdateTestCaseRequest(
            String name,
            TestCaseVisibility visibility,
            boolean isExample,
            @NotNull @Positive BigDecimal weight,
            String stdin,
            String expectedStdout,
            ComparisonMode comparisonMode,
            BigDecimal numericTolerance,
            Integer cpuTimeLimitMs) {}

    @Schema(description = "Coding task summary for list views")
    public record CodingTaskSummary(
            UUID id,
            UUID courseId,
            String code,
            Difficulty difficulty,
            PublicationStatus status,
            BigDecimal maxPoints,
            String title,
            boolean titleIsFallback) {}

    public record LanguageView(
            String code, String displayName, String versionLabel, String starterCode) {}

    @Schema(
            description =
                    "Teacher view of one language — includes solutionCode. taskLanguageId is the id "
                            + "that PATCH/DELETE /teacher/coding-task-languages/{taskLanguageId} take.")
    public record LanguageTeacherView(
            String code,
            String displayName,
            String versionLabel,
            String starterCode,
            String solutionCode,
            UUID taskLanguageId) {}

    public record HintView(UUID id, int position, BigDecimal penaltyPercent, String body) {}

    @Schema(description = "A hint as a student sees it: body is null until the hint is opened")
    public record StudentHintView(UUID id, int position, boolean revealed, String body) {}

    public record ProgrammingLanguageView(
            String code, String displayName, String versionLabel, String sourceFilename) {}

    @Schema(description = "Teacher view of one test case — includes expectedStdout even if hidden")
    public record TestCaseView(
            UUID id,
            int position,
            String name,
            TestCaseVisibility visibility,
            boolean isExample,
            BigDecimal weight,
            String stdin,
            String expectedStdout,
            ComparisonMode comparisonMode,
            BigDecimal numericTolerance,
            Integer cpuTimeLimitMs) {}

    @Schema(
            description =
                    "Student-facing test case — hidden cases carry neither stdin nor expectedStdout")
    public record TestCaseStudentView(
            UUID id,
            int position,
            String name,
            boolean isExample,
            String stdin,
            String expectedStdout) {}

    @Schema(
            description =
                    "Full teacher-facing coding task detail — includes solution code and hidden test cases")
    public record CodingTaskDetail(
            UUID id,
            UUID courseId,
            String code,
            Difficulty difficulty,
            PublicationStatus status,
            BigDecimal maxPoints,
            ScoringStrategy scoringStrategy,
            Integer maxAttempts,
            Integer cpuTimeLimitMs,
            Integer wallTimeLimitMs,
            Integer memoryLimitKb,
            Instant publishedAt,
            String title,
            String description,
            String instructions,
            String constraintsText,
            String explanation,
            boolean titleIsFallback,
            List<LanguageTeacherView> languages,
            List<HintView> hints,
            List<TestCaseView> testCases) {}

    @Schema(
            description =
                    "Student-facing coding task detail — no solution code, hidden test cases redacted")
    public record CodingTaskStudentDetail(
            UUID id,
            UUID courseId,
            String code,
            Difficulty difficulty,
            BigDecimal maxPoints,
            Integer maxAttempts,
            Integer cpuTimeLimitMs,
            Integer wallTimeLimitMs,
            Integer memoryLimitKb,
            String title,
            String description,
            String instructions,
            String constraintsText,
            boolean titleIsFallback,
            List<LanguageView> languages,
            List<TestCaseStudentView> exampleTestCases) {}
}
