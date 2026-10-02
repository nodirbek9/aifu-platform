package uz.aifu.lms.audit.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.audit.domain.AuditLog;

/**
 * Read/append repository for the audit trail.
 *
 * <p>No delete or update method is declared, and the database rejects both anyway. Do not add one.
 *
 * <p>{@link JpaSpecificationExecutor} backs the admin audit browse ({@link
 * uz.aifu.lms.audit.app.AuditService#search}) instead of a static {@code @Query} with "{@code
 * :param is null or ...}" guards: a bare {@code ? is null} gives the Postgres extended protocol no
 * column context to infer that bind parameter's type from, and it rejects the whole query with
 * "could not determine data type of parameter $N" the moment any filter is actually left null — a
 * latent bug in this repository since it was never exercised by a real caller until {@code
 * AdminAuditController} (Phase 10). A {@code Specification} that only adds a predicate for filters
 * that are actually set never binds an unqualified null at all.
 */
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    /** Count of failed logins for one account since a cut-off, used by brute-force reporting. */
    @Query(
            """
            select count(a) from AuditLog a
            where a.action = uz.aifu.lms.audit.domain.AuditAction.LOGIN_FAILURE
              and a.entityId = :userId
              and a.occurredAt >= :since
            """)
    long countRecentLoginFailures(@Param("userId") UUID userId, @Param("since") Instant since);
}
