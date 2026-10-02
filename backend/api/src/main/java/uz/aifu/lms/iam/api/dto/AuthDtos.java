package uz.aifu.lms.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import uz.aifu.lms.common.validation.StrongPassword;

/**
 * Authentication request/response contracts.
 *
 * <p>Grouped in one file on purpose: these five records are read together by anyone integrating the
 * login flow, and splitting them across five files costs an AI agent five file reads for no
 * benefit.
 */
public final class AuthDtos {

    private AuthDtos() {}

    @Schema(description = "Login with username or email plus password")
    public record LoginRequest(
            @Schema(example = "student01", description = "Username or email address")
                    @NotBlank
                    @Size(max = 320)
                    String identifier,
            @Schema(example = "Dev!Pass123") @NotBlank @Size(max = 200) String password) {}

    @Schema(
            description =
                    "Exchange a refresh token for a new token pair. The old token is consumed.")
    public record RefreshRequest(@NotBlank @Size(max = 200) String refreshToken) {}

    @Schema(
            description =
                    "Revoke one refresh token. Idempotent: an unknown token still returns 204.")
    public record LogoutRequest(@Size(max = 200) String refreshToken) {}

    @Schema(description = "Issued token pair")
    public record TokenResponse(
            @Schema(description = "Bearer access token, short-lived") String accessToken,
            @Schema(description = "Opaque single-use refresh token") String refreshToken,
            @Schema(example = "Bearer") String tokenType,
            @Schema(example = "900") long expiresInSeconds,
            Instant expiresAt,
            @Schema(
                            description =
                                    "When true the client must call PUT /auth/password before proceeding")
                    boolean mustChangePassword) {

        public static TokenResponse of(
                String accessToken,
                String refreshToken,
                long expiresInSeconds,
                Instant expiresAt,
                boolean mustChangePassword) {
            return new TokenResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    expiresInSeconds,
                    expiresAt,
                    mustChangePassword);
        }
    }

    @Schema(description = "Change own password. Revokes every refresh token for the account.")
    public record ChangePasswordRequest(
            @NotBlank @Size(max = 200) String currentPassword,
            @NotBlank @StrongPassword String newPassword) {}

    @Schema(description = "The authenticated caller, for bootstrapping a dashboard")
    public record CurrentUserResponse(
            UUID id,
            String username,
            String email,
            String fullName,
            String preferredLocale,
            Set<String> roles,
            Set<String> permissions,
            boolean mustChangePassword,
            Instant lastLoginAt,
            String phone,
            @Schema(description = "Set for a student") String studentNumber,
            @Schema(description = "The student's group, e.g. BK-DI26/1; null when not in a group")
                    String groupCode) {}

    @Schema(description = "What a user may change about their own account.")
    public record UpdateMyProfileRequest(
            @Schema(allowableValues = {"uz", "ru", "en"}) String preferredLocale,
            @Size(max = 32) String phone) {}
}
