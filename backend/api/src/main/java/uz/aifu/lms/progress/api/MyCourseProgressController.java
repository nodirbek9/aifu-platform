package uz.aifu.lms.progress.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.progress.api.dto.ProgressDtos.CourseProgressView;
import uz.aifu.lms.progress.app.ProgressService;

/**
 * The caller's own course-level progress. Resolves to the caller's most recent enrollment in the
 * course (any status) — a completed or withdrawn student keeps read access to their own history,
 * same rule {@code AccessGuard} applies elsewhere, reached here without depending on {@code iam}
 * (ADR-012).
 */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/progress/me")
@Tag(name = "My progress", description = "The caller's own course progress")
@SecurityRequirement(name = "bearerAuth")
public class MyCourseProgressController {

    private final ProgressService progress;
    private final CurrentUserProvider currentUser;

    public MyCourseProgressController(ProgressService progress, CurrentUserProvider currentUser) {
        this.progress = progress;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROGRESS_SELF_READ')")
    @Operation(summary = "The caller's own progress in a course, with a per-module breakdown")
    public CourseProgressView myProgress(@PathVariable UUID courseId) {
        UUID studentUserId = currentUser.require().id();
        var snapshot = progress.courseProgressForStudent(courseId, studentUserId);
        var modules = progress.moduleProgressForStudent(courseId, studentUserId);
        return ProgressMapper.toCourseProgressView(snapshot, modules);
    }
}
