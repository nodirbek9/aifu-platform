package uz.aifu.lms.enrollment.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.enrollment.domain.EnrollmentEvent;

public interface EnrollmentEventRepository extends JpaRepository<EnrollmentEvent, UUID> {

    List<EnrollmentEvent> findByEnrollment_IdOrderByOccurredAtAsc(UUID enrollmentId);
}
