package uz.aifu.lms.iam.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.iam.api.dto.AuthDtos.CurrentUserResponse;
import uz.aifu.lms.iam.app.user.UserService;
import uz.aifu.lms.iam.domain.User;

/**
 * "Who am I" for bootstrapping a dashboard (brief §8). Kept separate from {@link AuthController}
 * because it reads full account state (roles, permissions, locale, profile flags) rather than
 * issuing or revoking tokens.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login, token refresh, logout, password change")
@SecurityRequirement(name = "bearerAuth")
public class MeController {

    private final UserService users;
    private final CurrentUserProvider currentUser;

    public MeController(UserService users, CurrentUserProvider currentUser) {
        this.users = users;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get the current account",
            description =
                    "Full profile of the authenticated caller: roles, permissions and account flags.")
    public CurrentUserResponse me() {
        User user = users.require(currentUser.require().id());
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPreferredLocale().code(),
                user.roleCodes(),
                user.permissionCodes(),
                user.isMustChangePassword(),
                user.getLastLoginAt());
    }
}
