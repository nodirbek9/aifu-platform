package uz.aifu.lms.progress.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.progress.domain.CourseLearningPath;

public interface CourseLearningPathRepository extends JpaRepository<CourseLearningPath, UUID> {

    Optional<CourseLearningPath> findByCourseId(UUID courseId);
}
