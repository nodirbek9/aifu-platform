package uz.aifu.lms.curriculum.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.curriculum.domain.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    @EntityGraph(attributePaths = {"module", "module.course"})
    Optional<Lesson> findByIdAndDeletedAtIsNull(UUID id);

    /** Unpaginated on purpose — see {@code CourseModuleRepository}'s equivalent note. */
    List<Lesson> findByModule_IdAndDeletedAtIsNullOrderByPosition(UUID moduleId);

    List<Lesson> findByModule_IdAndStatusAndDeletedAtIsNullOrderByPosition(
            UUID moduleId, PublicationStatus status);

    @EntityGraph(attributePaths = {"module", "module.course"})
    Page<Lesson> findByModule_IdAndDeletedAtIsNull(UUID moduleId, Pageable pageable);

    @EntityGraph(attributePaths = {"module", "module.course"})
    Page<Lesson> findByModule_IdAndStatusAndDeletedAtIsNull(
            UUID moduleId, PublicationStatus status, Pageable pageable);

    long countByModule_IdAndDeletedAtIsNull(UUID moduleId);

    /** Denominator for module-level progress roll-up (progress module, Phase 6). */
    long countByModule_IdAndRequiredTrueAndStatusAndDeletedAtIsNull(
            UUID moduleId, PublicationStatus status);

    /** Denominator for course-level progress roll-up (progress module, Phase 6). */
    long countByModule_Course_IdAndRequiredTrueAndStatusAndDeletedAtIsNull(
            UUID courseId, PublicationStatus status);

    /** Every lesson of a course in teaching order (module, then lesson position). */
    @Query(
            "select l from Lesson l join fetch l.module m where m.course.id = :courseId"
                    + " and l.deletedAt is null and m.deletedAt is null"
                    + " order by m.position, l.position")
    List<Lesson> findAllOfCourse(@Param("courseId") UUID courseId);

    @Query(
            "select l from Lesson l join fetch l.module m where l.id in :ids and l.deletedAt is null")
    List<Lesson> findAllWithModuleByIdIn(@Param("ids") Collection<UUID> ids);

    /** Backs {@code CurriculumLocatorAdapter.courseIdOfLesson} — the {@code AccessGuard} port. */
    @Query("select l.module.course.id from Lesson l where l.id = :id and l.deletedAt is null")
    Optional<UUID> findCourseIdById(@Param("id") UUID id);

    /**
     * The position after the last live item: positions may have gaps (a deleted item, a reorder),
     * so counting the items would hand out a position already taken.
     */
    @Query(
            "select coalesce(max(x.position), -1) + 1 from Lesson x where x.module.id = :parentId and x.deletedAt is null")
    int nextPosition(@Param("parentId") UUID parentId);
}
