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
import uz.aifu.lms.iam.app.access.AccessGuard;
import uz.aifu.lms.iam.app.access.CourseAccess;
import uz.aifu.lms.progress.api.dto.ProgressDtos.LessonPathView;
import uz.aifu.lms.progress.app.LearningPathService;

/**
 * Read endpoint for one lesson's learning path state (Phase 5, B11, ADR-015): the three steps, the
 * reading counters and whether the lesson is open. Unlike the content and heartbeat endpoints it
 * answers for a locked lesson too, so the UI can explain why.
 */
@RestController
@RequestMapping("/api/v1/lessons/{lessonId}/path")
@Tag(name = "Lesson path", description = "Learning path state of a lesson")
@SecurityRequirement(name = "bearerAuth")
public class LessonPathController {

    private final LearningPathService learningPath;
    private final AccessGuard accessGuard;

    public LessonPathController(LearningPathService learningPath, AccessGuard accessGuard) {
        this.learningPath = learningPath;
        this.accessGuard = accessGuard;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COURSE_CONTENT_READ')")
    @Operation(
            summary = "The learning path state of a lesson for the caller",
            description =
                    "Steps (lecture, test, practice: DONE, TODO or NONE), reading seconds, whether"
                            + " the lesson is open and, when it is not, why. A teacher or admin"
                            + " previewing the course always gets open=true. Example: {\"steps\":"
                            + " {\"lecture\": \"DONE\", \"test\": \"TODO\", \"practice\":"
                            + " \"NONE\"}, \"activeReadingSeconds\": 240, \"requiredReadingSeconds\":"
                            + " 180, \"reachedEnd\": true, \"open\": true, \"testActivityId\":"
                            + " \"0b6e1c1e-6f0b-4b43-9d5e-3f2f6f1d9a10\", \"practiceActivityId\":"
                            + " null, \"lockReason\": null}")
    public LessonPathView get(@PathVariable UUID lessonId) {
        CourseAccess access = accessGuard.requireLessonViewer(lessonId);
        return learningPath.lessonPath(
                lessonId, access.courseId(), access.enrollment().orElse(null));
    }
}
