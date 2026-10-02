package uz.aifu.lms.audit.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.audit.domain.AuditOutcome;

public final class AuditDtos {

    private AuditDtos() {}

    @Schema(description = "One audit trail entry")
    public record AuditLogView(
            Long id,
            Instant occurredAt,
            UUID actorUserId,
            String actorLabel,
            AuditAction action,
            String entityType,
            UUID entityId,
            AuditOutcome outcome,
            String requestId,
            String ipAddress,
            String detailsJson) {}
}
