package uz.aifu.lms.iam.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.iam.domain.UserStatus;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Login lookup. Fetches roles and permissions eagerly via an entity graph so building the token
     * claims does not fire N+1 queries.
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query(
            "select u from User u where lower(u.username) = lower(:identifier) or lower(u.email) = lower(:identifier)")
    Optional<User> findByUsernameOrEmailWithAuthorities(@Param("identifier") String identifier);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findWithAuthoritiesById(UUID id);

    /**
     * Notes a successful sign-in on the user's row with one statement. Deliberately not a change of
     * the loaded entity: that would write the whole row under its version, and two sign-ins of one
     * account at the same instant (a phone and a laptop, a shared classroom account) would then
     * fail each other — one with a stale version, or both in a deadlock with the audit and
     * refresh-token rows they insert. This touches three columns, none of them a key, and waits its
     * turn.
     */
    @Modifying
    @Query(
            "update User u set u.failedLoginAttempts = 0, u.lockedUntil = null, u.lastLoginAt = :now where u.id = :id")
    void noteSuccessfulLogin(@Param("id") UUID id, @Param("now") Instant now);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    // Deliberately NO @EntityGraph on `roles` here: Hibernate hard-errors on a paginated query
    // that also fetch-joins a collection
    // (`hibernate.query.fail_on_pagination_over_collection_fetch`,
    // application.yml) because in-memory pagination over a fetched collection silently returns
    // wrong pages otherwise. Roles for a page of results are loaded separately via
    // findRoleCodesForUsers below.
    //
    // `pattern` is the already-formatted `%...%` LIKE pattern (or null), built in
    // UserService#search — NOT wrapped in concat() here. A null parameter bound through concat()
    // left Hibernate to guess its JDBC type, and it guessed bytea against Postgres.
    @Query(
            """
            select u from User u
            where (:status is null or u.status = :status)
              and (:role is null or exists (select 1 from u.roles r where r.code = :role))
              and (:pattern is null
                   or lower(u.fullName) like :pattern
                   or lower(u.email) like :pattern
                   or lower(u.username) like :pattern)
            """)
    Page<User> search(
            @Param("pattern") String pattern,
            @Param("status") UserStatus status,
            @Param("role") String role,
            Pageable pageable);

    /**
     * Batched role lookup for a page of users — one query, no N+1, no collection-fetch pagination.
     */
    @Query(
            "select u.id as userId, r.code as roleCode from User u join u.roles r where u.id in :userIds")
    List<UserRoleCodeProjection> findRoleCodesForUsers(@Param("userIds") Collection<UUID> userIds);

    @Query(
            """
            select count(u) > 0 from User u join u.roles r
            where u.id = :userId and r.code = :roleCode
            """)
    boolean hasRole(@Param("userId") UUID userId, @Param("roleCode") String roleCode);

    interface UserRoleCodeProjection {
        UUID getUserId();

        String getRoleCode();
    }
}
