package uz.aifu.lms.curriculum.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.ContentBlockView;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.CreateContentBlockRequest;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.ReorderContentBlocksRequest;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.UpdateContentBlockRequest;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.UpsertContentBlockTranslationRequest;
import uz.aifu.lms.curriculum.app.ContentBlockService;
import uz.aifu.lms.curriculum.domain.ContentBlock;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * Content block administration — the block's own {@code lesson_id} is used for the ownership guard,
 * never the {@code lessonId} path variable on write endpoints, so a caller cannot move a block by
 * pointing at a course they do not teach.
 */
@RestController
@Tag(name = "Teacher: content blocks", description = "Content block administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminContentBlockController {

    private final ContentBlockService blocks;
    private final AccessGuard accessGuard;
    private final CurrentUserProvider currentUser;
    private final ObjectMapper objectMapper;
    private final PageableSupport pageableSupport;

    public AdminContentBlockController(
            ContentBlockService blocks,
            AccessGuard accessGuard,
            CurrentUserProvider currentUser,
            ObjectMapper objectMapper,
            PageableSupport pageableSupport) {
        this.blocks = blocks;
        this.accessGuard = accessGuard;
        this.currentUser = currentUser;
        this.objectMapper = objectMapper;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping("/api/v1/teacher/lessons/{lessonId}/blocks")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "List a lesson's content blocks")
    public PageResponse<ContentBlockView> list(@PathVariable UUID lessonId, Pageable pageable) {
        accessGuard.requireLessonEditor(lessonId);
        var page =
                blocks.listByLesson(
                        lessonId, pageableSupport.sanitize(pageable, Sort.by("position")));
        var translations =
                blocks.listTranslationsForBlocks(
                        page.getContent().stream().map(ContentBlock::getId).toList());
        return PageResponse.of(
                page,
                b ->
                        ContentBlockMapper.toView(
                                b,
                                translations.getOrDefault(b.getId(), List.of()),
                                LocaleCode.DEFAULT.code(),
                                LocaleCode.DEFAULT.code(),
                                objectMapper));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/teacher/lessons/{lessonId}/blocks")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Create a content block within a lesson")
    public ResponseEntity<ContentBlockView> create(
            @PathVariable UUID lessonId,
            @Valid @RequestBody CreateContentBlockRequest request,
            HttpServletRequest http) {
        accessGuard.requireLessonEditor(lessonId);
        ContentBlock created =
                blocks.create(lessonId, request, currentUser.require().id(), clientIp(http));
        return ResponseEntity.created(URI.create("/api/v1/teacher/blocks/" + created.getId()))
                .body(
                        ContentBlockMapper.toView(
                                created,
                                List.of(),
                                LocaleCode.DEFAULT.code(),
                                LocaleCode.DEFAULT.code(),
                                objectMapper));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/api/v1/teacher/lessons/{lessonId}/blocks/reorder")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Reorder a lesson's content blocks")
    public ResponseEntity<Void> reorder(
            @PathVariable UUID lessonId,
            @Valid @RequestBody ReorderContentBlocksRequest request,
            HttpServletRequest http) {
        accessGuard.requireLessonEditor(lessonId);
        blocks.reorder(
                lessonId, request.orderedBlockIds(), currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/v1/teacher/blocks/{id}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Update a content block's settings/file/visibility")
    public ContentBlockView update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContentBlockRequest request,
            HttpServletRequest http) {
        ContentBlock existing = blocks.require(id);
        accessGuard.requireLessonEditor(existing.getLesson().getId());
        ContentBlock updated =
                blocks.updateSettings(id, request, currentUser.require().id(), clientIp(http));
        var translations = blocks.listTranslations(id);
        return ContentBlockMapper.toView(
                updated,
                translations,
                LocaleCode.DEFAULT.code(),
                LocaleCode.DEFAULT.code(),
                objectMapper);
    }

    @PutMapping("/api/v1/teacher/blocks/{id}/translations/{locale}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(
            summary =
                    "Upsert one locale's block content. Payload shape depends on the block's type.")
    public ContentBlockView upsertTranslation(
            @PathVariable UUID id,
            @PathVariable String locale,
            @Valid @RequestBody UpsertContentBlockTranslationRequest request,
            HttpServletRequest http) {
        ContentBlock existing = blocks.require(id);
        accessGuard.requireLessonEditor(existing.getLesson().getId());
        blocks.upsertTranslation(id, locale, request, currentUser.require().id(), clientIp(http));
        var translations = blocks.listTranslations(id);
        return ContentBlockMapper.toView(
                blocks.require(id), translations, locale, LocaleCode.DEFAULT.code(), objectMapper);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/v1/teacher/blocks/{id}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Soft-delete a content block")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest http) {
        ContentBlock existing = blocks.require(id);
        accessGuard.requireLessonEditor(existing.getLesson().getId());
        blocks.softDelete(id, currentUser.require().id(), clientIp(http));
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
