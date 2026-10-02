package uz.aifu.lms.scheduling.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.scheduling.domain.TeachingLoadItem;

public interface TeachingLoadItemRepository extends JpaRepository<TeachingLoadItem, UUID> {

    @EntityGraph(attributePaths = "group")
    List<TeachingLoadItem> findByTermId(UUID termId);

    Optional<TeachingLoadItem> findByTermIdAndCourseIdAndGroup_IdAndSessionType(
            UUID termId, UUID courseId, UUID groupId, SessionType sessionType);
}
