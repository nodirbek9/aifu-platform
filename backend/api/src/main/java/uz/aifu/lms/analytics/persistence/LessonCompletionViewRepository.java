package uz.aifu.lms.analytics.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.Repository;
import uz.aifu.lms.analytics.domain.LessonCompletionView;

public interface LessonCompletionViewRepository extends Repository<LessonCompletionView, UUID> {
    List<LessonCompletionView> findByCourseIdOrderByLessonId(UUID courseId);
}
