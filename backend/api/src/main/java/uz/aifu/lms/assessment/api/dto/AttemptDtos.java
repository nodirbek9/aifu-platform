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
                    "One option as served to the student. What gives the answer away — correct,"
                            + " correctPosition, pairKey — is present only once the quiz's reveal"
                            + " policy opens the review of this attempt")
    public record ServedOptionView(
            UUID id,
            String text,
            @Schema(description = "Choice questions, in a review: whether this option is right")
                    Boolean correct,
            @Schema(
                            description =
                                    "Ordering questions, in a review: the option's place in the"
                                            + " right order, from 0")
                    Integer correctPosition,
            @Schema(
                            description =
                                    "Matching questions, in a review: options with the same key"
                                            + " belong together")
                    String pairKey) {}

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
            String explanation,
            @Schema(
                            description =
                                    "Short-answer and numeric questions, in a review: an accepted"
                                            + " answer")
                    String correctAnswer) {}

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
            @Schema(
                            description =
                                    "The server's clock when this answer was written. A countdown"
                                            + " to expiresAt is measured from here, not from the"
                                            + " clock of the student's computer")
                    Instant serverTime,
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
