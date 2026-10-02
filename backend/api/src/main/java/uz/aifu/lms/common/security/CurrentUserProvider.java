package uz.aifu.lms.common.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;

/**
 * The single accessor for "who is calling". Services take this, never {@code HttpServletRequest}
 * and never a caller-supplied user id — trusting a user id from the request body is how horizontal
 * privilege escalation happens.
 */
@Component
public class CurrentUserProvider {

    public Optional<AuthenticatedUser> find() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public AuthenticatedUser require() {
        return find().orElseThrow(
                        () ->
                                Exceptions.rule(
                                        ErrorCode.AUTH_TOKEN_MISSING,
                                        "Authentication is required"));
    }
}
