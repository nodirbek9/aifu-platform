package uz.aifu.lms.assessment.app.grading;

import java.util.List;
import uz.aifu.lms.assessment.domain.AnswerOption;
import uz.aifu.lms.assessment.domain.Question;
import uz.aifu.lms.assessment.domain.QuestionAnswerKey;
import uz.aifu.lms.assessment.domain.QuizAttemptQuestion;
import uz.aifu.lms.assessment.domain.QuizAttemptSelectedOption;

/** Everything one {@link QuestionGrader} needs to score a single served question. */
public record GradingContext(
        Question question,
        List<AnswerOption> options,
        List<QuestionAnswerKey> answerKeys,
        QuizAttemptQuestion attemptQuestion,
        List<QuizAttemptSelectedOption> selectedOptions) {}
