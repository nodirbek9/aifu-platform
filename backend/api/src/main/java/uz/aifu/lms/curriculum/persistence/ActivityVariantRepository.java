package uz.aifu.lms.curriculum.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.curriculum.domain.ActivityVariant;

public interface ActivityVariantRepository extends JpaRepository<ActivityVariant, UUID> {

    List<ActivityVariant> findByActivity_IdOrderByVariantNo(UUID activityId);

    Optional<ActivityVariant> findByActivity_IdAndVariantNo(UUID activityId, int variantNo);

    long countByActivity_Id(UUID activityId);
}
