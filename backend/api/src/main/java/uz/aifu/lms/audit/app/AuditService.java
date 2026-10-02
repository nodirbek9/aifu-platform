package uz.aifu.lms.audit.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.audit.domain.AuditLog;
import uz.aifu.lms.audit.domain.AuditOutcome;
import uz.aifu.lms.audit.persistence.AuditLogRepository;

/**
 * Single entry point for writing the audit trail (brief §27).
 *
 * <p><b>Transaction semantics.</b> Every write runs in {@link Propagation#REQUIRES_NEW}. A failed
 * login, a denied authorization or a rolled-back business transaction must still leave a trace, so
 * the audit insert deliberately does not share the caller's transaction and commits independently.
 *
 * <p><b>Failure semantics.</b> Auditing must never break the request it is observing. If the insert
 * throws, the exception is swallowed and logged at ERROR with the action name. This is a conscious
 * trade-off: losing an audit row is bad, but failing a student's quiz submission because the audit
 * table is unavailable is worse. Alert on the {@code audit.write.failed} log marker in production.
 *
 * <p><b>SECURITY — what must never be passed in {@code details}:</b> passwords, password hashes,
 * JWTs, refresh tokens or their hashes, session identifiers, student source code, hidden test-case
 * expectations, or any file contents. {@link #sanitizeKey} rejects obviously dangerous keys as a
 * backstop, but the real guarantee is the caller's discipline.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    /** Keys that must never reach the audit table, checked case-insensitively as substrings. */
    private static final String[] FORBIDDEN_DETAIL_KEYS = {
        "password", "passwd", "secret", "token", "jwt", "credential", "authorization",
        "sourcecode", "source_code", "solution", "apikey", "api_key", "privatekey", "private_key"
    };

    /** Hard cap on serialized details, so a stray payload cannot bloat the table. */
    private static final int MAX_DETAILS_CHARS = 4_000;

    /** Hard cap on a stored user-agent string. */
    private static final int MAX_USER_AGENT_CHARS = 512;

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AuditService(AuditLogRepository repository, ObjectMapper objectMapper, Clock clock) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    /**
     * Records a successful action.
     *
     * @param action what happened; never null
     * @param actorId the acting user, or null for anonymous/system actions
     * @param entityType logical type name of the affected object, e.g. {@code "Course"}
     * @param entityId identifier of the affected object, or null
     * @param ip client IP address, or null
     * @param userAgent client user agent, or null
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            AuditAction action,
            UUID actorId,
            String entityType,
            UUID entityId,
            String ip,
            String userAgent) {
        write(action, AuditOutcome.SUCCESS, actorId, entityType, entityId, ip, userAgent, Map.of());
    }

    /**
     * Records a successful action with extra structured context.
     *
     * @param details small, non-sensitive key/value context, e.g. {@code {"from":"DRAFT"}}
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            AuditAction action,
            UUID actorId,
            String entityType,
            UUID entityId,
            String ip,
            String userAgent,
            Map<String, ?> details) {
        write(action, AuditOutcome.SUCCESS, actorId, entityType, entityId, ip, userAgent, details);
    }

    /**
     * Records a failed attempt.
     *
     * @param reason short, non-sensitive explanation. For authentication this must stay coarse
     *     ({@code "bad_credentials"}, {@code "account_locked"}) and must never reveal whether the
     *     account exists to anyone but an administrator reading the trail.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(
            AuditAction action,
            UUID actorId,
            String entityType,
            UUID entityId,
            String ip,
            String userAgent,
            String reason) {
        Map<String, Object> details = new LinkedHashMap<>();
        if (reason != null && !reason.isBlank()) {
            details.put("reason", truncate(reason, 256));
        }
        write(action, AuditOutcome.FAILURE, actorId, entityType, entityId, ip, userAgent, details);
    }

    /** Records an authorization refusal. Called by the global access-denied handler. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordDenied(
            AuditAction action,
            UUID actorId,
            String entityType,
            UUID entityId,
            String ip,
            String userAgent,
            String requiredPermission) {
        Map<String, Object> details = new LinkedHashMap<>();
        if (requiredPermission != null) {
            details.put("required", truncate(requiredPermission, 128));
        }
        write(action, AuditOutcome.DENIED, actorId, entityType, entityId, ip, userAgent, details);
    }

    /**
     * Backs the admin audit browser (Phase 10). All filters optional; nulls widen the query. Builds
     * a {@link Specification} rather than a static JPQL "{@code :param is null or ...}" query — see
     * {@link AuditLogRepository}'s class javadoc for why a null-bound "is null" guard breaks
     * against Postgres.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> search(
            UUID actorUserId,
            AuditAction action,
            String entityType,
            UUID entityId,
            Instant from,
            Instant to,
            Pageable pageable) {
        List<Specification<AuditLog>> predicates = new ArrayList<>();
        if (actorUserId != null) {
            predicates.add((root, query, cb) -> cb.equal(root.get("actorUserId"), actorUserId));
        }
        if (action != null) {
            predicates.add((root, query, cb) -> cb.equal(root.get("action"), action));
        }
        if (entityType != null) {
            predicates.add((root, query, cb) -> cb.equal(root.get("entityType"), entityType));
        }
        if (entityId != null) {
            predicates.add((root, query, cb) -> cb.equal(root.get("entityId"), entityId));
        }
        if (from != null) {
            predicates.add(
                    (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
        }
        if (to != null) {
            predicates.add((root, query, cb) -> cb.lessThan(root.get("occurredAt"), to));
        }
        Specification<AuditLog> spec = Specification.allOf(predicates);
        return repository.findAll(spec, pageable);
    }

    private void write(
            AuditAction action,
            AuditOutcome outcome,
            UUID actorId,
            String entityType,
            UUID entityId,
            String ip,
            String userAgent,
            Map<String, ?> details) {
        try {
            AuditLog entry =
                    AuditLog.builder()
                            .occurredAt(clock.instant())
                            .action(action)
                            .outcome(outcome)
                            .actorUserId(actorId)
                            .actorLabel(MDC.get("userLabel"))
                            .entityType(truncate(entityType, 128))
                            .entityId(entityId)
                            .requestId(MDC.get("requestId"))
                            .ipAddress(normalizeIp(ip))
                            .userAgent(truncate(userAgent, MAX_USER_AGENT_CHARS))
                            .details(toJson(details))
                            .build();
            repository.save(entry);
        } catch (RuntimeException ex) {
            // Auditing must not break the observed request. See class javadoc.
            log.error("audit.write.failed action={} outcome={}", action, outcome, ex);
        }
    }

    /**
     * Serializes details, dropping any key that looks like a credential and capping total size.
     * Returns {@code "{}"} rather than throwing if serialization fails.
     */
    private String toJson(Map<String, ?> details) {
        if (details == null || details.isEmpty()) {
            return "{}";
        }
        Map<String, Object> safe = new LinkedHashMap<>();
        details.forEach(
                (key, value) -> {
                    if (key == null) {
                        return;
                    }
                    if (!sanitizeKey(key)) {
                        log.warn("audit.details.key_rejected key={}", key);
                        return;
                    }
                    safe.put(key, value instanceof String s ? truncate(s, 512) : value);
                });
        try {
            String json = objectMapper.writeValueAsString(safe);
            if (json.length() > MAX_DETAILS_CHARS) {
                log.warn("audit.details.oversized chars={}", json.length());
                return "{\"_truncated\":true}";
            }
            return json;
        } catch (JsonProcessingException ex) {
            log.error("audit.details.serialization_failed", ex);
            return "{\"_unserializable\":true}";
        }
    }

    /**
     * @return true when the key is safe to persist.
     */
    private static boolean sanitizeKey(String key) {
        String lower = key.toLowerCase();
        for (String forbidden : FORBIDDEN_DETAIL_KEYS) {
            if (lower.contains(forbidden)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Guards the {@code inet} column. Anything that is not a plausible address is dropped rather
     * than risking an insert failure on a malformed {@code X-Forwarded-For} value.
     */
    private static String normalizeIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return null;
        }
        String candidate = ip.trim();
        int comma = candidate.indexOf(',');
        if (comma > 0) {
            candidate = candidate.substring(0, comma).trim();
        }
        if (candidate.length() > 45 || !candidate.matches("[0-9A-Fa-f:.]+")) {
            return null;
        }
        return candidate;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
