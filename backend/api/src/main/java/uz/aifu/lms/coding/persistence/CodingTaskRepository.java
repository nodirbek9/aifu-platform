package uz.aifu.lms.coding.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.coding.domain.CodingTask;
import uz.aifu.lms.common.domain.PublicationStatus;

public interface CodingTaskRepository extends JpaRepository<CodingTask, UUID> {

    @EntityGraph(attributePaths = "course")
    Optional<CodingTask> findWithCourseById(UUID id);

    @EntityGraph(attributePaths = "course")
    Page<CodingTask> findByCourse_Id(UUID courseId, Pageable pageable);

    @EntityGraph(attributePaths = "course")
    Page<CodingTask> findByCourse_IdAndStatus(
            UUID courseId, PublicationStatus status, Pageable pageable);

    /** Backs {@code CodingLocatorAdapter.courseIdOfCodingTask} — the {@code AccessGuard} port. */
    @Query("select t.course.id from CodingTask t where t.id = :id")
    Optional<UUID> findCourseIdById(@Param("id") UUID id);
}
