package uz.aifu.lms.assessment.app.grading;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import uz.aifu.lms.assessment.domain.AnswerOption;
import uz.aifu.lms.assessment.domain.QuestionType;
import uz.aifu.lms.assessment.domain.QuizAttemptSelectedOption;

/**
 * {@code ORDERING}: the correct sequence is the options by {@link AnswerOption#getPosition()}
 * (ascending); the student's proposed sequence is the options by {@link
 * QuizAttemptSelectedOption#getOrdinal()}. The two sequences are compared place by place, so the
 * numbers themselves do not matter — a student's client never learns the stored positions.
 * All-or-nothing by default; {@code settings.partialCredit=true} scores the fraction of options
 * standing in their correct place instead.
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
        List<UUID> correctOrder =
                options.stream()
                        .sorted(Comparator.comparingInt(AnswerOption::getPosition))
                        .map(AnswerOption::getId)
                        .toList();
        List<UUID> submittedOrder =
                context.selectedOptions().stream()
                        .filter(s -> s.getOrdinal() != null)
                        .sorted(Comparator.comparingInt(QuizAttemptSelectedOption::getOrdinal))
                        .map(s -> s.getOption().getId())
                        .distinct()
                        .toList();
        if (submittedOrder.size() != correctOrder.size()) {
            return new GradeResult(BigDecimal.ZERO, false);
        }

        long correctCount =
                IntStream.range(0, correctOrder.size())
                        .filter(i -> correctOrder.get(i).equals(submittedOrder.get(i)))
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
