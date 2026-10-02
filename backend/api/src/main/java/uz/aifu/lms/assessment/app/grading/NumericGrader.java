package uz.aifu.lms.assessment.app.grading;

import java.math.BigDecimal;
import java.util.Set;
import org.springframework.stereotype.Component;
import uz.aifu.lms.assessment.domain.MatchMode;
import uz.aifu.lms.assessment.domain.QuestionAnswerKey;
import uz.aifu.lms.assessment.domain.QuestionType;

/**
 * {@code NUMERIC}: the response number must equal (or, for {@code NUMERIC_TOLERANCE}, fall within
 * {@link QuestionAnswerKey#getTolerance()} of) at least one answer key.
 */
@Component
public class NumericGrader implements QuestionGrader {

    @Override
    public Set<QuestionType> supports() {
        return Set.of(QuestionType.NUMERIC);
    }

    @Override
    public GradeResult grade(GradingContext context) {
        BigDecimal response = context.attemptQuestion().getResponseNumber();
        if (response == null || context.answerKeys().isEmpty()) {
            return new GradeResult(BigDecimal.ZERO, false);
        }
        boolean correct = context.answerKeys().stream().anyMatch(key -> matches(key, response));
        return new GradeResult(correct ? context.question().getPoints() : BigDecimal.ZERO, correct);
    }

    private static boolean matches(QuestionAnswerKey key, BigDecimal response) {
        BigDecimal expected;
        try {
            expected = new BigDecimal(key.getExpectedValue());
        } catch (NumberFormatException e) {
            return false;
        }
        if (key.getMatchMode() == MatchMode.NUMERIC_TOLERANCE) {
            BigDecimal tolerance =
                    key.getTolerance() == null ? BigDecimal.ZERO : key.getTolerance();
            return response.subtract(expected).abs().compareTo(tolerance) <= 0;
        }
        return response.compareTo(expected) == 0;
    }
}
