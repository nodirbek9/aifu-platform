package uz.aifu.lms.curriculum.api;

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
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ModuleSummary;
import uz.aifu.lms.curriculum.app.ModuleService;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.curriculum.domain.CourseModuleTranslation;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * A course's published module list, enrollment-scoped. {@link AccessGuard#requireCourseViewer}
 * resolves admin/teacher/enrolled-student/past-student access uniformly — unpublished modules are
 * never returned here regardless of who's asking, matching {@link AdminModuleController} for the
 * full, any-status view.
 */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/modules")
@Tag(name = "Modules", description = "A course's published module list")
@SecurityRequirement(name = "bearerAuth")
public class ModuleController {

    private final ModuleService modules;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;

    public ModuleController(
            ModuleService modules, AccessGuard accessGuard, PageableSupport pageableSupport) {
        this.modules = modules;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COURSE_CONTENT_READ')")
    @Operation(summary = "List a course's published modules, in order")
    public PageResponse<ModuleSummary> list(
            @PathVariable UUID courseId,
            @RequestParam(required = false)
                    @Parameter(description = "Response locale; falls back per ADR-005")
                    String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            Pageable pageable) {
        accessGuard.requireCourseViewer(courseId);
        String resolvedLocale = resolveLocale(locale, acceptLanguage);
        Page<CourseModule> page =
                modules.listPublishedByCourse(
                        courseId, pageableSupport.sanitize(pageable, Sort.by("position")));
        Map<UUID, List<CourseModuleTranslation>> translations =
                modules.listTranslationsForModules(
                        page.getContent().stream().map(CourseModule::getId).toList());
        return PageResponse.of(
                page,
                m ->
                        ModuleMapper.toSummary(
                                m,
                                translations.getOrDefault(m.getId(), List.of()),
                                resolvedLocale));
    }

    private static String resolveLocale(String explicit, String acceptLanguage) {
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
