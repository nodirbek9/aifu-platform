package uz.aifu.lms.execution.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.coding.domain.SubmissionStatus;
import uz.aifu.lms.coding.domain.Verdict;

public final class SubmissionDtos {

    private SubmissionDtos() {}

    public record CreateSubmissionRequest(
            @NotNull UUID codingTaskId,
            @NotBlank String languageCode,
            @NotBlank String sourceCode,
            UUID activityId) {}

    @Schema(
            description =
                    "One test case's outcome — hidden cases never carry stdin/expected/actual output")
    public record TestResultView(
            UUID testCaseId,
            String name,
            boolean isExample,
            Boolean passed,
            String verdict,
            Integer timeMs,
            Integer memoryKb,
            BigDecimal pointsAwarded,
            String stdin,
            String expectedStdout,
            String actualStdout,
            String actualStderr) {}

    @Schema(description = "The caller's own submission, with per-test results")
    public record SubmissionDetail(
            UUID id,
            UUID codingTaskId,
            String languageCode,
            int attemptNumber,
            String sourceCode,
            SubmissionStatus status,
            Verdict verdict,
            BigDecimal score,
            BigDecimal maxScore,
            Integer testsPassed,
            Integer testsTotal,
            Instant submittedAt,
            Instant completedAt,
            String compileOutput,
            boolean outputTruncated,
            List<TestResultView> testResults) {}

    @Schema(description = "Submission summary for list views")
    public record SubmissionSummary(
            UUID id,
            UUID codingTaskId,
            String languageCode,
            int attemptNumber,
            SubmissionStatus status,
            Verdict verdict,
            BigDecimal score,
            BigDecimal maxScore,
            Instant submittedAt,
            Instant completedAt) {}

    @Schema(description = "One student's submission, for a teacher's review roster")
    public record StudentSubmissionSummary(
            UUID id,
            UUID studentUserId,
            String studentFullName,
            String languageCode,
            int attemptNumber,
            SubmissionStatus status,
            Verdict verdict,
            BigDecimal score,
            BigDecimal maxScore,
            Instant submittedAt,
            Instant completedAt) {}
}
