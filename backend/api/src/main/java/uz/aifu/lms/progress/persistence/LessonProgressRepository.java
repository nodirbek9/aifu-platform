package uz.aifu.lms.progress.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.progress.domain.LessonProgress;
import uz.aifu.lms.progress.domain.ProgressStatus;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    Optional<LessonProgress> findByEnrollment_IdAndLesson_Id(UUID enrollmentId, UUID lessonId);

    /** Everything one student has opened in a course: a few dozen rows at most. */
    List<LessonProgress> findByEnrollment_Id(UUID enrollmentId);

    /**
     * The numerator for {@code ModuleProgress.completionPercent}: navigates this entity's own
     * {@code lesson} association (a permitted domain read-only reference, same pattern as {@code
     * Enrollment.course}) rather than reaching into curriculum's persistence package.
     */
    @Query(
            "select count(lp) from LessonProgress lp join lp.lesson l "
                    + "where lp.enrollment.id = :enrollmentId and l.module.id = :moduleId "
                    + "and l.required = true and l.status = :publishedStatus and l.deletedAt is null "
                    + "and lp.status = :completedStatus")
    long countCompletedRequiredInModule(
            @Param("enrollmentId") UUID enrollmentId,
            @Param("moduleId") UUID moduleId,
            @Param("publishedStatus") PublicationStatus publishedStatus,
            @Param("completedStatus") ProgressStatus completedStatus);

    /** The numerator for {@code CourseProgress.lessonsCompleted}. */
    @Query(
            "select count(lp) from LessonProgress lp join lp.lesson l "
                    + "where lp.enrollment.id = :enrollmentId and l.module.course.id = :courseId "
                    + "and l.required = true and l.status = :publishedStatus and l.deletedAt is null "
                    + "and lp.status = :completedStatus")
    long countCompletedRequiredInCourse(
            @Param("enrollmentId") UUID enrollmentId,
            @Param("courseId") UUID courseId,
            @Param("publishedStatus") PublicationStatus publishedStatus,
            @Param("completedStatus") ProgressStatus completedStatus);

    /**
     * Checks if any other lesson belonging to the same enrollment has received a heartbeat since
     * the given cutoff. Used by the reading heartbeat to enforce single-clock reading across
     * parallel tabs.
     */
    @Query(
            "select case when count(lp) > 0 then true else false end from LessonProgress lp "
                    + "where lp.enrollment.id = :enrollmentId and lp.lesson.id <> :lessonId "
                    + "and lp.lastHeartbeatAt is not null and lp.lastHeartbeatAt >= :since")
    boolean hasOtherHeartbeatSince(
            @Param("enrollmentId") UUID enrollmentId,
            @Param("lessonId") UUID lessonId,
            @Param("since") Instant since);
}
