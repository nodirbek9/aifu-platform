package uz.aifu.lms.execution.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.coding.app.SubmissionService;
import uz.aifu.lms.coding.domain.Submission;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.idempotency.IdempotencyKeys;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.CreateSubmissionRequest;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.SubmissionDetail;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.SubmissionSummary;
import uz.aifu.lms.execution.app.ExecutionResultQueryService;
import uz.aifu.lms.execution.app.SubmissionSubmitService;
import uz.aifu.lms.execution.domain.ExecutionResult;
import uz.aifu.lms.execution.domain.ExecutionTestResult;

/**
 * A student's own submissions. Ownership is "the caller's own enrollment" — no {@code AccessGuard},
 * resolved entirely inside {@link SubmissionService} (same reasoning as {@code
 * assessment.api.QuizAttemptController}, ADR-012).
 */
@RestController
@Tag(
        name = "My submissions",
        description = "Creating and reading the caller's own coding submissions")
@SecurityRequirement(name = "bearerAuth")
public class SubmissionController {

    private final SubmissionSubmitService submit;
    private final SubmissionService submissions;
    private final ExecutionResultQueryService executionResults;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;

    public SubmissionController(
            SubmissionSubmitService submit,
            SubmissionService submissions,
            ExecutionResultQueryService executionResults,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser) {
        this.submit = submit;
        this.submissions = submissions;
        this.executionResults = executionResults;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/submissions")
    @PreAuthorize("hasAuthority('SUBMISSION_CREATE')")
    @Operation(summary = "Submit a solution for grading. Idempotency-Key honoured (CLAUDE.md §9).")
    public ResponseEntity<SubmissionDetail> create(
            @RequestBody CreateSubmissionRequest request,
            @RequestHeader(value = IdempotencyKeys.HEADER, required = false)
                    String idempotencyKey) {
        Submission submission =
                submit.submit(
                        request.codingTaskId(),
                        currentUser.require().id(),
                        request.languageCode(),
                        request.sourceCode(),
                        idempotencyKey,
                        request.activityId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetail(submission, true));
    }

    @GetMapping("/api/v1/submissions/{id}")
    @PreAuthorize("hasAuthority('SUBMISSION_SELF_READ')")
    @Operation(
            summary =
                    "Get the caller's own submission, with per-test results (hidden tests redacted)")
    public SubmissionDetail get(@PathVariable UUID id) {
        Submission submission = submissions.require(id, currentUser.require().id());
        return toDetail(submission, true);
    }

    @GetMapping("/api/v1/coding-tasks/{codingTaskId}/submissions/me")
    @PreAuthorize("hasAuthority('SUBMISSION_SELF_READ')")
    @Operation(
            summary =
                    "List the caller's own submissions to one coding task, most recent attempt first")
    public PageResponse<SubmissionSummary> listOwn(
            @PathVariable UUID codingTaskId, Pageable pageable) {
        Page<Submission> page =
                submissions.listOwn(
                        codingTaskId,
                        currentUser.require().id(),
                        pageableSupport.sanitize(pageable));
        return PageResponse.of(page, SubmissionMapper::toSummary);
    }

    private SubmissionDetail toDetail(Submission submission, boolean redactHidden) {
        Optional<ExecutionResult> result = executionResults.findResult(submission.getId());
        List<ExecutionTestResult> views =
                result.map(r -> executionResults.listTestResults(r.getId())).orElse(List.of());
        return SubmissionMapper.toDetail(submission, result, views, redactHidden);
    }
}
