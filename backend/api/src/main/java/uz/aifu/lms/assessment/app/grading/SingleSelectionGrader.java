package uz.aifu.lms.assessment.app.grading;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import uz.aifu.lms.assessment.domain.QuestionType;
import uz.aifu.lms.assessment.domain.QuizAttemptSelectedOption;

/**
 * {@code SINGLE_CHOICE}, {@code TRUE_FALSE}: exactly one option is correct, exactly one option must
 * be selected. Any other submission shape (zero or more than one selected) is incorrect — never a
 * partial score, since there is nothing to be partial about with one right answer.
 */
@Component
public class SingleSelectionGrader implements QuestionGrader {

    @Override
    public Set<QuestionType> supports() {
        return Set.of(QuestionType.SINGLE_CHOICE, QuestionType.TRUE_FALSE);
    }

    @Override
    public GradeResult grade(GradingContext context) {
        List<QuizAttemptSelectedOption> selected = context.selectedOptions();
        if (selected.size() != 1) {
            return new GradeResult(BigDecimal.ZERO, false);
        }
        boolean correct = selected.get(0).getOption().isCorrect();
        return new GradeResult(correct ? context.question().getPoints() : BigDecimal.ZERO, correct);
    }
}
