package uz.aifu.lms.progress.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.curriculum.domain.ActivityType;
import uz.aifu.lms.progress.app.PathRules.LessonSteps;
import uz.aifu.lms.progress.app.PathRules.LockReason;
import uz.aifu.lms.progress.app.PathRules.StepState;
import uz.aifu.lms.progress.domain.ProgressStatus;

public final class ProgressDtos {

    private ProgressDtos() {}

    @Schema(description = "Reading heartbeat payload sent periodically by the client")
    public record ReadingBeatRequest(@Min(0) int maxBlockPosition, boolean atEnd) {}

    @Schema(description = "Learning path state of the lesson returned after a reading heartbeat")
    public record LessonPathView(
            LessonSteps steps,
            int activeReadingSeconds,
            int requiredReadingSeconds,
            boolean reachedEnd,
            boolean open,
            @Schema(description = "The lesson's TEST step activity; null when it has none")
                    UUID testActivityId,
            @Schema(description = "The lesson's PRACTICE step activity; null when it has none")
                    UUID practiceActivityId,
            @Schema(
                            description = "Why the lesson is locked; null when it is open",
                            example = "PREVIOUS_LESSON_INCOMPLETE")
                    LockReason lockReason) {}

    @Schema(description = "State of the three learning-path steps of a lesson")
    public record LessonStepsView(
            @Schema(example = "DONE") StepState lecture,
            @Schema(example = "TODO") StepState test,
            @Schema(example = "NONE") StepState practice) {}

    @Schema(description = "The course's midterm and the caller's standing on it")
    public record MidtermView(
            UUID activityId,
            @Schema(description = "The quiz behind the midterm activity; null for other types")
                    UUID quizId,
            @Schema(example = "2026-10-20T09:00:00Z") Instant opensAt,
            @Schema(description = "Lessons after this one need the midterm to be passed")
                    UUID afterLessonId,
            boolean passed,
            @Schema(example = "1") int attemptsUsed,
            @Schema(description = "null means unlimited", example = "2") Integer attemptsAllowed) {}

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

    // ------------------------------------------------------------------ course plan (spec S-03.2)

    public record PlanLesson(
            UUID id,
            String code,
            String title,
            SessionType sessionType,
            BigDecimal allocatedHours,
            Integer estimatedMinutes,
            boolean required,
            @Schema(description = "null for a viewer who is not a student of the course")
                    ProgressStatus status,
            @Schema(description = "true while a lesson this one requires is not completed")
                    boolean locked,
            List<UUID> prerequisiteLessonIds,
            @Schema(description = "Quizzes and tasks attached to the lesson") int activitiesTotal,
            int activitiesCompleted,
            @Schema(description = "null for a viewer who is not a student of the course")
                    LessonStepsView steps,
            @Schema(
                            description = "Set while locked by the learning path gate",
                            example = "PREVIOUS_LESSON_INCOMPLETE")
                    LockReason lockReason,
            @Schema(description = "true when a teacher waived the lesson") boolean waived) {}

    public record PlanModule(UUID id, String code, String title, List<PlanLesson> lessons) {}

    @Schema(
            description =
                    "A course's published modules and lessons with the caller's status on each, in"
                            + " one call. continueLessonId is the first lesson that is neither done"
                            + " nor locked.")
    public record CoursePlanView(
            UUID courseId,
            UUID enrollmentId,
            int lessonsTotal,
            int lessonsCompleted,
            UUID continueLessonId,
            @Schema(description = "The lesson the student opened most recently") UUID lastLessonId,
            List<PlanModule> modules,
            @Schema(description = "null when the course has no midterm configured")
                    MidtermView midterm) {}

    // ------------------------------------------------------------------ my work (spec S-07,
    // S-01.3)

    @Schema(
            description =
                    "A quiz, coding task or other activity of one of the caller's courses, with the"
                            + " caller's state on it.")
    public record MyActivityView(
            UUID activityId,
            UUID courseId,
            String subjectCode,
            String courseTitle,
            UUID lessonId,
            ActivityType type,
            @Schema(description = "The quiz or coding task to open; null for other types")
                    UUID referenceId,
            String title,
            Instant availableFrom,
            Instant dueAt,
            boolean graded,
            BigDecimal maxPoints,
            Integer attemptLimit,
            int attemptsUsed,
            BigDecimal bestScore,
            @Schema(allowableValues = {"UPCOMING", "TODO", "IN_PROGRESS", "COMPLETED", "OVERDUE"})
                    String state,
            Instant completedAt) {}

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
