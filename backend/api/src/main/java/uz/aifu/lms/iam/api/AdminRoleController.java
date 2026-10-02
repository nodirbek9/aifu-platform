package uz.aifu.lms.iam.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.iam.api.dto.RoleDtos.PermissionView;
import uz.aifu.lms.iam.api.dto.RoleDtos.RoleView;
import uz.aifu.lms.iam.app.user.UserService;
import uz.aifu.lms.iam.domain.Permission;
import uz.aifu.lms.iam.domain.Role;

/**
 * Read-only role/permission catalogue (Phase 10). Gated by {@code ROLE_MANAGE} — the same
 * permission that already guards role assignment in {@link AdminUserController} — since ADR-004
 * treats "who may see what a role grants" and "who may grant it" as the same trust boundary; no
 * narrower {@code ROLE_READ} permission exists in V003 and CLAUDE.md §14.5 forbids inventing one.
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin: roles", description = "Role/permission catalogue (data, not code — ADR-004)")
@SecurityRequirement(name = "bearerAuth")
public class AdminRoleController {

    private final UserService users;

    public AdminRoleController(UserService users) {
        this.users = users;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Operation(summary = "List every role with the permission codes it grants")
    public List<RoleView> listRoles() {
        return users.listRoles().stream()
                .map(AdminRoleController::toView)
                .collect(Collectors.toList());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Operation(summary = "List the full permission catalogue")
    public List<PermissionView> listPermissions() {
        return users.listPermissions().stream()
                .map(p -> new PermissionView(p.getCode(), p.getDescription(), p.getDomain()))
                .collect(Collectors.toList());
    }

    private static RoleView toView(Role role) {
        return new RoleView(
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isSystem(),
                role.getPermissions().stream()
                        .map(Permission::getCode)
                        .collect(Collectors.toSet()));
    }
}
