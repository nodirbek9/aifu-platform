package uz.aifu.lms.iam.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.iam.api.dto.AuthDtos.CurrentUserResponse;
import uz.aifu.lms.iam.api.dto.AuthDtos.UpdateMyProfileRequest;
import uz.aifu.lms.iam.api.dto.UserDtos.UpdateUserRequest;
import uz.aifu.lms.iam.app.user.UserService;
import uz.aifu.lms.iam.domain.StudentProfile;
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
        return toResponse(users.require(currentUser.require().id()));
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Change my own interface language or phone",
            description =
                    "Null fields are left unchanged. The name is changed by an administrator.")
    public CurrentUserResponse updateMe(
            @Valid @RequestBody UpdateMyProfileRequest request, HttpServletRequest http) {
        UUID id = currentUser.require().id();
        if (request.preferredLocale() != null) {
            LocaleCode.require(request.preferredLocale());
        }
        users.updateProfile(
                id,
                new UpdateUserRequest(null, request.phone(), request.preferredLocale()),
                id,
                http.getRemoteAddr());
        return toResponse(users.require(id));
    }

    private CurrentUserResponse toResponse(User user) {
        Optional<StudentProfile> student = users.studentProfile(user.getId());
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPreferredLocale().code(),
                user.roleCodes(),
                user.permissionCodes(),
                user.isMustChangePassword(),
                user.getLastLoginAt(),
                user.getPhone(),
                student.map(StudentProfile::getStudentNumber).orElse(null),
                student.map(StudentProfile::getGroupCode).orElse(null));
    }
}
