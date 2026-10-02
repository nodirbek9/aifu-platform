package uz.aifu.lms.progress.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.iam.app.access.AccessGuard;
import uz.aifu.lms.progress.api.dto.ProgressDtos.StudentCourseProgressSummary;
import uz.aifu.lms.progress.app.ProgressService;

/**
 * A teacher's course-level progress roster — "which students are stuck" (brief §26). Unlike the two
 * student-facing endpoints in this module, this one genuinely needs "is the caller a teacher of
 * THIS course, or an admin", so it consumes {@code AccessGuard} directly (ADR-012, the second,
 * distinct reason {@code progress} is allowed to depend on {@code iam}).
 */
@RestController
@RequestMapping("/api/v1/teacher/courses/{courseId}/progress")
@Tag(name = "Teacher: course progress", description = "A teacher's own course progress roster")
@SecurityRequirement(name = "bearerAuth")
public class TeacherCourseProgressController {

    private final ProgressService progress;
    private final AccessGuard accessGuard;
    private final PageableSupport pageableSupport;

    public TeacherCourseProgressController(
            ProgressService progress, AccessGuard accessGuard, PageableSupport pageableSupport) {
        this.progress = progress;
        this.accessGuard = accessGuard;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROGRESS_COURSE_READ')")
    @Operation(
            summary =
                    "List course-level progress for every student in a course the caller teaches "
                            + "(or any course, for an admin)")
    public PageResponse<StudentCourseProgressSummary> roster(
            @PathVariable UUID courseId, Pageable pageable) {
        accessGuard.requireCourseEditor(courseId);
        var page = progress.courseProgressRoster(courseId, pageableSupport.sanitize(pageable));
        return PageResponse.of(page, ProgressMapper::toStudentSummary);
    }
}
