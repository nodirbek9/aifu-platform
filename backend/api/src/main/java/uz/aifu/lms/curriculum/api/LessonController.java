package uz.aifu.lms.curriculum.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonDetail;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonSummary;
import uz.aifu.lms.curriculum.app.ContentBlockService;
import uz.aifu.lms.curriculum.app.LessonService;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.curriculum.domain.LessonTranslation;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * A module's published lesson list, and one lesson's full content — both enrollment-scoped (brief
 * §9). An unpublished lesson is invisible to a student even if the course itself is published:
 * {@link LessonService#requirePublished} returns 404, matching the platform-wide "a caller with no
 * relationship to an object gets 404, not 403" rule (no enumeration oracle).
 */
@RestController
@Tag(name = "Lessons", description = "A module's published lessons, and one lesson's full content")
@SecurityRequirement(name = "bearerAuth")
public class LessonController {

    private final LessonService lessons;
    private final ContentBlockService blocks;
    private final AccessGuard accessGuard;
    private final ObjectMapper objectMapper;
    private final PageableSupport pageableSupport;

    public LessonController(
            LessonService lessons,
            ContentBlockService blocks,
            AccessGuard accessGuard,
            ObjectMapper objectMapper,
            PageableSupport pageableSupport) {
        this.lessons = lessons;
        this.blocks = blocks;
        this.accessGuard = accessGuard;
        this.objectMapper = objectMapper;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping("/api/v1/modules/{moduleId}/lessons")
    @PreAuthorize("hasAuthority('LESSON_READ')")
    @Operation(summary = "List a module's published lessons, in order")
    public PageResponse<LessonSummary> list(
            @PathVariable UUID moduleId,
            @RequestParam(required = false)
                    @Parameter(description = "Response locale; falls back per ADR-005")
                    String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            Pageable pageable) {
        accessGuard.requireModuleViewer(moduleId);
        String resolvedLocale = resolveLocale(locale, acceptLanguage);
        Page<Lesson> page =
                lessons.listPublishedByModule(
                        moduleId, pageableSupport.sanitize(pageable, Sort.by("position")));
        Map<UUID, List<LessonTranslation>> translations =
                lessons.listTranslationsForLessons(
                        page.getContent().stream().map(Lesson::getId).toList());
        return PageResponse.of(
                page,
                l ->
                        LessonMapper.toSummary(
                                l,
                                translations.getOrDefault(l.getId(), List.of()),
                                resolvedLocale));
    }

    @GetMapping("/api/v1/lessons/{lessonId}")
    @PreAuthorize("hasAuthority('LESSON_READ')")
    @Operation(
            summary =
                    "Get one published lesson with all its content blocks, resolved for one locale")
    public LessonDetail get(
            @PathVariable UUID lessonId,
            @RequestParam(required = false)
                    @Parameter(description = "Response locale; falls back per ADR-005")
                    String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        accessGuard.requireOpenLesson(lessonId);
        Lesson lesson = lessons.requirePublished(lessonId);
        String resolvedLocale = resolveLocale(locale, acceptLanguage);
        // The student-facing rendering, also used for a teacher's preview and for the projector:
        // teacher-only blocks (lesson plans, notes) are never part of it.
        return LessonMapper.toDetail(lesson, lessons, blocks, resolvedLocale, objectMapper, false);
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
