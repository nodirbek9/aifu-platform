package uz.aifu.lms.curriculum.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.curriculum.domain.LessonLearningOutcome;
import uz.aifu.lms.curriculum.domain.LessonLearningOutcomeId;

public interface LessonLearningOutcomeRepository
        extends JpaRepository<LessonLearningOutcome, LessonLearningOutcomeId> {

    @EntityGraph(attributePaths = "learningOutcome")
    List<LessonLearningOutcome> findByLesson_Id(UUID lessonId);

    boolean existsByLesson_IdAndLearningOutcome_Id(UUID lessonId, UUID learningOutcomeId);

    void deleteByLesson_IdAndLearningOutcome_Id(UUID lessonId, UUID learningOutcomeId);
}
