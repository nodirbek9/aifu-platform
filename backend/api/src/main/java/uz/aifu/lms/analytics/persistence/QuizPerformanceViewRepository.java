package uz.aifu.lms.analytics.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;
import uz.aifu.lms.analytics.domain.QuizPerformanceView;

public interface QuizPerformanceViewRepository extends Repository<QuizPerformanceView, UUID> {
    Optional<QuizPerformanceView> findByQuizId(UUID quizId);

    List<QuizPerformanceView> findByCourseIdOrderByQuizId(UUID courseId);
}
