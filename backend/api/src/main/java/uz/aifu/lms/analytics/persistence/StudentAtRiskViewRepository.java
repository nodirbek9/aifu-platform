package uz.aifu.lms.analytics.persistence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import uz.aifu.lms.analytics.domain.StudentAtRiskView;

public interface StudentAtRiskViewRepository extends Repository<StudentAtRiskView, UUID> {
    Page<StudentAtRiskView> findByCourseId(UUID courseId, Pageable pageable);
}
