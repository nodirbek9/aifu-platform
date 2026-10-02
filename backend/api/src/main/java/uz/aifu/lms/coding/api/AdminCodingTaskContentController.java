package uz.aifu.lms.coding.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CreateHintRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CreateTestCaseRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.HintView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.LanguageTeacherView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.TestCaseView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpdateTestCaseRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpsertHintTranslationRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpsertLanguageRequest;
import uz.aifu.lms.coding.app.CodingTaskContentService;
import uz.aifu.lms.coding.app.CodingTaskService;
import uz.aifu.lms.coding.domain.CodingTaskHint;
import uz.aifu.lms.coding.domain.CodingTaskHintTranslation;
import uz.aifu.lms.coding.domain.CodingTaskLanguage;
import uz.aifu.lms.coding.domain.CodingTaskTestCase;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * Languages, hints and test cases for a coding task — same shape as {@code
 * assessment.api.AdminQuestionController}'s bundling of answer options and answer keys. {@code
 * CODING_TASK_MANAGE}, TEACHER (V003).
 */
@RestController
@Tag(name = "Teacher: coding task content", description = "Languages, hints, and test cases")
@SecurityRequirement(name = "bearerAuth")
public class AdminCodingTaskContentController {

    private final CodingTaskContentService content;
    private final CodingTaskService codingTasks;
    private final AccessGuard accessGuard;
    private final CurrentUserProvider currentUser;

    public AdminCodingTaskContentController(
            CodingTaskContentService content,
            CodingTaskService codingTasks,
            AccessGuard accessGuard,
            CurrentUserProvider currentUser) {
        this.content = content;
        this.codingTasks = codingTasks;
        this.accessGuard = accessGuard;
        this.currentUser = currentUser;
    }

    // ---------------------------------------------------------------- languages

