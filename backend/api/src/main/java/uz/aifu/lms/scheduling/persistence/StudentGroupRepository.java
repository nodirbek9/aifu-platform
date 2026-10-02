package uz.aifu.lms.scheduling.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.scheduling.domain.StudentGroup;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, UUID> {

    List<StudentGroup> findAllByOrderByCode();

    Optional<StudentGroup> findByCode(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<StudentGroup> findByIdIn(Collection<UUID> ids);
}
