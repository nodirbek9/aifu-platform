package uz.aifu.lms.curriculum.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.curriculum.domain.ContentBlock;

public interface ContentBlockRepository extends JpaRepository<ContentBlock, UUID> {

    Optional<ContentBlock> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Unpaginated on purpose — see {@code CourseModuleRepository}'s equivalent note; also used to
     * render every block inline in one lesson's detail view (CLAUDE.md §9's pagination rule is
     * about top-level list endpoints, not a single parent's embedded child collection — same
     * precedent as {@code CourseDetail.sessionAllocations} in {@code academics}).
     */
    List<ContentBlock> findByLesson_IdAndDeletedAtIsNullOrderByPosition(UUID lessonId);

    Page<ContentBlock> findByLesson_IdAndDeletedAtIsNull(UUID lessonId, Pageable pageable);

    long countByLesson_IdAndDeletedAtIsNull(UUID lessonId);
}
