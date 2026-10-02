package uz.aifu.lms.curriculum.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
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
import uz.aifu.lms.curriculum.api.dto.LessonDtos.ChangeLessonStatusRequest;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.CreateLessonRequest;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonDetail;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.ReorderLessonsRequest;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.UpdateLessonRequest;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.UpsertLessonTranslationRequest;
import uz.aifu.lms.curriculum.app.ContentBlockService;
import uz.aifu.lms.curriculum.app.LessonService;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * Lesson administration. See {@link AdminModuleController}'s javadoc for the authorization model.
 */
@RestController
@Tag(name = "Teacher: lessons", description = "Lesson administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminLessonController {

    private final LessonService lessons;
    private final ContentBlockService blocks;
    private final AccessGuard accessGuard;
    private final CurrentUserProvider currentUser;
    private final ObjectMapper objectMapper;
    private final PageableSupport pageableSupport;

    public AdminLessonController(
            LessonService lessons,
            ContentBlockService blocks,
            AccessGuard accessGuard,
            CurrentUserProvider currentUser,
            ObjectMapper objectMapper,
            PageableSupport pageableSupport) {
        this.lessons = lessons;
        this.blocks = blocks;
        this.accessGuard = accessGuard;
        this.currentUser = currentUser;
        this.objectMapper = objectMapper;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping("/api/v1/teacher/modules/{moduleId}/lessons")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "List a module's lessons, any status")
    public PageResponse<LessonDetail> list(@PathVariable UUID moduleId, Pageable pageable) {
        accessGuard.requireModuleEditor(moduleId);
        var page =
                lessons.listByModule(
                        moduleId, pageableSupport.sanitize(pageable, Sort.by("position")));
        return PageResponse.of(
                page,
                l ->
                        LessonMapper.toDetail(
                                l, lessons, blocks, LocaleCode.DEFAULT.code(), objectMapper));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/teacher/modules/{moduleId}/lessons")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Create a lesson within a module")
    public ResponseEntity<LessonDetail> create(
            @PathVariable UUID moduleId,
            @Valid @RequestBody CreateLessonRequest request,
            HttpServletRequest http) {
        accessGuard.requireModuleEditor(moduleId);
        Lesson created =
                lessons.create(moduleId, request, currentUser.require().id(), clientIp(http));
        return ResponseEntity.created(URI.create("/api/v1/teacher/lessons/" + created.getId()))
                .body(
                        LessonMapper.toDetail(
                                created, lessons, blocks, LocaleCode.DEFAULT.code(), objectMapper));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/api/v1/teacher/modules/{moduleId}/lessons/reorder")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Reorder a module's lessons")
    public ResponseEntity<Void> reorder(
            @PathVariable UUID moduleId,
            @Valid @RequestBody ReorderLessonsRequest request,
            HttpServletRequest http) {
        accessGuard.requireModuleEditor(moduleId);
        lessons.reorder(
                moduleId, request.orderedLessonIds(), currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/teacher/lessons/{id}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Get one lesson, any status, with its content blocks")
    public LessonDetail get(@PathVariable UUID id) {
        accessGuard.requireLessonEditor(id);
        return LessonMapper.toDetail(
                lessons.require(id), lessons, blocks, LocaleCode.DEFAULT.code(), objectMapper);
    }

    @PatchMapping("/api/v1/teacher/lessons/{id}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Update lesson fields")
    public LessonDetail update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLessonRequest request,
            HttpServletRequest http) {
        accessGuard.requireLessonEditor(id);
        Lesson updated =
                lessons.updateDetails(id, request, currentUser.require().id(), clientIp(http));
        return LessonMapper.toDetail(
                updated, lessons, blocks, LocaleCode.DEFAULT.code(), objectMapper);
    }

    @PutMapping("/api/v1/teacher/lessons/{id}/status")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Publish, archive or return a lesson to draft")
    public LessonDetail changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeLessonStatusRequest request,
            HttpServletRequest http) {
        accessGuard.requireLessonEditor(id);
        UUID actorId = currentUser.require().id();
        String ip = clientIp(http);
        Lesson lesson =
                switch (request.status()) {
                    case PUBLISHED -> lessons.publish(id, actorId, ip);
                    case ARCHIVED -> lessons.archive(id, actorId, ip);
                    case DRAFT -> lessons.returnToDraft(id, actorId, ip);
                };
        return LessonMapper.toDetail(
                lesson, lessons, blocks, LocaleCode.DEFAULT.code(), objectMapper);
    }

    @PutMapping("/api/v1/teacher/lessons/{id}/translations/{locale}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Upsert one locale's lesson copy")
    public LessonDetail upsertTranslation(
            @PathVariable UUID id,
            @PathVariable String locale,
            @Valid @RequestBody UpsertLessonTranslationRequest request,
            HttpServletRequest http) {
        accessGuard.requireLessonEditor(id);
        lessons.upsertTranslation(id, locale, request, currentUser.require().id(), clientIp(http));
        return LessonMapper.toDetail(
                lessons.require(id), lessons, blocks, LocaleCode.DEFAULT.code(), objectMapper);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/api/v1/teacher/lessons/{id}/outcomes/{outcomeId}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Link a learning outcome this lesson advances")
    public ResponseEntity<Void> addOutcome(
            @PathVariable UUID id, @PathVariable UUID outcomeId, HttpServletRequest http) {
        accessGuard.requireLessonEditor(id);
        lessons.addOutcome(id, outcomeId, currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/v1/teacher/lessons/{id}/outcomes/{outcomeId}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Unlink a learning outcome")
    public ResponseEntity<Void> removeOutcome(
            @PathVariable UUID id, @PathVariable UUID outcomeId, HttpServletRequest http) {
        accessGuard.requireLessonEditor(id);
        lessons.removeOutcome(id, outcomeId, currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/api/v1/teacher/lessons/{id}/prerequisites/{requiredLessonId}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Add a lesson prerequisite")
    public ResponseEntity<Void> addPrerequisite(
            @PathVariable UUID id, @PathVariable UUID requiredLessonId, HttpServletRequest http) {
        accessGuard.requireLessonEditor(id);
        lessons.addPrerequisite(id, requiredLessonId, currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/v1/teacher/lessons/{id}/prerequisites/{requiredLessonId}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Remove a lesson prerequisite")
    public ResponseEntity<Void> removePrerequisite(
            @PathVariable UUID id, @PathVariable UUID requiredLessonId, HttpServletRequest http) {
        accessGuard.requireLessonEditor(id);
        lessons.removePrerequisite(
                id, requiredLessonId, currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/v1/teacher/lessons/{id}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Soft-delete a lesson")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest http) {
        accessGuard.requireLessonEditor(id);
        lessons.softDelete(id, currentUser.require().id(), clientIp(http));
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
