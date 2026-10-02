package uz.aifu.lms.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
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

    @Schema(description = "One student of an imported list.")
    public record ImportStudentRow(
            @Size(max = 200) String fullName,
            @Size(max = 32) String studentNumber,
            @Schema(example = "BK-DI26/1") @Size(max = 32) String groupCode,
            @Schema(description = "Default: the student number, lowercased") @Size(max = 64)
                    String username,
            @Schema(description = "Default: username@emailDomain") @Size(max = 320) String email,
            @Size(max = 32) String phone,
            UUID programmeId,
            Integer enrollmentYear) {}

    @Schema(
            description =
                    "Import students with their groups. The browser parses the CSV or Excel file"
                            + " and sends the rows. With dryRun=true nothing is written and the"
                            + " result says what would happen.")
    public record ImportStudentsRequest(
            @NotEmpty @Size(max = 2000) List<@NotNull @Valid ImportStudentRow> rows,
            @Schema(
                            description =
                                    "The first password of every created account; each student must"
                                            + " change it at first login. Required unless dryRun.")
                    @StrongPassword
                    String initialPassword,
            @Schema(
                            example = "student.aifu.uz",
                            description = "Builds an email for rows that have none")
                    @Size(max = 200)
                    String emailDomain,
            @Schema(allowableValues = {"uz", "ru", "en"}) String preferredLocale,
            Boolean dryRun) {}

    public record ImportStudentResult(
            @Schema(description = "1-based position in the request") int row,
            String studentNumber,
            String username,
            @Schema(
                            description =
                                    "CREATED: a new account. MOVED: an existing student put into the"
                                            + " row's group. UNCHANGED: already as the row says."
                                            + " FAILED: see message.",
                            allowableValues = {"CREATED", "MOVED", "UNCHANGED", "FAILED"})
                    String outcome,
            UUID userId,
            String message) {}

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
