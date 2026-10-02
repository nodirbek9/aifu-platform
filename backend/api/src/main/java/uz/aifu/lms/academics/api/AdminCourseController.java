package uz.aifu.lms.academics.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.academics.api.dto.CourseDtos.AssignTeacherRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.CourseDetail;
import uz.aifu.lms.academics.api.dto.CourseDtos.CourseSummary;
import uz.aifu.lms.academics.api.dto.CourseDtos.CreateAssessmentComponentRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.CreateCourseRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.SetSessionAllocationRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.UpdateCourseDeliveryRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.UpdateEnrollmentWindowRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.UpdateRequiredLocalesRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.UpsertCourseTranslationRequest;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.CourseStatus;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * Course delivery administration. {@code COURSE_MANAGE} is granted to both ADMIN and TEACHER (V003)
 * — {@link AccessGuard#requireCourseEditor} is what actually restricts a teacher to their OWN
 * assigned courses (skills/authorization/SKILL.md L2); the permission alone would not. Creating a
 * new course and everything calendar/teacher-roster-shaped stays admin-only.
 */
@RestController
@RequestMapping("/api/v1/admin/courses")
@Tag(name = "Admin: courses", description = "Course delivery administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminCourseController {

    private final CourseService courses;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;

    public AdminCourseController(
            CourseService courses,
            AccessGuard accessGuard,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser) {
        this.courses = courses;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Operation(summary = "Search the full course catalogue, any status. Admin only.")
    public PageResponse<CourseSummary> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) UUID subjectId,
            @RequestParam(required = false) UUID termId,
            Pageable pageable) {
        var statuses = status == null ? null : java.util.EnumSet.of(status);
        var page =
                courses.search(
                        query,
                        statuses,
                        subjectId,
                        termId,
                        "uz",
                        pageableSupport.sanitize(pageable));
        return PageResponse.of(
                page, c -> CourseMapper.toSummary(c, courses.listTranslations(c.getId()), "uz"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Operation(summary = "Get one course, any status. Caller must be admin or an assigned teacher.")
    public CourseDetail get(@PathVariable UUID id) {
        accessGuard.requireCourseEditor(id);
        return CourseMapper.toDetail(courses.require(id), courses);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Operation(summary = "Create one delivery of a subject in a term")
    public ResponseEntity<CourseDetail> create(
            @Valid @RequestBody CreateCourseRequest request, HttpServletRequest http) {
        Course created = courses.create(request, currentUser.require().id(), clientIp(http));
        return ResponseEntity.created(URI.create("/api/v1/admin/courses/" + created.getId()))
                .body(CourseMapper.toDetail(created, courses));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Operation(summary = "Update delivery details (cohort, contact hours, mode, form, programme)")
    public CourseDetail updateDelivery(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseDeliveryRequest request,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(id);
        return CourseMapper.toDetail(
                courses.updateDelivery(id, request, currentUser.require().id(), clientIp(http)),
                courses);
    }

    @PutMapping("/{id}/required-locales")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Operation(summary = "Set the locales required before this course may publish (ADR-005)")
    public CourseDetail updateRequiredLocales(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRequiredLocalesRequest request,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(id);
        return CourseMapper.toDetail(
                courses.updateRequiredLocales(
                        id, request.locales(), currentUser.require().id(), clientIp(http)),
                courses);
    }

    @PutMapping("/{id}/enrollment-window")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Operation(summary = "Set the enrollment window and capacity")
    public CourseDetail updateEnrollmentWindow(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEnrollmentWindowRequest request,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(id);
        return CourseMapper.toDetail(
                courses.updateEnrollmentWindow(
                        id, request, currentUser.require().id(), clientIp(http)),
                courses);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Operation(summary = "Publish, close, archive or return a course to draft")
    public CourseDetail changeStatus(
            @PathVariable UUID id, @RequestParam CourseStatus status, HttpServletRequest http) {
        accessGuard.requireCourseEditor(id);
        UUID actorId = currentUser.require().id();
        String ip = clientIp(http);
        Course course =
                switch (status) {
                    case PUBLISHED -> courses.publish(id, actorId, ip);
                    case CLOSED -> courses.close(id, actorId, ip);
                    case ARCHIVED -> courses.archive(id, actorId, ip);
                    case DRAFT -> courses.returnToDraft(id, actorId, ip);
                };
        return CourseMapper.toDetail(course, courses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Operation(summary = "Soft-delete a course. Admin only.")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest http) {
        courses.softDelete(id, currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/translations/{locale}")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Operation(summary = "Upsert one locale's course copy")
    public CourseDetail upsertTranslation(
            @PathVariable UUID id,
            @PathVariable String locale,
            @Valid @RequestBody UpsertCourseTranslationRequest request,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(id);
        courses.upsertTranslation(id, locale, request, currentUser.require().id(), clientIp(http));
        return CourseMapper.toDetail(courses.require(id), courses);
    }

    @PostMapping("/{id}/teachers")
    @PreAuthorize("hasAuthority('COURSE_TEACHER_MANAGE')")
    @Operation(summary = "Assign a teacher to the course. Admin only.")
    public CourseDetail assignTeacher(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTeacherRequest request,
            HttpServletRequest http) {
        courses.assignTeacher(id, request, currentUser.require().id(), clientIp(http));
        return CourseMapper.toDetail(courses.require(id), courses);
    }

    @DeleteMapping("/{id}/teachers/{teacherUserId}")
    @PreAuthorize("hasAuthority('COURSE_TEACHER_MANAGE')")
    @Operation(summary = "Remove a teacher from the course. Admin only.")
    public ResponseEntity<Void> removeTeacher(
            @PathVariable UUID id, @PathVariable UUID teacherUserId, HttpServletRequest http) {
        courses.removeTeacher(id, teacherUserId, currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/session-allocations")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Operation(summary = "Set the declared hour budget for one session type")
    public CourseDetail setSessionAllocation(
            @PathVariable UUID id,
            @Valid @RequestBody SetSessionAllocationRequest request,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(id);
        courses.setSessionAllocation(id, request, currentUser.require().id(), clientIp(http));
        return CourseMapper.toDetail(courses.require(id), courses);
    }

    @PostMapping("/{id}/assessment-components")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Operation(
            summary =
                    "Add a node to the assessment rubric tree (e.g. JN, then JN_ATTENDANCE under it)")
    public CourseDetail addAssessmentComponent(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAssessmentComponentRequest request,
            HttpServletRequest http) {
        accessGuard.requireCourseEditor(id);
        courses.addAssessmentComponent(id, request, currentUser.require().id(), clientIp(http));
        return CourseMapper.toDetail(courses.require(id), courses);
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
