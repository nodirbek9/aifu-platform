package uz.aifu.lms.execution.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.execution.api.dto.RunDtos.RunRequest;
import uz.aifu.lms.execution.api.dto.RunDtos.RunView;
import uz.aifu.lms.execution.app.CodeRunService;

/**
 * "Run" and "Run examples" (product spec S-04.4, S-05.4, S-05.5): execute code in the sandbox
 * without grading it. Not an attempt, never reaches the gradebook.
 */
@RestController
@RequestMapping("/api/v1/runs")
@Tag(name = "Code runs", description = "Run code without grading: custom input or the examples")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('CODE_RUN')")
public class CodeRunController {

    private final CodeRunService runs;
    private final CurrentUserProvider currentUser;

    public CodeRunController(CodeRunService runs, CurrentUserProvider currentUser) {
        this.runs = runs;
        this.currentUser = currentUser;
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping
    @Operation(
            summary = "Queue a run",
            description =
                    "Answers 202 with status QUEUED; poll GET /runs/{id}. 429 when the caller runs"
                            + " too often or already has a run in progress.")
    public ResponseEntity<RunView> run(@Valid @RequestBody RunRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(runs.run(currentUser.require().id(), request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "The caller's own run and, once finished, its output or test results")
    public RunView get(@PathVariable UUID id) {
        return runs.get(id, currentUser.require().id());
    }
}
