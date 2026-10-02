package uz.aifu.lms.assessment.api;

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
import uz.aifu.lms.assessment.api.dto.QuizDtos.QuizDetail;
import uz.aifu.lms.assessment.api.dto.QuizDtos.QuizSummary;
import uz.aifu.lms.assessment.app.QuizService;
import uz.aifu.lms.assessment.domain.Quiz;
import uz.aifu.lms.assessment.domain.QuizTranslation;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * A course's published quiz list, enrollment-scoped. {@link AccessGuard#requireCourseViewer}
 * resolves admin/teacher/enrolled-student/past-student access uniformly, matching {@code
 * ModuleController}'s pattern.
 */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/quizzes")
@Tag(name = "Quizzes", description = "A course's published quiz list")
@SecurityRequirement(name = "bearerAuth")
public class QuizController {

    private final QuizService quizzes;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;

    public QuizController(
            QuizService quizzes, AccessGuard accessGuard, PageableSupport pageableSupport) {
        this.quizzes = quizzes;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('QUIZ_READ')")
    @Operation(summary = "List a course's published quizzes")
    public PageResponse<QuizSummary> list(
            @PathVariable UUID courseId,
            @RequestParam(required = false)
                    @Parameter(description = "Response locale; falls back per ADR-005")
                    String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            Pageable pageable) {
        accessGuard.requireCourseViewer(courseId);
        String resolvedLocale = resolveLocale(locale, acceptLanguage);
        Page<Quiz> page =
                quizzes.listPublishedByCourse(
                        courseId, pageableSupport.sanitize(pageable, Sort.by("code")));
        Map<UUID, List<QuizTranslation>> translations =
                quizzes.listTranslationsForQuizzes(
                        page.getContent().stream().map(Quiz::getId).toList());
        return PageResponse.of(
                page,
                q ->
                        QuizMapper.toSummary(
                                q,
                                translations.getOrDefault(q.getId(), List.of()),
                                resolvedLocale));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('QUIZ_READ')")
    @Operation(summary = "Get one published quiz")
    public QuizDetail get(
            @PathVariable UUID courseId,
            @PathVariable UUID id,
            @RequestParam(required = false) String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        accessGuard.requireCourseViewer(courseId);
        Quiz quiz = quizzes.requirePublished(id);
        String resolvedLocale = resolveLocale(locale, acceptLanguage);
        return QuizMapper.toDetail(quiz, quizzes.listTranslations(id), resolvedLocale);
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
