package uz.aifu.lms.enrollment.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.EnumSet;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentSummary;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.EnrollmentStatus;
import uz.aifu.lms.iam.app.access.AccessGuard;

/**
 * A teacher's own course roster. {@link AccessGuard#requireCourseEditor} is what actually restricts
 * a teacher to a course they are assigned to (skills/authorization/SKILL.md L2) — {@code
 * ENROLLMENT_READ} alone would let them read any course's roster.
 */
@RestController
@RequestMapping("/api/v1/teacher/courses/{courseId}/students")
@Tag(name = "Teacher: course roster", description = "A teacher's own course enrollments")
@SecurityRequirement(name = "bearerAuth")
public class TeacherCourseStudentsController {

    private final EnrollmentService enrollments;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;

    public TeacherCourseStudentsController(
            EnrollmentService enrollments,
            AccessGuard accessGuard,
            PageableSupport pageableSupport) {
        this.enrollments = enrollments;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ENROLLMENT_READ')")
    @Operation(
            summary =
                    "List the roster of a course the caller teaches (or any course, for an admin)")
    public PageResponse<EnrollmentSummary> roster(
            @PathVariable UUID courseId,
            @RequestParam(required = false) EnrollmentStatus status,
            Pageable pageable) {
        accessGuard.requireCourseEditor(courseId);
        var statuses = status == null ? null : EnumSet.of(status);
        var page = enrollments.listByCourse(courseId, statuses, pageableSupport.sanitize(pageable));
        return PageResponse.of(page, EnrollmentMapper::toSummary);
    }
}
