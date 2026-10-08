package uz.aifu.lms.curriculum.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.curriculum.domain.ActivityType;
import uz.aifu.lms.curriculum.domain.CollaborationMode;
import uz.aifu.lms.curriculum.domain.PathStep;
import uz.aifu.lms.curriculum.domain.VariantStrategy;

public final class ActivityDtos {

    private ActivityDtos() {}

    @Schema(
            description =
                    "Create an activity — placed under a module OR a lesson. Admin/teacher only.")
    public record CreateActivityRequest(
            UUID moduleId,
            UUID lessonId,
            @NotNull ActivityType activityType,
            boolean isGraded,
            UUID assessmentComponentId,
            BigDecimal maxPoints,
            @Schema(
                            description =
                                    "Learning path role of the activity (TEST, PRACTICE: need a lesson; MIDTERM: a course-level QUIZ); null for ordinary activities")
                    PathStep pathStep) {
        public CreateActivityRequest(
                UUID moduleId,
                UUID lessonId,
                ActivityType activityType,
                boolean isGraded,
                UUID assessmentComponentId,
                BigDecimal maxPoints) {
            this(
                    moduleId,
                    lessonId,
                    activityType,
                    isGraded,
                    assessmentComponentId,
                    maxPoints,
                    null);
        }
    }

    public record UpdateActivityRequest(
            UUID referenceId,
            BigDecimal weight,
            Integer attemptLimit,
            CollaborationMode collaborationMode,
            VariantStrategy variantStrategy,
            Instant availableFrom,
            Instant dueAt) {}

    public record UpsertActivityTranslationRequest(String title, String instructions) {}

    public record ChangeActivityStatusRequest(@NotNull PublicationStatus status) {}

    public record UpsertActivityVariantRequest(
            UUID referenceId, com.fasterxml.jackson.databind.JsonNode payload) {}

    @Schema(description = "Activity summary")
    public record ActivitySummary(
            UUID id,
            UUID courseId,
            UUID moduleId,
            UUID lessonId,
            int position,
            ActivityType activityType,
            PublicationStatus status,
            boolean graded,
            String title,
            boolean titleIsFallback) {}

    @Schema(description = "Full activity detail")
    public record ActivityDetail(
            UUID id,
            UUID courseId,
            UUID moduleId,
            UUID lessonId,
            int position,
            ActivityType activityType,
            UUID referenceId,
            UUID assessmentComponentId,
            boolean graded,
            BigDecimal maxPoints,
            BigDecimal weight,
            Integer attemptLimit,
            CollaborationMode collaborationMode,
            VariantStrategy variantStrategy,
            Instant availableFrom,
            Instant dueAt,
            PathStep pathStep,
            PublicationStatus status,
            List<ActivityTranslationView> translations,
            List<ActivityVariantView> variants) {}

    public record ActivityTranslationView(String locale, String title, String instructions) {}

    public record ActivityVariantView(UUID id, int variantNo, UUID referenceId) {}
}
