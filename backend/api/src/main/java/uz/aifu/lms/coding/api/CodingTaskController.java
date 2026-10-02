package uz.aifu.lms.coding.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CodingTaskStudentDetail;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CodingTaskSummary;
import uz.aifu.lms.coding.app.CodingTaskContentService;
import uz.aifu.lms.coding.app.CodingTaskService;
import uz.aifu.lms.coding.domain.CodingTask;
import uz.aifu.lms.coding.domain.CodingTaskTranslation;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * A course's published coding task list, enrollment-scoped. {@link AccessGuard#requireCourseViewer}
 * resolves admin/teacher/enrolled-student/past-student access uniformly, matching {@code
 * assessment.api.QuizController}'s pattern.
 */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/coding-tasks")
@Tag(name = "Coding tasks", description = "A course's published coding task list")
@SecurityRequirement(name = "bearerAuth")
public class CodingTaskController {

    private final CodingTaskService codingTasks;
    private final CodingTaskContentService content;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;

    public CodingTaskController(
            CodingTaskService codingTasks,
            CodingTaskContentService content,
            AccessGuard accessGuard,
            PageableSupport pageableSupport) {
        this.codingTasks = codingTasks;
        this.content = content;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CODING_TASK_READ')")
    @Operation(summary = "List a course's published coding tasks")
    public PageResponse<CodingTaskSummary> list(
            @PathVariable UUID courseId,
            @RequestParam(required = false)
                    @Parameter(description = "Response locale; falls back per ADR-005")
                    String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            Pageable pageable) {
        accessGuard.requireCourseViewer(courseId);
        String resolvedLocale = resolveLocale(locale, acceptLanguage);
        Page<CodingTask> page =
                codingTasks.listPublishedByCourse(
                        courseId, pageableSupport.sanitize(pageable, Sort.by("code")));
        Map<UUID, List<CodingTaskTranslation>> translations =
                codingTasks.listTranslationsForTasks(
                        page.getContent().stream().map(CodingTask::getId).toList());
        return PageResponse.of(
                page,
                t ->
                        CodingTaskMapper.toSummary(
                                t,
                                translations.getOrDefault(t.getId(), List.of()),
                                resolvedLocale));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CODING_TASK_READ')")
    @Operation(
            summary =
                    "Get one published coding task, student-safe (no solution code, hidden tests redacted)")
    public CodingTaskStudentDetail get(
            @PathVariable UUID courseId,
            @PathVariable UUID id,
            @RequestParam(required = false) String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        accessGuard.requireCourseViewer(courseId);
        CodingTask task = codingTasks.requirePublished(id);
        String resolvedLocale = resolveLocale(locale, acceptLanguage);
        return CodingTaskMapper.toStudentDetail(
                task,
                codingTasks.listTranslations(id),
                resolvedLocale,
                content.listLanguages(id),
                content.listTestCases(id));
    }

    static String resolveLocale(String explicit, String acceptLanguage) {
        return LocaleCode.find(explicit)
                .or(() -> LocaleCode.find(firstAcceptLanguageTag(acceptLanguage)))
                .orElse(LocaleCode.DEFAULT)
                .code();
    }

    private static String firstAcceptLanguageTag(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return null;
        }
        return acceptLanguage.split(",")[0].split(";")[0].trim();
    }
}
