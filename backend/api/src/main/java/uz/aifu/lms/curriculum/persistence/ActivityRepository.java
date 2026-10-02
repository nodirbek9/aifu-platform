package uz.aifu.lms.curriculum.persistence;

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
import uz.aifu.lms.curriculum.domain.Activity;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    @EntityGraph(attributePaths = {"course", "module", "lesson", "assessmentComponent"})
    Optional<Activity> findWithDetailsById(UUID id);

    /** Unpaginated on purpose — see {@code CourseModuleRepository}'s equivalent note. */
    List<Activity> findByCourse_IdOrderByPosition(UUID courseId);

    List<Activity> findByModule_IdOrderByPosition(UUID moduleId);

    List<Activity> findByLesson_IdOrderByPosition(UUID lessonId);

    @EntityGraph(attributePaths = {"course", "module", "lesson", "assessmentComponent"})
    Page<Activity> findByCourse_Id(UUID courseId, Pageable pageable);

    long countByModule_Id(UUID moduleId);

    long countByLesson_Id(UUID lessonId);

    /** Denominator for course-level progress roll-up (progress module, Phase 6). */
    long countByCourse_IdAndStatus(UUID courseId, PublicationStatus status);

    /** Backs {@code CurriculumLocatorAdapter.courseIdOfActivity} — the {@code AccessGuard} port. */
    @Query("select a.course.id from Activity a where a.id = :id")
    Optional<UUID> findCourseIdById(@Param("id") UUID id);
}
