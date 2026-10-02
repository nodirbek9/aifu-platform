package uz.aifu.lms.grading.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.grading.domain.Feedback;
import uz.aifu.lms.grading.domain.FeedbackTargetType;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    List<Feedback> findByTargetTypeAndTargetIdOrderByCreatedAt(
            FeedbackTargetType targetType, UUID targetId);
}
