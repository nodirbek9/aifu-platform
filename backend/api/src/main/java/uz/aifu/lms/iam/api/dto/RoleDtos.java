package uz.aifu.lms.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Read-only role/permission catalogue contracts (Phase 10, ADR-004: roles are data, not code).
 * Assignment itself is {@code PUT /admin/users/{id}/roles} ({@link UserDtos.SetRolesRequest}) —
 * these views exist so an admin screen can show what a role code actually grants before picking it.
 */
public final class RoleDtos {

    private RoleDtos() {}

    @Schema(description = "One role and the permission codes it grants")
    public record RoleView(
            String code,
            String name,
            String description,
            boolean system,
            Set<String> permissions) {}

    @Schema(description = "One permission")
    public record PermissionView(String code, String description, String domain) {}
}
