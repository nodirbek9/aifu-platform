package uz.aifu.lms.progress.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.progress.api.dto.ProgressDtos.LessonProgressView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.RecordLessonProgressRequest;
import uz.aifu.lms.progress.app.ProgressService;
import uz.aifu.lms.progress.domain.ProgressStatus;

/**
 * A student recording their own progress on a lesson. No {@code AccessGuard} here (ADR-012):
 * ownership is "the caller's own ACTIVE enrollment in this lesson's course", resolved entirely
 * inside {@link ProgressService} through the caller's id — never a client-supplied enrollment id.
 */
@RestController
@RequestMapping("/api/v1/lessons/{lessonId}/progress")
@Tag(name = "My progress", description = "Recording the caller's own lesson progress")
@SecurityRequirement(name = "bearerAuth")
public class LessonProgressController {

    private final ProgressService progress;
    private final CurrentUserProvider currentUser;

    public LessonProgressController(ProgressService progress, CurrentUserProvider currentUser) {
        this.progress = progress;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROGRESS_SELF_WRITE')")
    @Operation(
            summary = "Record a lesson view or completion for the caller's own active enrollment",
            description =
                    "status=IN_PROGRESS records a view; status=COMPLETED marks it done. Completing an "
                            + "already-completed lesson again is a no-op for completedAt (idempotent).")
    public LessonProgressView record(
            @PathVariable UUID lessonId, @Valid @RequestBody RecordLessonProgressRequest request) {
        if (request.status() == ProgressStatus.NOT_STARTED) {
            throw Exceptions.validation("status must be IN_PROGRESS or COMPLETED");
        }
        long additionalTimeSpentSeconds =
                request.additionalTimeSpentSeconds() == null
                        ? 0L
                        : request.additionalTimeSpentSeconds();
        var saved =
                progress.recordLessonProgress(
                        lessonId,
                        request.status() == ProgressStatus.COMPLETED,
                        additionalTimeSpentSeconds,
                        currentUser.require().id());
        return ProgressMapper.toLessonProgressView(saved);
    }
}
