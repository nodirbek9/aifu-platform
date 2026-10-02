package uz.aifu.lms.assessment.app.grading;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uz.aifu.lms.assessment.domain.AnswerOption;
import uz.aifu.lms.assessment.domain.QuestionType;
import uz.aifu.lms.assessment.domain.QuizAttemptSelectedOption;

/**
 * {@code ORDERING}: the correct sequence is each {@link AnswerOption#getPosition()} (ascending);
 * the student's proposed sequence is {@link QuizAttemptSelectedOption#getOrdinal()}. All-or-nothing
 * by default; {@code settings.partialCredit=true} scores the fraction of options placed at their
 * correct position instead.
 */
@Component
public class OrderingGrader implements QuestionGrader {

    private final ObjectMapper objectMapper;

    public OrderingGrader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Set<QuestionType> supports() {
        return Set.of(QuestionType.ORDERING);
    }

    @Override
    public GradeResult grade(GradingContext context) {
        BigDecimal points = context.question().getPoints();
        var options = context.options();
        if (options.isEmpty()) {
            return new GradeResult(BigDecimal.ZERO, false);
        }
        Map<UUID, Integer> correctPositionById =
                options.stream()
                        .collect(Collectors.toMap(AnswerOption::getId, AnswerOption::getPosition));
        Map<UUID, Integer> submittedOrdinalById =
                context.selectedOptions().stream()
                        .filter(s -> s.getOrdinal() != null)
                        .collect(
                                Collectors.toMap(
                                        s -> s.getOption().getId(),
                                        QuizAttemptSelectedOption::getOrdinal));
        if (submittedOrdinalById.size() != options.size()) {
            return new GradeResult(BigDecimal.ZERO, false);
        }

        long correctCount =
                options.stream()
                        .filter(
                                o ->
                                        Objects.equals(
                                                submittedOrdinalById.get(o.getId()),
                                                correctPositionById.get(o.getId())))
                        .count();
        boolean allCorrect = correctCount == options.size();

        if (!QuestionSettingsJson.partialCredit(objectMapper, context.question().getSettings())) {
            return new GradeResult(allCorrect ? points : BigDecimal.ZERO, allCorrect);
        }
        BigDecimal fraction =
                BigDecimal.valueOf(correctCount)
                        .divide(BigDecimal.valueOf(options.size()), 6, RoundingMode.HALF_UP);
        BigDecimal awarded = points.multiply(fraction).setScale(2, RoundingMode.HALF_UP);
        return new GradeResult(awarded, allCorrect);
    }
}
