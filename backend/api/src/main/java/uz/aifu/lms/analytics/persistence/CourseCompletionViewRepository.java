package uz.aifu.lms.analytics.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;
import uz.aifu.lms.analytics.domain.CourseCompletionView;

/** Read-only by construction: {@link Repository}, not {@code JpaRepository} — no write methods. */
public interface CourseCompletionViewRepository extends Repository<CourseCompletionView, UUID> {
    Optional<CourseCompletionView> findByCourseId(UUID courseId);
}
