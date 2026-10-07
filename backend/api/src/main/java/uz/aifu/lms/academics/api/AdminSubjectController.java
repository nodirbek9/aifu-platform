package uz.aifu.lms.academics.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.academics.api.dto.SubjectDtos.AddLearningOutcomeRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.AddPrerequisiteRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.ChangeSubjectStatusRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.CreateSubjectRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.SubjectDetail;
import uz.aifu.lms.academics.api.dto.SubjectDtos.SubjectSummary;
import uz.aifu.lms.academics.api.dto.SubjectDtos.UpdateSubjectRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.UpsertLearningOutcomeTranslationRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.UpsertSubjectTranslationRequest;
import uz.aifu.lms.academics.app.SubjectService;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.i18n.LocaleResolver;
import uz.aifu.lms.common.security.CurrentUserProvider;

/**
 * Subject catalogue administration. All routes are ADMIN-only ({@code SUBJECT_MANAGE}) — the
 * catalogue itself, unlike course delivery, is not something a teacher edits
 * (skills/authorization/SKILL.md).
 */
@RestController
@RequestMapping("/api/v1/admin/subjects")
@Tag(name = "Admin: subjects", description = "Subject catalogue administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminSubjectController {

    private final SubjectService subjects;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;
    private final LocaleResolver localeResolver;

    public AdminSubjectController(
            SubjectService subjects,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser,
            LocaleResolver localeResolver) {
        this.subjects = subjects;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
        this.localeResolver = localeResolver;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUBJECT_MANAGE')")
    @Operation(summary = "Search the full subject catalogue, any status")
    public PageResponse<SubjectSummary> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) PublicationStatus status,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) String locale,
            Pageable pageable,
            HttpServletRequest http) {
        String resolvedLocale = localeResolver.preferred(locale, http, null, null).code();
        var page =
                subjects.search(
                        query,
                        status,
                        departmentId,
                        resolvedLocale,
                        pageableSupport.sanitize(pageable));
        return PageResponse.of(
                page,
                s ->
                        SubjectMapper.toSummary(
                                s, subjects.listTranslations(s.getId()), resolvedLocale));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUBJECT_MANAGE')")
    @Operation(summary = "Get one subject, any status")
    public SubjectDetail get(@PathVariable UUID id) {
        return SubjectMapper.toDetail(subjects.require(id), subjects);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @PreAuthorize("hasAuthority('SUBJECT_MANAGE')")
    @Operation(summary = "Create a subject")
    public ResponseEntity<SubjectDetail> create(
            @Valid @RequestBody CreateSubjectRequest request, HttpServletRequest http) {
        Subject created = subjects.create(request, currentUser.require().id(), clientIp(http));
        return ResponseEntity.created(URI.create("/api/v1/admin/subjects/" + created.getId()))
                .body(SubjectMapper.toDetail(created, subjects));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SUBJECT_MANAGE')")
    @Operation(summary = "Update catalogue fields")
    public SubjectDetail update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubjectRequest request,
            HttpServletRequest http) {
        return SubjectMapper.toDetail(
                subjects.update(id, request, currentUser.require().id(), clientIp(http)), subjects);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SUBJECT_MANAGE')")
    @Operation(
            summary = "Change publication status",
            description =
                    "PUBLISHED requires a translation for the subject's default instruction language.")
    public SubjectDetail changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeSubjectStatusRequest request,
            HttpServletRequest http) {
        UUID actorId = currentUser.require().id();
        String ip = clientIp(http);
        Subject subject =
                switch (request.status()) {
                    case PUBLISHED -> subjects.publish(id, actorId, ip);
                    case ARCHIVED -> subjects.archive(id, actorId, ip);
                    case DRAFT -> subjects.returnToDraft(id, actorId, ip);
                };
        return SubjectMapper.toDetail(subject, subjects);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUBJECT_MANAGE')")
    @Operation(summary = "Soft-delete a subject")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest http) {
        subjects.softDelete(id, currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/translations/{locale}")
    @PreAuthorize("hasAuthority('SUBJECT_MANAGE')")
    @Operation(summary = "Upsert one locale's subject copy")
    public SubjectDetail upsertTranslation(
            @PathVariable UUID id,
            @PathVariable String locale,
            @Valid @RequestBody UpsertSubjectTranslationRequest request,
            HttpServletRequest http) {
        subjects.upsertTranslation(id, locale, request, currentUser.require().id(), clientIp(http));
        return SubjectMapper.toDetail(subjects.require(id), subjects);
    }

    @PostMapping("/{id}/prerequisites")
    @PreAuthorize("hasAuthority('SUBJECT_MANAGE')")
    @Operation(summary = "Add a prerequisite (coded subject or free-text note)")
    public SubjectDetail addPrerequisite(
            @PathVariable UUID id,
            @Valid @RequestBody AddPrerequisiteRequest request,
            HttpServletRequest http) {
        subjects.addPrerequisite(id, request, currentUser.require().id(), clientIp(http));
        return SubjectMapper.toDetail(subjects.require(id), subjects);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}/prerequisites/{prerequisiteId}")
    @PreAuthorize("hasAuthority('SUBJECT_MANAGE')")
    @Operation(summary = "Remove a prerequisite")
    public ResponseEntity<Void> removePrerequisite(
            @PathVariable UUID id, @PathVariable UUID prerequisiteId, HttpServletRequest http) {
        subjects.removePrerequisite(id, prerequisiteId, currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/learning-outcomes")
    @PreAuthorize("hasAuthority('SUBJECT_MANAGE')")
    @Operation(summary = "Add a learning outcome (TN1, TN2, ...)")
    public SubjectDetail addOutcome(
            @PathVariable UUID id,
            @Valid @RequestBody AddLearningOutcomeRequest request,
            HttpServletRequest http) {
        subjects.addOutcome(id, request, currentUser.require().id(), clientIp(http));
        return SubjectMapper.toDetail(subjects.require(id), subjects);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/learning-outcomes/{outcomeId}/translations/{locale}")
    @PreAuthorize("hasAuthority('SUBJECT_MANAGE')")
    @Operation(summary = "Upsert one locale's statement for a learning outcome")
    public ResponseEntity<Void> upsertOutcomeTranslation(
            @PathVariable UUID outcomeId,
            @PathVariable String locale,
            @Valid @RequestBody UpsertLearningOutcomeTranslationRequest request,
            HttpServletRequest http) {
        subjects.upsertOutcomeTranslation(
                outcomeId, locale, request.statement(), currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    private static String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        return http.getRemoteAddr();
    }
}
