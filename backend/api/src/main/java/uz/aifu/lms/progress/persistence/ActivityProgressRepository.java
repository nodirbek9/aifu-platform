package uz.aifu.lms.progress.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.progress.domain.ActivityProgress;
import uz.aifu.lms.progress.domain.ActivityProgressStatus;

public interface ActivityProgressRepository extends JpaRepository<ActivityProgress, UUID> {

    Optional<ActivityProgress> findByEnrollment_IdAndActivity_Id(
            UUID enrollmentId, UUID activityId);

    List<ActivityProgress> findByEnrollment_IdIn(Collection<UUID> enrollmentIds);

    /**
     * The numerator for {@code CourseProgress.activitiesCompleted}. Always zero until {@code
     * assessment}/{@code coding} (Phase 7/8) start writing rows here — see the class javadoc on
     * {@link ActivityProgress}.
     */
    @Query(
            "select count(ap) from ActivityProgress ap join ap.activity a "
                    + "where ap.enrollment.id = :enrollmentId and a.course.id = :courseId "
                    + "and ap.status = :completedStatus")
    long countCompletedInCourse(
            @Param("enrollmentId") UUID enrollmentId,
            @Param("courseId") UUID courseId,
            @Param("completedStatus") ActivityProgressStatus completedStatus);
}
