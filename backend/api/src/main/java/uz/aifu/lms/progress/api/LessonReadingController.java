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
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.iam.app.access.AccessGuard;
import uz.aifu.lms.progress.api.dto.ProgressDtos.LessonPathView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.ReadingBeatRequest;
import uz.aifu.lms.progress.app.ReadingService;

/**
 * Endpoint for client-side reading heartbeats (Phase 5, B6, ADR-015).
 *
 * <p>Enforces the learning path gate via {@link AccessGuard#requireOpenLesson(UUID)}, credits
 * reading time measured by the server Clock, records sticky end-of-lesson status, and returns the
 * updated {@link LessonPathView}.
 */
@RestController
@RequestMapping("/api/v1/lessons/{lessonId}/reading")
@Tag(name = "Lesson reading", description = "Reading heartbeat tracking for the learning path")
@SecurityRequirement(name = "bearerAuth")
public class LessonReadingController {

    private final ReadingService readingService;
    private final AccessGuard accessGuard;
    private final CurrentUserProvider currentUser;

    public LessonReadingController(
            ReadingService readingService,
            AccessGuard accessGuard,
            CurrentUserProvider currentUser) {
        this.readingService = readingService;
        this.accessGuard = accessGuard;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROGRESS_SELF_WRITE')")
    @Operation(
            summary = "Record a reading heartbeat on a lesson",
            description =
                    "Tracks active reading time, sticky end-reached state, and reevaluates lesson progress")
    public LessonPathView recordReadingBeat(
            @PathVariable UUID lessonId, @Valid @RequestBody ReadingBeatRequest request) {
        accessGuard.requireOpenLesson(lessonId);
        return readingService.recordReadingBeat(lessonId, request, currentUser.require().id());
    }
}
