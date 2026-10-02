package uz.aifu.lms.iam.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.iam.domain.Department;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByCode(String code);
}
