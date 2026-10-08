package uz.aifu.lms.progress.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class LearningPathDtos {

    private LearningPathDtos() {}

    @Schema(description = "Request payload to configure a course learning path")
    public record LearningPathConfigRequest(
            @Schema(
                            description = "Whether server-side learning path gating is enabled",
                            example = "true")
                    boolean enabled,
            @Schema(
                            description =
                                    "Midterm QUIZ activity ID (optional, null if unconfigured)",
                            example = "b845ef20-8047-495c-9c97-6a422eb7813a")
                    UUID midtermActivityId,
            @Schema(
                            description =
                                    "Anchor lesson ID after which the midterm is placed (optional, null if unconfigured)",
                            example = "e87f7b2c-623b-48d9-a41e-355d14ff1841")
                    UUID midtermAfterLessonId,
            @Schema(
                            description =
                                    "When the midterm opens (optional; defaults to week 8 Monday 00:00 Asia/Tashkent)",
                            example = "2026-10-19T00:00:00+05:00")
                    Instant midtermOpensAt) {}

    @Schema(description = "Learning path configuration of a course")
    public record LearningPathConfigView(
            @Schema(
                            description = "Whether server-side learning path gating is enabled",
                            example = "true")
                    boolean enabled,
            @Schema(
                            description = "Midterm QUIZ activity ID",
                            example = "b845ef20-8047-495c-9c97-6a422eb7813a")
                    UUID midtermActivityId,
            @Schema(
                            description = "Anchor lesson ID after which the midterm is placed",
                            example = "e87f7b2c-623b-48d9-a41e-355d14ff1841")
                    UUID midtermAfterLessonId,
            @Schema(description = "When the midterm opens", example = "2026-10-19T00:00:00+05:00")
                    Instant midtermOpensAt,
            @Schema(
                            description = "When the learning path was enabled",
                            example = "2026-10-08T12:00:00Z")
                    Instant enabledAt,
            @Schema(
                            description = "Admin user ID who enabled the learning path",
                            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
                    UUID enabledBy,
            @Schema(
                            description =
                                    "Minimum reading fraction required to complete reading (default 0.50)",
                            example = "0.50")
                    BigDecimal minReadingFraction) {}
}
