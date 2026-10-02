package uz.aifu.lms.assessment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.assessment.domain.AttemptStatus;
import uz.aifu.lms.assessment.domain.QuestionType;

public final class AttemptDtos {

    private AttemptDtos() {}

    public record StartAttemptRequest(String locale, UUID activityId) {}

    public record SelectedOptionInput(UUID optionId, Integer ordinal) {}

    public record RecordAnswerRequest(
            String responseText,
            BigDecimal responseNumber,
            List<SelectedOptionInput> selectedOptions) {}

    @Schema(
            description =
                    "One selectable option as served to the student — never carries isCorrect")
    public record ServedOptionView(UUID id, String text) {}

    @Schema(description = "One served question, with the student's own recorded response")
    public record ServedQuestionView(
            UUID questionId,
            int servedPosition,
            QuestionType questionType,
            BigDecimal pointsPossible,
            String prompt,
            String hint,
            List<ServedOptionView> options,
            String responseText,
            BigDecimal responseNumber,
            List<UUID> selectedOptionIds,
            BigDecimal pointsAwarded,
            Boolean isCorrect,
            String explanation) {}

    @Schema(description = "The caller's own attempt, with served questions and their own responses")
    public record AttemptDetail(
            UUID id,
            UUID quizId,
            int attemptNumber,
            AttemptStatus status,
            BigDecimal score,
            BigDecimal maxScore,
            BigDecimal percent,
            Boolean passed,
            Instant startedAt,
            Instant expiresAt,
            Instant submittedAt,
            Instant gradedAt,
            List<ServedQuestionView> questions) {}

    @Schema(description = "Attempt summary for list views")
    public record AttemptSummary(
            UUID id,
            UUID quizId,
            int attemptNumber,
            AttemptStatus status,
            BigDecimal score,
            BigDecimal maxScore,
            BigDecimal percent,
            Boolean passed,
            Instant startedAt,
            Instant submittedAt) {}

    @Schema(description = "One student's attempt, for a teacher's roster view")
    public record StudentAttemptSummary(
            UUID id,
            UUID studentUserId,
            String studentFullName,
            int attemptNumber,
            AttemptStatus status,
            BigDecimal score,
            BigDecimal maxScore,
            BigDecimal percent,
            Boolean passed,
            Instant startedAt,
            Instant submittedAt) {}
}
