package uz.aifu.lms.common.security;

import java.util.Optional;

/**
 * Port for turning a raw bearer token into an {@link AuthenticatedUser}.
 *
 * <p>{@code common} must depend on no module (CLAUDE.md §5), but request authentication is
 * unavoidably a cross-cutting servlet-filter concern that needs to invoke {@code iam}'s token
 * logic. This interface is the seam: {@link JwtAuthenticationFilter} depends only on this port, and
 * {@code iam.app.auth.TokenService} implements it — the dependency arrow stays {@code iam ->
 * common}, never the reverse.
 */
public interface AccessTokenAuthenticator {

    Optional<AuthenticatedUser> parseAccessToken(String token);
}
