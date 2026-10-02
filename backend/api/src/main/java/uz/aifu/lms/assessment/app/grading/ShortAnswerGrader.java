package uz.aifu.lms.assessment.app.grading;

import java.math.BigDecimal;
import java.util.Set;
import org.springframework.stereotype.Component;
import uz.aifu.lms.assessment.domain.QuestionAnswerKey;
import uz.aifu.lms.assessment.domain.QuestionType;

/**
 * {@code SHORT_ANSWER}: the response text must match at least one {@link QuestionAnswerKey} —
 * multiple keys allow multiple acceptable spellings/locales for the same question.
 */
@Component
public class ShortAnswerGrader implements QuestionGrader {

    @Override
    public Set<QuestionType> supports() {
        return Set.of(QuestionType.SHORT_ANSWER);
    }

    @Override
    public GradeResult grade(GradingContext context) {
        String response = context.attemptQuestion().getResponseText();
        if (response == null || context.answerKeys().isEmpty()) {
            return new GradeResult(BigDecimal.ZERO, false);
        }
        boolean correct = context.answerKeys().stream().anyMatch(key -> matches(key, response));
        return new GradeResult(correct ? context.question().getPoints() : BigDecimal.ZERO, correct);
    }

    private static boolean matches(QuestionAnswerKey key, String response) {
        String expected = key.getExpectedValue();
        return switch (key.getMatchMode()) {
            case EXACT -> response.equals(expected);
            case CASE_INSENSITIVE -> response.equalsIgnoreCase(expected);
            case TRIMMED -> response.trim().equals(expected.trim());
            case REGEX -> response.matches(expected);
            case NUMERIC_TOLERANCE -> false;
        };
    }
}
