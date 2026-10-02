package uz.aifu.lms.announcement.persistence;

import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.announcement.domain.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

    Page<Announcement> findByCourseIdOrderByCreatedAtDesc(UUID courseId, Pageable pageable);

    Page<Announcement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * What one person should see: announcements of the given courses or groups, and their own. Pass
     * a collection holding a random id instead of an empty one (an empty IN list is not portable).
     */
    @Query(
            "select a from Announcement a where a.courseId in :courseIds or a.groupId in :groupIds"
                    + " or a.authorUserId = :authorId order by a.createdAt desc")
    Page<Announcement> findVisible(
            @Param("courseIds") Collection<UUID> courseIds,
            @Param("groupIds") Collection<UUID> groupIds,
            @Param("authorId") UUID authorId,
            Pageable pageable);
}
