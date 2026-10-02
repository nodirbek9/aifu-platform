package uz.aifu.lms.academics.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.academics.domain.GradeScale;

public interface GradeScaleRepository extends JpaRepository<GradeScale, UUID> {

    Optional<GradeScale> findByCode(String code);

    @EntityGraph(attributePaths = "bands")
    Optional<GradeScale> findWithBandsById(UUID id);
}
