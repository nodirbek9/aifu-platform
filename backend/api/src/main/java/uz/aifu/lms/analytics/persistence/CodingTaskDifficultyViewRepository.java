package uz.aifu.lms.analytics.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;
import uz.aifu.lms.analytics.domain.CodingTaskDifficultyView;

public interface CodingTaskDifficultyViewRepository
        extends Repository<CodingTaskDifficultyView, UUID> {
    Optional<CodingTaskDifficultyView> findByCodingTaskId(UUID codingTaskId);

    List<CodingTaskDifficultyView> findByCourseIdOrderByCodingTaskId(UUID courseId);
}
