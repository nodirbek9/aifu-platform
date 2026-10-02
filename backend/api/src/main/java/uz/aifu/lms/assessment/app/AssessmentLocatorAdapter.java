package uz.aifu.lms.assessment.app;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.assessment.persistence.QuizRepository;
import uz.aifu.lms.iam.app.access.AssessmentLocator;

/** Real implementation of {@link AssessmentLocator} (ADR-013). */
@Component
public class AssessmentLocatorAdapter implements AssessmentLocator {

    private final QuizRepository quizzes;

    public AssessmentLocatorAdapter(QuizRepository quizzes) {
        this.quizzes = quizzes;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> courseIdOfQuiz(UUID quizId) {
        return quizzes.findCourseIdById(quizId);
    }
}
