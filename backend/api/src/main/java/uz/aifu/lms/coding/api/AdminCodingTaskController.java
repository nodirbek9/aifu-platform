package uz.aifu.lms.coding.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.ChangeCodingTaskStatusRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CodingTaskDetail;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CreateCodingTaskRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpdateCodingTaskRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpsertCodingTaskTranslationRequest;
import uz.aifu.lms.coding.app.CodingTaskContentService;
import uz.aifu.lms.coding.app.CodingTaskService;
import uz.aifu.lms.coding.domain.CodingTask;
import uz.aifu.lms.coding.domain.CodingTaskHint;
import uz.aifu.lms.coding.domain.CodingTaskHintTranslation;
import uz.aifu.lms.coding.domain.CodingTaskLanguage;
import uz.aifu.lms.coding.domain.CodingTaskTestCase;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * Coding task administration. {@code CODING_TASK_MANAGE} is granted to TEACHER (V003), and {@link
 * AccessGuard#requireCourseEditor}/{@link AccessGuard#requireCodingTaskEditor} are what actually
 * restrict a teacher to a course they are assigned to (skills/authorization/SKILL.md L2).
 */
@RestController
@Tag(name = "Teacher: coding tasks", description = "Coding task administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminCodingTaskController {

    private final CodingTaskService codingTasks;
    private final CodingTaskContentService content;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;

    public AdminCodingTaskController(
            CodingTaskService codingTasks,
            CodingTaskContentService content,
            AccessGuard accessGuard,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser) {
        this.codingTasks = codingTasks;
        this.content = content;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/coding-tasks")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "List all of a course's coding tasks, any status")
    public PageResponse<CodingTaskDetail> list(@PathVariable UUID courseId, Pageable pageable) {
        accessGuard.requireCourseEditor(courseId);
        Page<CodingTask> page =
                codingTasks.listByCourse(courseId, pageableSupport.sanitize(pageable));
        return PageResponse.of(page, this::toDetail);
    }

    @PostMapping("/api/v1/teacher/courses/{courseId}/coding-tasks")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Create a coding task")
    public ResponseEntity<CodingTaskDetail> create(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateCodingTaskRequest request,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(courseId);
        CodingTask created =
                codingTasks.create(
                        courseId, request, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/v1/teacher/coding-tasks/" + created.getId()))
                .body(toDetail(created));
    }

    @GetMapping("/api/v1/teacher/coding-tasks/{id}")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Get one coding task, any status, with solution code and hidden tests")
    public CodingTaskDetail get(@PathVariable UUID id) {
        CodingTask task = requireEditable(id);
        return toDetail(task);
    }

    @PatchMapping("/api/v1/teacher/coding-tasks/{id}")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Update a coding task's settings")
    public CodingTaskDetail update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCodingTaskRequest request,
            HttpServletRequest http) {
        requireEditable(id);
        CodingTask updated =
                codingTasks.updateDetails(
                        id, request, currentUser.require().id(), http.getRemoteAddr());
        return toDetail(updated);
    }

    @PutMapping("/api/v1/teacher/coding-tasks/{id}/status")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Publish, archive, or return a coding task to draft")
    public CodingTaskDetail changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeCodingTaskStatusRequest request,
            HttpServletRequest http) {
        requireEditable(id);
        UUID actorId = currentUser.require().id();
        String ip = http.getRemoteAddr();
        CodingTask updated =
                switch (request.status()) {
                    case PUBLISHED -> codingTasks.publish(id, actorId, ip);
                    case ARCHIVED -> codingTasks.archive(id, actorId, ip);
                    case DRAFT -> codingTasks.returnToDraft(id, actorId, ip);
                };
        return toDetail(updated);
    }

    @PutMapping("/api/v1/teacher/coding-tasks/{id}/translations/{locale}")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Upsert one locale's coding task translation")
    public CodingTaskDetail upsertTranslation(
            @PathVariable UUID id,
            @PathVariable String locale,
            @Valid @RequestBody UpsertCodingTaskTranslationRequest request,
            HttpServletRequest http) {
        requireEditable(id);
        codingTasks.upsertTranslation(
                id, locale, request, currentUser.require().id(), http.getRemoteAddr());
        return toDetail(codingTasks.require(id));
    }

    @DeleteMapping("/api/v1/teacher/coding-tasks/{id}")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(
            summary =
                    "Archive a coding task (real delete is never exposed — submission history references it)")
    public ResponseEntity<Void> archive(@PathVariable UUID id, HttpServletRequest http) {
        requireEditable(id);
        codingTasks.archive(id, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    private CodingTask requireEditable(UUID id) {
        CodingTask task = codingTasks.require(id);
        accessGuard.requireCourseEditor(task.getCourse().getId());
        return task;
    }

    private CodingTaskDetail toDetail(CodingTask task) {
        List<CodingTaskHint> hints = content.listHints(task.getId());
        Map<UUID, List<CodingTaskHintTranslation>> hintTranslations =
                hints.stream()
                        .collect(
                                Collectors.toMap(
                                        CodingTaskHint::getId,
                                        h -> content.listHintTranslations(h.getId())));
        List<CodingTaskLanguage> languages = content.listLanguages(task.getId());
        List<CodingTaskTestCase> testCases = content.listTestCases(task.getId());
        return CodingTaskMapper.toDetail(
                task,
                codingTasks.listTranslations(task.getId()),
                "uz",
                languages,
                hints,
                hintTranslations,
                testCases);
    }
}
