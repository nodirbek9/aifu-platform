package uz.aifu.lms.execution.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.execution.domain.CodeRunMode;
import uz.aifu.lms.execution.domain.CodeRunStatus;
import uz.aifu.lms.execution.domain.CodeRunVerdict;

/** Request and response shapes of ungraded runs. */
public final class RunDtos {

    private RunDtos() {}

    @Schema(
            description =
                    "Run code without grading. CUSTOM (default) runs it with the given stdin; with no"
                            + " codingTaskId it is a free run (lesson examples, playground). EXAMPLES"
                            + " needs a codingTaskId and runs the task's example tests.")
    public record RunRequest(
            UUID codingTaskId,
            @Schema(example = "python") @NotBlank String languageCode,
            @NotBlank String sourceCode,
            @Schema(description = "Standard input for a CUSTOM run") String stdin,
            @Schema(allowableValues = {"CUSTOM", "EXAMPLES"}) CodeRunMode mode) {}

    @Schema(description = "One test of an EXAMPLES or VERIFY run.")
    public record RunTestView(
            UUID testCaseId,
            int position,
            String name,
            boolean passed,
            String verdict,
            Integer timeMs,
            Integer memoryKb,
            String stdin,
            String expectedStdout,
            String actualStdout,
            String actualStderr) {}

    @Schema(
            description =
                    "Poll until status is COMPLETED or FAILED. CUSTOM: stdout / stderr / exitCode."
                            + " EXAMPLES and VERIFY: tests, testsPassed, testsTotal.")
    public record RunView(
            UUID id,
            UUID codingTaskId,
            String languageCode,
            CodeRunMode mode,
            CodeRunStatus status,
            @Schema(
                            description =
                                    "OK = a custom run finished normally; ACCEPTED = every test"
                                            + " passed; otherwise what went wrong first")
                    CodeRunVerdict verdict,
            String compileOutput,
            String stdout,
            String stderr,
            Integer exitCode,
            Integer timeMs,
            Integer memoryKb,
            Integer testsPassed,
            Integer testsTotal,
            @Schema(description = "Set when the platform itself failed to run the code")
                    String error,
            Instant createdAt,
            Instant completedAt,
            List<RunTestView> tests) {}

    @Schema(description = "The latest check of each language's reference solution (spec T-05).")
    public record VerificationView(
            UUID codingTaskId,
            @Schema(description = "Languages of the task that have no reference solution")
                    List<String> languagesWithoutSolution,
            @Schema(description = "true when every language has a solution and all of them pass")
                    boolean allPassed,
            List<RunView> runs) {}
}
