package uz.aifu.lms.academics.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.academics.domain.AcademicYear;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {

    Optional<AcademicYear> findByCode(String code);

    Optional<AcademicYear> findByCurrentTrue();

    List<AcademicYear> findAllByOrderByStartsOnDesc();

    boolean existsByCode(String code);
}
