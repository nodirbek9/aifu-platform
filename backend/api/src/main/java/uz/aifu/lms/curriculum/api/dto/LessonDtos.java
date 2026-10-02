package uz.aifu.lms.curriculum.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.curriculum.domain.Difficulty;

public final class LessonDtos {

    private LessonDtos() {}

    @Schema(description = "Create a lesson within a module. Admin/teacher only.")
    public record CreateLessonRequest(String code, @NotNull SessionType sessionType) {}

    public record UpdateLessonRequest(
            String code,
            @NotNull SessionType sessionType,
            BigDecimal allocatedHours,
            Integer estimatedMinutes,
            Difficulty difficulty,
            boolean isRequired,
            Instant availableFrom) {}

    public record UpsertLessonTranslationRequest(String title, String summary) {}

    public record ChangeLessonStatusRequest(@NotNull PublicationStatus status) {}

    public record ReorderLessonsRequest(@NotEmpty List<UUID> orderedLessonIds) {}

    @Schema(description = "Lesson summary for a module's lesson list")
    public record LessonSummary(
            UUID id,
            UUID moduleId,
            int position,
            String code,
            SessionType sessionType,
            PublicationStatus status,
            String title,
            boolean titleIsFallback) {}

    @Schema(description = "Full lesson detail, including its content blocks")
    public record LessonDetail(
            UUID id,
            UUID moduleId,
            int position,
            String code,
            SessionType sessionType,
            BigDecimal allocatedHours,
            Integer estimatedMinutes,
            Difficulty difficulty,
            boolean required,
            PublicationStatus status,
            Instant availableFrom,
            Instant publishedAt,
            List<LessonTranslationView> translations,
            List<UUID> learningOutcomeIds,
            List<UUID> prerequisiteLessonIds,
            List<ContentBlockDtos.ContentBlockView> blocks) {}

    public record LessonTranslationView(String locale, String title, String summary) {}
}
