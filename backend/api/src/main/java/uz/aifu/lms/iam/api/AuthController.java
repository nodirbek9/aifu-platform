package uz.aifu.lms.iam.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.security.AuthenticatedUser;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.iam.api.dto.AuthDtos.ChangePasswordRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.LogoutRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.RefreshRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.app.auth.AuthenticationService;
import uz.aifu.lms.iam.app.auth.AuthenticationService.AuthTokens;

/**
 * Authentication endpoints (brief §8).
 *
 * <p>These are the only endpoints reachable without a token, and the only ones rate-limited by IP
 * rather than by principal. Every failure mode returns the same {@code AUTH_INVALID_CREDENTIALS}
 * body: the API must not tell an attacker whether a username exists.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login, token refresh, logout, password change")
public class AuthController {

    private final AuthenticationService authentication;
    private final CurrentUserProvider currentUser;

    public AuthController(AuthenticationService authentication, CurrentUserProvider currentUser) {
        this.authentication = authentication;
        this.currentUser = currentUser;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Log in",
            description =
                    """
                    Exchanges credentials for an access token and a refresh token.

                    The access token is a short-lived JWT to be sent as `Authorization: Bearer <token>`.
                    The refresh token is an opaque, single-use value: calling `/auth/refresh` consumes it
                    and returns a new pair. Presenting an already-consumed refresh token revokes the whole
                    token family, on the assumption that it leaked.

                    Failures always return `AUTH_INVALID_CREDENTIALS` with HTTP 401, whether the account
                    is unknown, the password is wrong, or the account is locked. Check the audit log for
                    the real reason.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authenticated"),
        @ApiResponse(responseCode = "401", description = "AUTH_INVALID_CREDENTIALS"),
        @ApiResponse(responseCode = "403", description = "AUTH_ACCOUNT_NOT_ACTIVE"),
        @ApiResponse(responseCode = "429", description = "RATE_LIMIT_EXCEEDED")
    })
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        AuthTokens tokens =
                authentication.login(
                        request.identifier(),
                        request.password(),
                        http.getHeader(HttpHeaders.USER_AGENT),
                        clientIp(http));
        return toResponse(tokens);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh the token pair",
            description =
                    "Consumes the supplied refresh token and issues a new pair. Re-presenting a consumed "
                            + "token revokes every token in its family and returns "
                            + "`AUTH_REFRESH_TOKEN_REUSED`.")
    public TokenResponse refresh(
            @Valid @RequestBody RefreshRequest request, HttpServletRequest http) {
        AuthTokens tokens =
                authentication.refresh(
                        request.refreshToken(),
                        http.getHeader(HttpHeaders.USER_AGENT),
                        clientIp(http));
        return toResponse(tokens);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    @Operation(
            summary = "Log out of this session",
            description =
                    "Revokes the supplied refresh token. Idempotent by design: an unknown or already "
                            + "revoked token still returns 204, so a client can always safely clear state.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody(required = false) LogoutRequest request, HttpServletRequest http) {
        AuthenticatedUser caller = currentUser.require();
        String token = request == null ? null : request.refreshToken();
        authentication.logout(
                token, caller.id(), clientIp(http), http.getHeader(HttpHeaders.USER_AGENT));
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout-all")
    @Operation(
            summary = "Log out everywhere",
            description =
                    "Revokes every refresh token for the caller. Use after a suspected compromise.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logoutAll(HttpServletRequest http) {
        AuthenticatedUser caller = currentUser.require();
        authentication.logoutAll(
                caller.id(), clientIp(http), http.getHeader(HttpHeaders.USER_AGENT));
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/password")
    @Operation(
            summary = "Change own password",
            description =
                    "Requires the current password. On success every refresh token for the account is "
                            + "revoked, so all other sessions are ended and the client must log in again.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request, HttpServletRequest http) {
        AuthenticatedUser caller = currentUser.require();
        authentication.changePassword(
                caller.id(), request.currentPassword(), request.newPassword(), clientIp(http));
        return ResponseEntity.noContent().build();
    }

    private static TokenResponse toResponse(AuthTokens tokens) {
        return TokenResponse.of(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresInSeconds(),
                tokens.expiresAt(),
                tokens.mustChangePassword());
    }

    /**
     * Best-effort client address.
     *
     * <p>{@code X-Forwarded-For} is honoured because the documented deployment puts the API behind
     * a reverse proxy, but it is attacker-controlled when that proxy is absent. It is therefore
     * used only for audit context and coarse rate limiting, never for an authorization decision.
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
