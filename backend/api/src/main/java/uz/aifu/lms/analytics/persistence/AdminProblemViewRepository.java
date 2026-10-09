package uz.aifu.lms.analytics.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import uz.aifu.lms.analytics.domain.AdminProblemView;
import uz.aifu.lms.analytics.domain.AdminProblemView.AdminProblemType;

/**
 * Read-only by construction: {@link Repository}, not {@code JpaRepository} — no write methods.
 * Filtering and pagination happen in SQL.
 */
public interface AdminProblemViewRepository extends Repository<AdminProblemView, String> {

    Optional<AdminProblemView> findById(String problemKey);

    Page<AdminProblemView> findAll(Pageable pageable);

    Page<AdminProblemView> findByType(AdminProblemType type, Pageable pageable);

    long count();

    long countByType(AdminProblemType type);
}
