package uz.aifu.lms.curriculum.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ChangeModuleStatusRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.CreateModuleRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ModuleDetail;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ReorderModulesRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.UpdateModuleRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.UpsertModuleTranslationRequest;
import uz.aifu.lms.curriculum.app.ModuleService;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * Module administration. {@code COURSE_CONTENT_WRITE} is granted to TEACHER (V003), and {@link
 * AccessGuard#requireCourseEditor}/{@link AccessGuard#requireModuleEditor} are what actually
 * restrict a teacher to a course they are assigned to (skills/authorization/SKILL.md L2).
 */
@RestController
@Tag(name = "Teacher: modules", description = "Course module administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminModuleController {

    private final ModuleService modules;
    private final AccessGuard accessGuard;
    private final CurrentUserProvider currentUser;
    private final PageableSupport pageableSupport;

    public AdminModuleController(
            ModuleService modules,
            AccessGuard accessGuard,
            CurrentUserProvider currentUser,
            PageableSupport pageableSupport) {
        this.modules = modules;
        this.accessGuard = accessGuard;
        this.currentUser = currentUser;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/modules")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "List a course's modules, any status")
    public PageResponse<ModuleDetail> list(@PathVariable UUID courseId, Pageable pageable) {
        accessGuard.requireCourseEditor(courseId);
        var page =
                modules.listByCourse(
                        courseId, pageableSupport.sanitize(pageable, Sort.by("position")));
        return PageResponse.of(page, m -> ModuleMapper.toDetail(m, modules));
    }

    @PostMapping("/api/v1/teacher/courses/{courseId}/modules")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Create a module within a course")
    public ResponseEntity<ModuleDetail> create(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateModuleRequest request,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(courseId);
        CourseModule created =
                modules.create(courseId, request, currentUser.require().id(), clientIp(http));
        return ResponseEntity.created(URI.create("/api/v1/teacher/modules/" + created.getId()))
                .body(ModuleMapper.toDetail(created, modules));
    }

    @PutMapping("/api/v1/teacher/courses/{courseId}/modules/reorder")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Reorder a course's modules")
    public ResponseEntity<Void> reorder(
            @PathVariable UUID courseId,
            @Valid @RequestBody ReorderModulesRequest request,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(courseId);
        modules.reorder(
                courseId, request.orderedModuleIds(), currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/teacher/modules/{id}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Get one module, any status")
    public ModuleDetail get(@PathVariable UUID id) {
        accessGuard.requireModuleEditor(id);
        return ModuleMapper.toDetail(modules.require(id), modules);
    }

    @PatchMapping("/api/v1/teacher/modules/{id}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Update module fields")
    public ModuleDetail update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateModuleRequest request,
            HttpServletRequest http) {
        accessGuard.requireModuleEditor(id);
        return ModuleMapper.toDetail(
                modules.updateDetails(id, request, currentUser.require().id(), clientIp(http)),
                modules);
    }

    @PutMapping("/api/v1/teacher/modules/{id}/status")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(
            summary = "Publish, archive or return a module to draft",
            description =
                    "PUBLISHED requires a translation for the course's default instruction language.")
    public ModuleDetail changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeModuleStatusRequest request,
            HttpServletRequest http) {
        accessGuard.requireModuleEditor(id);
        UUID actorId = currentUser.require().id();
        String ip = clientIp(http);
        CourseModule module =
                switch (request.status()) {
                    case PUBLISHED -> modules.publish(id, actorId, ip);
                    case ARCHIVED -> modules.archive(id, actorId, ip);
                    case DRAFT -> modules.returnToDraft(id, actorId, ip);
                };
        return ModuleMapper.toDetail(module, modules);
    }

    @PutMapping("/api/v1/teacher/modules/{id}/translations/{locale}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Upsert one locale's module copy")
    public ModuleDetail upsertTranslation(
            @PathVariable UUID id,
            @PathVariable String locale,
            @Valid @RequestBody UpsertModuleTranslationRequest request,
            HttpServletRequest http) {
        accessGuard.requireModuleEditor(id);
        modules.upsertTranslation(id, locale, request, currentUser.require().id(), clientIp(http));
        return ModuleMapper.toDetail(modules.require(id), modules);
    }

    @DeleteMapping("/api/v1/teacher/modules/{id}")
    @PreAuthorize("hasAuthority('COURSE_CONTENT_WRITE')")
    @Operation(summary = "Soft-delete a module")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest http) {
        accessGuard.requireModuleEditor(id);
        modules.softDelete(id, currentUser.require().id(), clientIp(http));
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
