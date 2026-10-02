package uz.aifu.lms.execution.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.coding.app.SubmissionService;
import uz.aifu.lms.coding.domain.Submission;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.StudentSubmissionSummary;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.SubmissionDetail;
import uz.aifu.lms.execution.app.ExecutionResultQueryService;
import uz.aifu.lms.execution.domain.ExecutionResult;
import uz.aifu.lms.execution.domain.ExecutionTestResult;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * A teacher's own coding task's submission roster. {@code SUBMISSION_REVIEW} is granted to TEACHER
 * (V003); {@link AccessGuard#requireCodingTaskEditor} is what actually restricts a teacher to a
 * course they are assigned to (skills/authorization/SKILL.md L2). Unlike the student-facing {@link
 * SubmissionController}, hidden test-case output is never redacted here.
 */
@RestController
@RequestMapping("/api/v1/teacher/coding-tasks/{codingTaskId}/submissions")
@Tag(name = "Teacher: submissions", description = "A teacher's own coding task's submission roster")
@SecurityRequirement(name = "bearerAuth")
public class TeacherSubmissionController {

    private final SubmissionService submissions;
    private final ExecutionResultQueryService executionResults;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;

    public TeacherSubmissionController(
            SubmissionService submissions,
            ExecutionResultQueryService executionResults,
            AccessGuard accessGuard,
            PageableSupport pageableSupport) {
        this.submissions = submissions;
        this.executionResults = executionResults;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUBMISSION_REVIEW')")
    @Operation(
            summary =
                    "List every submission to a coding task the caller teaches (or any task, for an admin)")
    public PageResponse<StudentSubmissionSummary> list(
            @PathVariable UUID codingTaskId, Pageable pageable) {
        accessGuard.requireCodingTaskEditor(codingTaskId);
        Page<Submission> page =
                submissions.listForTask(codingTaskId, pageableSupport.sanitize(pageable));
        return PageResponse.of(page, SubmissionMapper::toStudentSummary);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUBMISSION_REVIEW')")
    @Operation(
            summary = "Get one submission with full per-test results, including hidden test output")
    public SubmissionDetail get(@PathVariable UUID codingTaskId, @PathVariable UUID id) {
        accessGuard.requireCodingTaskEditor(codingTaskId);
        Submission submission = submissions.require(id);
        Optional<ExecutionResult> result = executionResults.findResult(submission.getId());
        List<ExecutionTestResult> views =
                result.map(r -> executionResults.listTestResults(r.getId())).orElse(List.of());
        return SubmissionMapper.toDetail(submission, result, views, false);
    }
}
