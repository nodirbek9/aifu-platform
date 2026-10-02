package uz.aifu.lms.audit.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.audit.api.dto.AuditDtos.AuditLogView;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.audit.domain.AuditLog;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;

/**
 * Admin audit browser (Phase 10, brief §27). {@code AUDIT_READ} is ADMIN-only (V003). The
 * underlying table is append-only ({@code trg_audit_log_no_update}/{@code trg_audit_log_no_delete})
 * — this controller is read-only by construction, there is nothing else it could expose.
 */
@RestController
@RequestMapping("/api/v1/admin/audit-log")
@Tag(name = "Admin: audit log", description = "Browse the append-only audit trail")
@SecurityRequirement(name = "bearerAuth")
public class AdminAuditController {

    private final AuditService audit;
    private final PageableSupport pageableSupport;

    public AdminAuditController(AuditService audit, PageableSupport pageableSupport) {
        this.audit = audit;
        this.pageableSupport = pageableSupport;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    @Operation(
            summary = "Search the audit trail",
            description =
                    "Every filter is optional; omitting all of them lists everything, newest first.")
    public PageResponse<AuditLogView> search(
            @RequestParam(required = false) UUID actorUserId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Instant to,
            Pageable pageable) {
        Page<AuditLog> page =
                audit.search(
                        actorUserId,
                        action,
                        entityType,
                        entityId,
                        from,
                        to,
                        pageableSupport.sanitize(
                                pageable,
                                Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id"))));
        return PageResponse.of(page, AdminAuditController::toView);
    }

    private static AuditLogView toView(AuditLog log) {
        return new AuditLogView(
                log.getId(),
                log.getOccurredAt(),
                log.getActorUserId(),
                log.getActorLabel(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getOutcome(),
                log.getRequestId(),
                log.getIpAddress(),
                log.getDetails());
    }
}
