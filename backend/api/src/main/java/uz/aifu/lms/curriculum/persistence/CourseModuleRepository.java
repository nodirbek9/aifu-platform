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
import uz.aifu.lms.curriculum.domain.CourseModule;

public interface CourseModuleRepository extends JpaRepository<CourseModule, UUID> {

    /**
     * {@code course} is fetched eagerly because {@code ModuleService.require} is the single lookup
     * used everywhere a module's course is later read after this method's own transaction has
     * closed (mappers, and — critically — {@code LessonService.create}, which resolves the owning
     * module this way before building a {@code Lesson} whose {@code getModule().getCourse()} is
     * read back in the controller). Leaving {@code course} lazy here produced a real {@code
     * LazyInitializationException} on "create lesson" — see BUILD-STATUS.md.
     */
    @EntityGraph(attributePaths = "course")
    Optional<CourseModule> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Unpaginated on purpose: {@code ModuleService.reorder} needs every sibling at once to validate
     * the caller supplied exactly the current set. Never exposed as a list endpoint directly —
     * {@link #findByCourse_IdAndDeletedAtIsNull(UUID, Pageable)} is (CLAUDE.md §9).
     */
    List<CourseModule> findByCourse_IdAndDeletedAtIsNullOrderByPosition(UUID courseId);

    List<CourseModule> findByCourse_IdAndStatusAndDeletedAtIsNullOrderByPosition(
            UUID courseId, PublicationStatus status);

    @EntityGraph(attributePaths = "course")
    Page<CourseModule> findByCourse_IdAndDeletedAtIsNull(UUID courseId, Pageable pageable);

    @EntityGraph(attributePaths = "course")
    Page<CourseModule> findByCourse_IdAndStatusAndDeletedAtIsNull(
            UUID courseId, PublicationStatus status, Pageable pageable);

    long countByCourse_IdAndDeletedAtIsNull(UUID courseId);

    /** Backs {@code CurriculumLocatorAdapter.courseIdOfModule} — the {@code AccessGuard} port. */
    @Query("select m.course.id from CourseModule m where m.id = :id and m.deletedAt is null")
    Optional<UUID> findCourseIdById(@Param("id") UUID id);
}
