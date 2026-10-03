package uz.aifu.lms.assessment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.assessment.domain.RevealPolicy;
import uz.aifu.lms.common.domain.PublicationStatus;

public final class QuizDtos {

    private QuizDtos() {}

    public record CreateQuizRequest(String code) {}

    public record UpdateQuizRequest(
            String code,
            @PositiveOrZero BigDecimal maxPoints,
            @Min(0) @Max(100) BigDecimal passingPercent,
            Integer timeLimitSeconds,
            Integer maxAttempts,
            Integer questionPoolSize,
            boolean shuffleQuestions,
            boolean shuffleOptions,
            RevealPolicy revealPolicy) {}

    public record ChangeQuizStatusRequest(@NotNull PublicationStatus status) {}

    public record UpsertQuizTranslationRequest(
            String title, String description, String instructions) {}

    @Schema(description = "Quiz summary for list views")
    public record QuizSummary(
            UUID id,
            UUID courseId,
            String code,
            PublicationStatus status,
            BigDecimal maxPoints,
            Integer maxAttempts,
            String title,
            boolean titleIsFallback) {}

    @Schema(description = "Full quiz detail, teacher/admin view (includes questions)")
    public record QuizDetail(
            UUID id,
            UUID courseId,
            String code,
            PublicationStatus status,
            BigDecimal maxPoints,
            BigDecimal passingPercent,
            Integer timeLimitSeconds,
            Integer maxAttempts,
            Integer questionPoolSize,
            boolean shuffleQuestions,
            boolean shuffleOptions,
            RevealPolicy revealPolicy,
            Instant publishedAt,
            String title,
            String description,
            String instructions,
            boolean titleIsFallback,
            @Schema(description = "Every language the texts exist in, for the editor")
                    List<QuizTranslationView> translations) {}

    public record QuizTranslationView(
            String locale, String title, String description, String instructions) {}
}
