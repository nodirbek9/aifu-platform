package uz.aifu.lms.assessment.app.grading;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uz.aifu.lms.assessment.domain.AnswerOption;
import uz.aifu.lms.assessment.domain.QuestionType;

/**
 * {@code MULTIPLE_CHOICE}: the selected set must equal the correct set. With {@code
 * settings.partialCredit=true} (V009 column comment), the score instead scales with {@code max(0,
 * correctSelected - incorrectSelected) / totalCorrect} — a standard "wrong picks cancel out right
 * picks, never go negative" partial-credit formula.
 */
@Component
public class MultipleChoiceGrader implements QuestionGrader {

    private final ObjectMapper objectMapper;

    public MultipleChoiceGrader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Set<QuestionType> supports() {
        return Set.of(QuestionType.MULTIPLE_CHOICE);
    }

    @Override
    public GradeResult grade(GradingContext context) {
        BigDecimal points = context.question().getPoints();
        Set<UUID> correctIds =
                context.options().stream()
                        .filter(AnswerOption::isCorrect)
                        .map(AnswerOption::getId)
                        .collect(Collectors.toSet());
        Set<UUID> selectedIds =
                context.selectedOptions().stream()
                        .map(o -> o.getOption().getId())
                        .collect(Collectors.toSet());
        if (correctIds.isEmpty()) {
            return new GradeResult(BigDecimal.ZERO, false);
        }
        boolean exactMatch = correctIds.equals(selectedIds);

        if (!QuestionSettingsJson.partialCredit(objectMapper, context.question().getSettings())) {
            return new GradeResult(exactMatch ? points : BigDecimal.ZERO, exactMatch);
        }
        long correctSelected = selectedIds.stream().filter(correctIds::contains).count();
        long incorrectSelected = selectedIds.size() - correctSelected;
        long netCorrect = Math.max(0, correctSelected - incorrectSelected);
        BigDecimal fraction =
                BigDecimal.valueOf(netCorrect)
                        .divide(BigDecimal.valueOf(correctIds.size()), 6, RoundingMode.HALF_UP);
        BigDecimal awarded = points.multiply(fraction).setScale(2, RoundingMode.HALF_UP);
        return new GradeResult(awarded, exactMatch);
    }
}
