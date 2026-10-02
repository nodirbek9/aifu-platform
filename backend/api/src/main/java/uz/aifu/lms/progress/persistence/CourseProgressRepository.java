package uz.aifu.lms.progress.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.progress.domain.CourseProgress;

/** {@code enrollment_id} is the primary key itself — {@link #findById} is the single-row lookup. */
public interface CourseProgressRepository extends JpaRepository<CourseProgress, UUID> {

    /** Backs the teacher roster view: one batched fetch instead of N. */
    List<CourseProgress> findByEnrollmentIdIn(Collection<UUID> enrollmentIds);
}
