package uz.aifu.lms.assessment.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.assessment.domain.MatchMode;
import uz.aifu.lms.assessment.domain.QuestionType;
import uz.aifu.lms.curriculum.domain.Difficulty;

public final class QuestionDtos {

    private QuestionDtos() {}

    public record CreateQuestionRequest(
            @NotNull QuestionType questionType, @NotNull @Positive BigDecimal points) {}

    public record UpdateQuestionRequest(
            @NotNull @Positive BigDecimal points,
            Difficulty difficulty,
            boolean isRequired,
            JsonNode settings) {}

    public record ReorderQuestionsRequest(@NotEmpty List<UUID> orderedQuestionIds) {}

    public record UpsertQuestionTranslationRequest(
            String prompt, String explanation, String hint) {}

    public record CreateAnswerOptionRequest(boolean isCorrect, String matchKey) {}

    public record UpdateAnswerOptionRequest(boolean isCorrect, String matchKey) {}

    public record ReorderAnswerOptionsRequest(@NotEmpty List<UUID> orderedOptionIds) {}

    public record UpsertAnswerOptionTranslationRequest(String text, String feedback) {}

    public record CreateAnswerKeyRequest(
            @NotNull MatchMode matchMode,
            String expectedValue,
            BigDecimal tolerance,
            String locale) {}

    @Schema(description = "Teacher view of one answer option — includes isCorrect/matchKey")
    public record AnswerOptionView(
            UUID id,
            int position,
            boolean isCorrect,
            String matchKey,
            String text,
            String feedback,
            @Schema(description = "Every language the option exists in, for the editor")
                    List<OptionTranslationView> translations) {}

    public record OptionTranslationView(String locale, String text, String feedback) {}

    public record QuestionTranslationView(
            String locale, String prompt, String hint, String explanation) {}

    @Schema(description = "Teacher view of one answer key")
    public record AnswerKeyView(
            UUID id,
            MatchMode matchMode,
            String expectedValue,
            BigDecimal tolerance,
            String locale) {}

    @Schema(description = "Question summary for list views")
    public record QuestionSummary(
            UUID id,
            UUID quizId,
            int position,
            QuestionType questionType,
            BigDecimal points,
            Difficulty difficulty,
            boolean isRequired,
            String prompt) {}

    @Schema(description = "Full teacher-facing question detail, including correct answers")
    public record QuestionDetail(
            UUID id,
            UUID quizId,
            int position,
            QuestionType questionType,
            BigDecimal points,
            Difficulty difficulty,
            boolean isRequired,
            JsonNode settings,
            String prompt,
            String explanation,
            String hint,
            List<AnswerOptionView> options,
            List<AnswerKeyView> answerKeys,
            @Schema(description = "Every language the texts exist in, for the editor")
                    List<QuestionTranslationView> translations) {}
}
