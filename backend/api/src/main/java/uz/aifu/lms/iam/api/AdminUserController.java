package uz.aifu.lms.iam.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.iam.api.dto.UserDtos.ChangeStatusRequest;
import uz.aifu.lms.iam.api.dto.UserDtos.CreateUserRequest;
import uz.aifu.lms.iam.api.dto.UserDtos.ResetPasswordRequest;
import uz.aifu.lms.iam.api.dto.UserDtos.SetRolesRequest;
import uz.aifu.lms.iam.api.dto.UserDtos.StudentProfileView;
import uz.aifu.lms.iam.api.dto.UserDtos.TeacherProfileView;
import uz.aifu.lms.iam.api.dto.UserDtos.UpdateUserRequest;
import uz.aifu.lms.iam.api.dto.UserDtos.UserDetail;
import uz.aifu.lms.iam.api.dto.UserDtos.UserSummary;
import uz.aifu.lms.iam.app.user.UserService;
import uz.aifu.lms.iam.domain.StudentProfile;
import uz.aifu.lms.iam.domain.TeacherProfile;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.iam.domain.UserStatus;

/**
 * Account administration (brief §22, CLAUDE.md §9).
 *
 * <p>Every mutation here is one of the most sensitive in the platform (account creation, status,
 * role, password) so every one requires an explicit permission, never a bare role check, and every
 * one is audited by {@link UserService} itself — this controller stays a thin translation layer.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin: users", description = "Account provisioning and lifecycle administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserService users;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;

    public AdminUserController(
            UserService users, PageableSupport pageableSupport, CurrentUserProvider currentUser) {
        this.users = users;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    @Operation(
            summary = "Search accounts",
            description = "Paginated, filterable by free-text query and status.")
    public PageResponse<UserSummary> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable) {
        var page = users.search(query, status, pageableSupport.sanitize(pageable));
        var rolesByUserId =
                users.rolesByUserIds(page.getContent().stream().map(User::getId).toList());
        return PageResponse.of(
                page, user -> toSummary(user, rolesByUserId.getOrDefault(user.getId(), Set.of())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    @Operation(summary = "Get one account")
    public UserDetail get(@PathVariable UUID id) {
        return toDetail(users.require(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(
            summary = "Create an account",
            description =
                    "Self-service registration does not exist: accounts are provisioned by an "
                            + "administrator. Omit initialPassword to create a PENDING account with no usable "
                            + "password.")
    public ResponseEntity<UserDetail> create(
            @Valid @RequestBody CreateUserRequest request, HttpServletRequest http) {
        User created = users.create(request, currentUser.require().id(), clientIp(http));
        UserDetail body = toDetail(created);
        return ResponseEntity.created(URI.create("/api/v1/admin/users/" + created.getId()))
                .body(body);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Update profile fields", description = "Null fields are left unchanged.")
    public UserDetail updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest http) {
        return toDetail(
                users.updateProfile(id, request, currentUser.require().id(), clientIp(http)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(
            summary = "Change lifecycle status",
            description =
                    "Moving away from ACTIVE immediately revokes every refresh token for the account.")
    public UserDetail changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusRequest request,
            HttpServletRequest http) {
        return toDetail(
                users.changeStatus(
                        id,
                        request.status(),
                        request.reason(),
                        currentUser.require().id(),
                        clientIp(http)));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Operation(
            summary = "Replace the role set",
            description =
                    "Replaces roles wholesale and revokes every refresh token (claims changed).")
    public UserDetail setRoles(
            @PathVariable UUID id,
            @Valid @RequestBody SetRolesRequest request,
            HttpServletRequest http) {
        return toDetail(
                users.setRoles(id, request.roles(), currentUser.require().id(), clientIp(http)));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(
            summary = "Administrative password reset",
            description =
                    "Forces a password change at next login and revokes every existing session.")
    public ResponseEntity<Void> resetPassword(
            @PathVariable UUID id,
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest http) {
        users.resetPassword(id, request.newPassword(), currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(
            summary = "Clear a brute-force lockout",
            description = "Does not touch the password.")
    public ResponseEntity<Void> unlock(@PathVariable UUID id, HttpServletRequest http) {
        users.unlock(id, currentUser.require().id(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------------- mapping

    private static UserSummary toSummary(User user, Set<String> roles) {
        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                roles,
                user.getLastLoginAt());
    }

    private UserDetail toDetail(User user) {
        StudentProfileView studentView =
                users.studentProfile(user.getId())
                        .map(AdminUserController::toStudentView)
                        .orElse(null);
        TeacherProfileView teacherView =
                users.teacherProfile(user.getId())
                        .map(AdminUserController::toTeacherView)
                        .orElse(null);
        return new UserDetail(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getPreferredLocale().code(),
                user.getStatus(),
                user.roleCodes(),
                user.permissionCodes(),
                user.isMustChangePassword(),
                user.getLastLoginAt(),
                user.getLockedUntil(),
                user.getCreatedAt(),
                studentView,
                teacherView);
    }

    private static StudentProfileView toStudentView(StudentProfile profile) {
        return new StudentProfileView(
                profile.getStudentNumber(),
                profile.getProgrammeId(),
                profile.getGroupCode(),
                profile.getEnrollmentYear(),
                profile.getStudyForm());
    }

    private static TeacherProfileView toTeacherView(TeacherProfile profile) {
        return new TeacherProfileView(
                profile.getEmployeeNumber(),
                profile.getDepartment() == null ? null : profile.getDepartment().getId(),
                profile.getDepartment() == null ? null : profile.getDepartment().getName(),
                profile.getAcademicTitle(),
                profile.getAcademicDegree(),
                profile.getOfficeHours());
    }

    // ----------------------------------------------------------------------- request context

    /**
     * Mirrors {@code AuthController.clientIp}: honoured only for audit context, never for authz.
     */
    private static String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        return http.getRemoteAddr();
    }
}
