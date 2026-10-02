package uz.aifu.lms.academics.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.academics.domain.AssessmentComponent;

public interface AssessmentComponentRepository extends JpaRepository<AssessmentComponent, UUID> {

    List<AssessmentComponent> findByCourse_IdOrderByDisplayOrder(UUID courseId);

    List<AssessmentComponent> findByCourse_IdAndParentIsNull(UUID courseId);

    boolean existsByCourse_IdAndCodeIgnoreCase(UUID courseId, String code);
}
