package uz.aifu.lms.coding.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.ProgrammingLanguageView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.StudentHintView;
import uz.aifu.lms.coding.app.CodingHintService;
import uz.aifu.lms.coding.app.CodingTaskService;
import uz.aifu.lms.coding.app.ProgrammingLanguageService;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.iam.app.access.AccessGuard;
import uz.aifu.lms.iam.app.access.CourseAccess;

/**
 * What a student needs around a coding task besides the task itself: the languages code can be
 * written in, and the task's hints opened one at a time (product spec S-05.1, S-05.2).
 */
@RestController
@Tag(name = "Coding tasks", description = "Languages and hints")
@SecurityRequirement(name = "bearerAuth")
public class CodingTaskStudentController {

    private final ProgrammingLanguageService languages;
    private final CodingHintService hints;
    private final CodingTaskService codingTasks;
    private final AccessGuard accessGuard;

    public CodingTaskStudentController(
            ProgrammingLanguageService languages,
            CodingHintService hints,
            CodingTaskService codingTasks,
            AccessGuard accessGuard) {
        this.languages = languages;
        this.hints = hints;
        this.codingTasks = codingTasks;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/api/v1/programming-languages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "The enabled programming languages (Python, Java, C++)")
    public List<ProgrammingLanguageView> languages() {
        return languages.listEnabled().stream()
                .map(
                        l ->
                                new ProgrammingLanguageView(
                                        l.getCode(),
                                        l.getDisplayName(),
                                        l.getVersionLabel(),
                                        l.getSourceFilename()))
                .toList();
    }

    @GetMapping("/api/v1/coding-tasks/{codingTaskId}/hints")
    @PreAuthorize("hasAuthority('CODING_TASK_READ')")
    @Operation(
            summary = "The task's hints as the caller has opened them",
            description = "A hint that is not opened yet has no body.")
    public List<StudentHintView> hints(
            @PathVariable UUID codingTaskId,
            @RequestParam(required = false) String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        UUID userId = requireReadable(codingTaskId);
        return hints.list(
                codingTaskId, userId, CodingTaskController.resolveLocale(locale, acceptLanguage));
    }

    @PostMapping("/api/v1/coding-tasks/{codingTaskId}/hints/{hintId}/reveal")
    @PreAuthorize("hasAuthority('CODING_TASK_READ')")
    @Operation(
            summary = "Open the next hint",
            description = "Hints open in order; skipping ahead answers 422. Repeating is harmless.")
    public List<StudentHintView> reveal(
            @PathVariable UUID codingTaskId,
            @PathVariable UUID hintId,
            @RequestParam(required = false) String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        UUID userId = requireReadable(codingTaskId);
        return hints.reveal(
                codingTaskId,
                hintId,
                userId,
                CodingTaskController.resolveLocale(locale, acceptLanguage));
    }

    /** A student reads published tasks of their courses; a draft exists only for its editors. */
    private UUID requireReadable(UUID codingTaskId) {
        CourseAccess access = accessGuard.requireCodingTaskViewer(codingTaskId);
        if (!access.canSeeDrafts()
                && codingTasks.require(codingTaskId).getStatus() != PublicationStatus.PUBLISHED) {
            throw Exceptions.notFound("CodingTask", codingTaskId);
        }
        return access.userId();
    }
}
