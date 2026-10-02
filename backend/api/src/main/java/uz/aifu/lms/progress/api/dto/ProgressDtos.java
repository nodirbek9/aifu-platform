package uz.aifu.lms.progress.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.progress.domain.ProgressStatus;

public final class ProgressDtos {

    private ProgressDtos() {}

    @Schema(
            description =
                    "Record a lesson view or completion for the caller's own active enrollment. "
                            + "status must be IN_PROGRESS or COMPLETED.")
    public record RecordLessonProgressRequest(
            @NotNull ProgressStatus status, @PositiveOrZero Long additionalTimeSpentSeconds) {}

    @Schema(description = "The caller's progress on one lesson, after recording it")
    public record LessonProgressView(
            UUID lessonId,
            ProgressStatus status,
            int viewCount,
            long timeSpentSeconds,
            Instant firstAccessedAt,
            Instant lastAccessedAt,
            Instant completedAt) {}

    @Schema(description = "One module's roll-up within the caller's course progress")
    public record ModuleProgressView(
            UUID moduleId,
            ProgressStatus status,
            BigDecimal completionPercent,
            Instant startedAt,
            Instant lastAccessedAt,
            Instant completedAt) {}

    @Schema(description = "The caller's own course-level progress, with a per-module breakdown")
    public record CourseProgressView(
            UUID enrollmentId,
            UUID courseId,
            ProgressStatus status,
            BigDecimal completionPercent,
            int lessonsTotal,
            int lessonsCompleted,
            int activitiesTotal,
            int activitiesCompleted,
            long timeSpentSeconds,
            Instant startedAt,
            Instant lastAccessedAt,
            Instant completedAt,
            List<ModuleProgressView> modules) {}

    @Schema(description = "One student's course-level progress, for a teacher's roster view")
    public record StudentCourseProgressSummary(
            UUID enrollmentId,
            UUID studentUserId,
            String studentFullName,
            int attemptNo,
            ProgressStatus status,
            BigDecimal completionPercent,
            int lessonsTotal,
            int lessonsCompleted,
            Instant lastAccessedAt) {}
}
