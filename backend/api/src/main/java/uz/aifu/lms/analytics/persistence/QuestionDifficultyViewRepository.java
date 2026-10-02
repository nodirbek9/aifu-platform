package uz.aifu.lms.analytics.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.Repository;
import uz.aifu.lms.analytics.domain.QuestionDifficultyView;

public interface QuestionDifficultyViewRepository extends Repository<QuestionDifficultyView, UUID> {
    List<QuestionDifficultyView> findByQuizIdOrderByQuestionId(UUID quizId);
}
