package uz.aifu.lms.iam.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.aifu.lms.iam.domain.Permission;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /** Backs the admin role-management screen's permission catalogue (Phase 10). */
    @Query("select p from Permission p order by p.domain, p.code")
    List<Permission> findAllOrdered();
}