    @GetMapping("/api/v1/teacher/coding-tasks/{id}/languages")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "List a coding task's languages, with solution code")
    public List<LanguageTeacherView> listLanguages(@PathVariable UUID id) {
        requireEditable(id);
        return content.listLanguages(id).stream()
                .map(AdminCodingTaskContentController::toLanguageTeacherView)
                .toList();
    }

    @PostMapping("/api/v1/teacher/coding-tasks/{id}/languages/{languageCode}")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Attach an enabled language to a coding task")
    public ResponseEntity<LanguageTeacherView> addLanguage(
            @PathVariable UUID id,
            @PathVariable String languageCode,
            @Valid @RequestBody UpsertLanguageRequest request,
            HttpServletRequest http) {
        requireEditable(id);
        CodingTaskLanguage saved =
                content.addLanguage(
                        id,
                        languageCode,
                        request,
                        currentUser.require().id(),
                        http.getRemoteAddr());
        return ResponseEntity.status(201)
                .body(AdminCodingTaskContentController.toLanguageTeacherView(saved));
    }

    @PatchMapping("/api/v1/teacher/coding-task-languages/{taskLanguageId}")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Update a language's starter/solution code")
    public LanguageTeacherView updateLanguage(
            @PathVariable UUID taskLanguageId,
            @Valid @RequestBody UpsertLanguageRequest request,
            HttpServletRequest http) {
        UUID taskId = content.codingTaskIdOfLanguage(taskLanguageId);
        accessGuard.requireCodingTaskEditor(taskId);
        content.updateLanguage(
                taskLanguageId, request, currentUser.require().id(), http.getRemoteAddr());
        // Re-read through listLanguages (which loads the language row) instead of mapping the
        // returned entity: its ProgrammingLanguage is a lazy proxy once the service transaction
        // has closed, which failed with LazyInitializationException (500).
        return content.listLanguages(taskId).stream()
                .filter(l -> l.getId().equals(taskLanguageId))
                .findFirst()
                .map(AdminCodingTaskContentController::toLanguageTeacherView)
                .orElseThrow(() -> Exceptions.notFound("CodingTaskLanguage", taskLanguageId));
    }

    @DeleteMapping("/api/v1/teacher/coding-task-languages/{taskLanguageId}")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Detach a language from a coding task")
    public ResponseEntity<Void> removeLanguage(
            @PathVariable UUID taskLanguageId, HttpServletRequest http) {
        accessGuard.requireCodingTaskEditor(content.codingTaskIdOfLanguage(taskLanguageId));
        content.removeLanguage(taskLanguageId, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- hints

    @GetMapping("/api/v1/teacher/coding-tasks/{id}/hints")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "List a coding task's hints")
    public List<HintView> listHints(@PathVariable UUID id) {
        requireEditable(id);
        return content.listHints(id).stream()
                .map(h -> toHintView(h, content.listHintTranslations(h.getId())))
                .toList();
    }

    @PostMapping("/api/v1/teacher/coding-tasks/{id}/hints")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Add a hint to a coding task")
    public ResponseEntity<HintView> addHint(
            @PathVariable UUID id,
            @Valid @RequestBody CreateHintRequest request,
            HttpServletRequest http) {
        requireEditable(id);
        CodingTaskHint saved =
                content.addHint(id, request, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.status(201).body(toHintView(saved, List.of()));
    }

    @DeleteMapping("/api/v1/teacher/coding-task-hints/{hintId}")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Remove a hint")
    public ResponseEntity<Void> removeHint(@PathVariable UUID hintId, HttpServletRequest http) {
        accessGuard.requireCodingTaskEditor(content.codingTaskIdOfHint(hintId));
        content.removeHint(hintId, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/teacher/coding-task-hints/{hintId}/translations/{locale}")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Upsert one locale's hint translation")
    public HintView upsertHintTranslation(
            @PathVariable UUID hintId,
            @PathVariable String locale,
            @Valid @RequestBody UpsertHintTranslationRequest request,
            HttpServletRequest http) {
        accessGuard.requireCodingTaskEditor(content.codingTaskIdOfHint(hintId));
        CodingTaskHintTranslation translation =
                content.upsertHintTranslation(
                        hintId, locale, request, currentUser.require().id(), http.getRemoteAddr());
        return toHintView(translation.getHint(), List.of(translation));
    }

    // ---------------------------------------------------------------- test cases

    @GetMapping("/api/v1/teacher/coding-tasks/{id}/test-cases")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(
            summary = "List a coding task's test cases, including hidden ones and expected output")
    public List<TestCaseView> listTestCases(@PathVariable UUID id) {
        requireEditable(id);
        return content.listTestCases(id).stream()
                .map(AdminCodingTaskContentController::toTestCaseView)
                .toList();
    }

    @PostMapping("/api/v1/teacher/coding-tasks/{id}/test-cases")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Add a test case to a coding task")
    public ResponseEntity<TestCaseView> addTestCase(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTestCaseRequest request,
            HttpServletRequest http) {
        requireEditable(id);
        CodingTaskTestCase saved =
                content.addTestCase(id, request, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.status(201)
                .body(AdminCodingTaskContentController.toTestCaseView(saved));
    }

    @PatchMapping("/api/v1/teacher/coding-task-test-cases/{testCaseId}")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Update a test case")
    public TestCaseView updateTestCase(
            @PathVariable UUID testCaseId,
            @Valid @RequestBody UpdateTestCaseRequest request,
            HttpServletRequest http) {
        accessGuard.requireCodingTaskEditor(content.codingTaskIdOfTestCase(testCaseId));
        CodingTaskTestCase updated =
                content.updateTestCase(
                        testCaseId, request, currentUser.require().id(), http.getRemoteAddr());
        return AdminCodingTaskContentController.toTestCaseView(updated);
    }

    @DeleteMapping("/api/v1/teacher/coding-task-test-cases/{testCaseId}")
    @PreAuthorize("hasAuthority('CODING_TASK_MANAGE')")
    @Operation(summary = "Remove a test case")
    public ResponseEntity<Void> removeTestCase(
            @PathVariable UUID testCaseId, HttpServletRequest http) {
        accessGuard.requireCodingTaskEditor(content.codingTaskIdOfTestCase(testCaseId));
        content.removeTestCase(testCaseId, currentUser.require().id(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- helpers

    private void requireEditable(UUID codingTaskId) {
        var task = codingTasks.require(codingTaskId);
        accessGuard.requireCourseEditor(task.getCourse().getId());
    }

    private static HintView toHintView(
            CodingTaskHint hint, List<CodingTaskHintTranslation> translations) {
        String body =
                translations.stream()
                        .findFirst()
                        .map(CodingTaskHintTranslation::getBody)
                        .orElse(null);
        return new HintView(hint.getId(), hint.getPosition(), hint.getPenaltyPercent(), body);
    }

    private static LanguageTeacherView toLanguageTeacherView(CodingTaskLanguage entry) {
        return new LanguageTeacherView(
                entry.getLanguage().getCode(),
                entry.getLanguage().getDisplayName(),
                entry.getLanguage().getVersionLabel(),
                entry.getStarterCode(),
                entry.getSolutionCode(),
                entry.getId());
    }

    private static TestCaseView toTestCaseView(CodingTaskTestCase testCase) {
        return new TestCaseView(
                testCase.getId(),
                testCase.getPosition(),
                testCase.getName(),
                testCase.getVisibility(),
                testCase.isExample(),
                testCase.getWeight(),
                testCase.getStdin(),
                testCase.getExpectedStdout(),
                testCase.getComparisonMode(),
                testCase.getNumericTolerance(),
                testCase.getCpuTimeLimitMs());
    }
}
