package uz.aifu.lms.iam.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.aifu.lms.iam.domain.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findByCode(String code);

    /** Backs the admin role-management list — every role with its permission set, in one query. */
    @EntityGraph(attributePaths = "permissions")
    @Query("select r from Role r order by r.code")
    List<Role> findAllWithPermissions();
}
