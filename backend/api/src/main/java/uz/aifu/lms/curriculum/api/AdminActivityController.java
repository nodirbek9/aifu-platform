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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.ActivityDetail;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.ChangeActivityStatusRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.CreateActivityRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.UpdateActivityRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.UpsertActivityTranslationRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.UpsertActivityVariantRequest;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * Activity administration — "anything a student DOES." {@code ACTIVITY_MANAGE} is granted to
 * TEACHER (V003); {@link AccessGuard#requireActivityEditor}/{@link AccessGuard#requireCourseEditor}
 * restrict a teacher to their own course (skills/authorization/SKILL.md L2).
 */
@RestController
@Tag(name = "Teacher: activities", description = "Activity administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminActivityController {

    private final ActivityService activities;
    private final AccessGuard accessGuard;
    private final CurrentUserProvider currentUser;
    private final PageableSupport pageableSupport;

    public AdminActivityController(
            ActivityService activities,
            AccessGuard accessGuard,
            CurrentUserProvider currentUser,
            PageableSupport pageableSupport) {
        this.activities = activities;
        this.accessGuard = accessGuard;
        this.currentUser = currentUser;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping("/api/v1/teacher/courses/{courseId}/activities")
    @PreAuthorize("hasAuthority('ACTIVITY_MANAGE')")
    @Operation(summary = "List a course's activities, any status")
    public PageResponse<ActivityDetail> list(@PathVariable UUID courseId, Pageable pageable) {
        accessGuard.requireCourseEditor(courseId);
        var page =
                activities.listByCourse(
                        courseId, pageableSupport.sanitize(pageable, Sort.by("position")));
        return PageResponse.of(page, a -> ActivityMapper.toDetail(a, activities));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/teacher/courses/{courseId}/activities")
    @PreAuthorize("hasAuthority('ACTIVITY_MANAGE')")
    @Operation(summary = "Create an activity, placed under a module or a lesson")
    public ResponseEntity<ActivityDetail> create(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateActivityRequest request,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(courseId);
        Activity created =
                activities.create(courseId, request, currentUser.require().id(), clientIp(http));
        return ResponseEntity.created(URI.create("/api/v1/teacher/activities/" + created.getId()))
                .body(ActivityMapper.toDetail(created, activities));
    }

    @GetMapping("/api/v1/teacher/activities/{id}")
    @PreAuthorize("hasAuthority('ACTIVITY_MANAGE')")
    @Operation(summary = "Get one activity, any status")
    public ActivityDetail get(@PathVariable UUID id) {
        accessGuard.requireActivityEditor(id);
        return ActivityMapper.toDetail(activities.require(id), activities);
    }

    @PatchMapping("/api/v1/teacher/activities/{id}")
    @PreAuthorize("hasAuthority('ACTIVITY_MANAGE')")
    @Operation(summary = "Update activity fields")
    public ActivityDetail update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateActivityRequest request,
            HttpServletRequest http) {
        accessGuard.requireActivityEditor(id);
        return ActivityMapper.toDetail(
                activities.updateDetails(id, request, currentUser.require().id(), clientIp(http)),
                activities);
    }

    @PutMapping("/api/v1/teacher/activities/{id}/status")
    @PreAuthorize("hasAuthority('ACTIVITY_MANAGE')")
    @Operation(summary = "Publish, archive or return an activity to draft")
    public ActivityDetail changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeActivityStatusRequest request,
            HttpServletRequest http) {
        accessGuard.requireActivityEditor(id);
        UUID actorId = currentUser.require().id();
        String ip = clientIp(http);
        Activity activity =
                switch (request.status()) {
                    case PUBLISHED -> activities.publish(id, actorId, ip);
                    case ARCHIVED -> activities.archive(id, actorId, ip);
                    case DRAFT -> activities.returnToDraft(id, actorId, ip);
                };
        return ActivityMapper.toDetail(activity, activities);
    }

    @PutMapping("/api/v1/teacher/activities/{id}/translations/{locale}")
    @PreAuthorize("hasAuthority('ACTIVITY_MANAGE')")
    @Operation(summary = "Upsert one locale's activity copy")
    public ActivityDetail upsertTranslation(
            @PathVariable UUID id,
            @PathVariable String locale,
            @Valid @RequestBody UpsertActivityTranslationRequest request,
            HttpServletRequest http) {
        accessGuard.requireActivityEditor(id);
        activities.upsertTranslation(
                id, locale, request, currentUser.require().id(), clientIp(http));
        return ActivityMapper.toDetail(activities.require(id), activities);
    }

    @PutMapping("/api/v1/teacher/activities/{id}/variants/{variantNo}")
    @PreAuthorize("hasAuthority('ACTIVITY_MANAGE')")
    @Operation(
            summary =
                    "Upsert one variant (DAS1110: \"mustaqil ish topshiriqlari variativ tavsifga ega bo'lishi lozim\")")
    public ActivityDetail upsertVariant(
            @PathVariable UUID id,
            @PathVariable int variantNo,
            @Valid @RequestBody UpsertActivityVariantRequest request,
            HttpServletRequest http) {
        accessGuard.requireActivityEditor(id);
        activities.upsertVariant(
                id, variantNo, request, currentUser.require().id(), clientIp(http));
        return ActivityMapper.toDetail(activities.require(id), activities);
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
