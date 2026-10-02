package uz.aifu.lms.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import uz.aifu.lms.common.validation.StrongPassword;
import uz.aifu.lms.iam.domain.UserStatus;

/** Administrative user-management contracts (brief §22). */
public final class UserDtos {

    private UserDtos() {}

    @Schema(description = "Create an account. Admin only.")
    public record CreateUserRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @Schema(
                            example = "student01",
                            description = "Lowercase letters, digits, dot, dash, underscore")
                    @NotBlank
                    @Size(min = 3, max = 64)
                    @Pattern(regexp = "^[a-z0-9._-]+$")
                    String username,
            @NotBlank @Size(max = 200) String fullName,
            @Schema(
                            description =
                                    "Initial password. Omit to have the account created in PENDING "
                                            + "with no usable password.")
                    @StrongPassword
                    String initialPassword,
            @Schema(
                            example = "uz",
                            allowableValues = {"uz", "ru", "en"})
                    String preferredLocale,
            @Size(max = 32) String phone,
            @Schema(example = "[\"STUDENT\"]") @NotEmpty Set<String> roles,
            @Schema(
                            description =
                                    "When true the user must change the password at first login. Strongly "
                                            + "recommended whenever an administrator sets the initial password.")
                    Boolean mustChangePassword,
            @Schema(description = "Required when roles include STUDENT")
                    StudentProfilePayload studentProfile,
            @Schema(description = "Optional when roles include TEACHER")
                    TeacherProfilePayload teacherProfile) {}

    public record StudentProfilePayload(
            @NotBlank @Size(max = 32) String studentNumber,
            UUID programmeId,
            @Size(max = 32) String groupCode,
            Integer enrollmentYear) {}

    public record TeacherProfilePayload(
            @Size(max = 32) String employeeNumber,
            UUID departmentId,
            @Size(max = 100) String academicTitle,
            @Size(max = 100) String academicDegree,
            @Size(max = 200) String officeHours) {}

    @Schema(description = "Partial profile update. Null fields are left unchanged.")
    public record UpdateUserRequest(
            @Size(max = 200) String fullName,
            @Size(max = 32) String phone,
            @Schema(allowableValues = {"uz", "ru", "en"}) String preferredLocale) {}

    @Schema(description = "Change an account's lifecycle status. Admin only.")
    public record ChangeStatusRequest(
            @Schema(description = "Target status") UserStatus status,
            @Size(max = 500) String reason) {}

    @Schema(description = "Replace an account's role set. Admin only.")
    public record SetRolesRequest(@NotEmpty Set<String> roles) {}

    @Schema(description = "Administrative password reset. Forces a change at next login.")
    public record ResetPasswordRequest(@NotBlank @StrongPassword String newPassword) {}

    @Schema(description = "User summary for lists")
    public record UserSummary(
            UUID id,
            String username,
            String email,
            String fullName,
            UserStatus status,
            Set<String> roles,
            Instant lastLoginAt) {}

    @Schema(description = "Full user detail")
    public record UserDetail(
            UUID id,
            String username,
            String email,
            String fullName,
            String phone,
            String preferredLocale,
            UserStatus status,
            Set<String> roles,
            Set<String> permissions,
            boolean mustChangePassword,
            Instant lastLoginAt,
            Instant lockedUntil,
            Instant createdAt,
            StudentProfileView studentProfile,
            TeacherProfileView teacherProfile) {}

    public record StudentProfileView(
            String studentNumber,
            UUID programmeId,
            String groupCode,
            Integer enrollmentYear,
            String studyForm) {}

    public record TeacherProfileView(
            String employeeNumber,
            UUID departmentId,
            String departmentName,
            String academicTitle,
            String academicDegree,
            String officeHours) {}
}
