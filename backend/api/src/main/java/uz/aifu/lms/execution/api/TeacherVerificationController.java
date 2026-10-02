package uz.aifu.lms.execution.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.execution.api.dto.RunDtos.VerificationView;
import uz.aifu.lms.execution.app.CodeRunService;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * "Verify solutions" in the coding task editor (product spec T-05): runs every reference solution
 * of a task against all of its tests, so a broken task is caught before students meet it.
 */
@RestController
@RequestMapping("/api/v1/teacher/coding-tasks/{codingTaskId}/verification")
@Tag(
        name = "Teacher: verify coding task solutions",
        description = "Check the reference solutions against the task's tests")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
public class TeacherVerificationController {

    private final CodeRunService runs;
    private final AccessGuard accessGuard;
    private final CurrentUserProvider currentUser;

    public TeacherVerificationController(
            CodeRunService runs, AccessGuard accessGuard, CurrentUserProvider currentUser) {
        this.runs = runs;
        this.accessGuard = accessGuard;
        this.currentUser = currentUser;
    }

    @PostMapping
    @Operation(
            summary = "Run every reference solution against all tests",
            description = "Answers 202; poll the GET until every run is COMPLETED or FAILED.")
    public ResponseEntity<VerificationView> verify(@PathVariable UUID codingTaskId) {
        accessGuard.requireCodingTaskEditor(codingTaskId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(runs.verify(codingTaskId, currentUser.require().id()));
    }

    @GetMapping
    @Operation(summary = "The latest verification of each language's solution")
    public VerificationView latest(@PathVariable UUID codingTaskId) {
        accessGuard.requireCodingTaskEditor(codingTaskId);
        return runs.verification(codingTaskId);
    }
}
