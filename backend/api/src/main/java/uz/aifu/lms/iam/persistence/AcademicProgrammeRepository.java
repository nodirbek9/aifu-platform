package uz.aifu.lms.iam.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.iam.domain.AcademicProgramme;

public interface AcademicProgrammeRepository extends JpaRepository<AcademicProgramme, UUID> {
    Optional<AcademicProgramme> findByCode(String code);
}
