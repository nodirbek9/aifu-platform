package uz.aifu.lms.assessment.app;

import java.util.List;
import uz.aifu.lms.assessment.domain.QuizAttempt;
import uz.aifu.lms.assessment.domain.QuizAttemptQuestion;

/** An attempt with the questions actually served to it, in serving order. */
public record AttemptView(QuizAttempt attempt, List<QuizAttemptQuestion> servedQuestions) {}
