package uz.aifu.lms.academics.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.academics.api.dto.SubjectDtos.SubjectDetail;
import uz.aifu.lms.academics.api.dto.SubjectDtos.SubjectSummary;
import uz.aifu.lms.academics.app.SubjectService;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;

/**
 * Public subject catalogue ("fan"). Always PUBLISHED-only, regardless of caller role — a caller
 * wanting drafts uses {@code /admin/subjects}. See {@link AdminSubjectController}.
 */
@RestController
@RequestMapping("/api/v1/subjects")
@Tag(name = "Subjects", description = "Public subject catalogue")
@SecurityRequirement(name = "bearerAuth")
public class SubjectController {

    private final SubjectService subjects;
    private final PageableSupport pageableSupport;

    public SubjectController(SubjectService subjects, PageableSupport pageableSupport) {
        this.subjects = subjects;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUBJECT_READ')")
    @Operation(
            summary = "Browse the published subject catalogue",
            description = "Paginated, filtered by free-text query.")
    public PageResponse<SubjectSummary> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false)
                    @Parameter(description = "Response locale; falls back per ADR-005")
                    String locale,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            Pageable pageable) {
        String resolvedLocale = resolveLocale(locale, acceptLanguage);
        var page =
                subjects.search(
                        query,
                        PublicationStatus.PUBLISHED,
                        departmentId,
                        resolvedLocale,
                        pageableSupport.sanitize(pageable));
        var byId =
                subjects.listTranslationsForSubjects(
                        page.getContent().stream().map(Subject::getId).toList());
        return PageResponse.of(
                page,
                s ->
                        SubjectMapper.toSummary(
                                s,
                                byId.getOrDefault(s.getId(), java.util.List.of()),
                                resolvedLocale));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUBJECT_READ')")
    @Operation(summary = "Get one published subject with outcomes and prerequisites")
    public SubjectDetail get(@PathVariable UUID id) {
        Subject subject = subjects.require(id);
        if (subject.getStatus() != PublicationStatus.PUBLISHED) {
            throw Exceptions.notFound("Subject", id);
        }
        return SubjectMapper.toDetail(subject, subjects);
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
