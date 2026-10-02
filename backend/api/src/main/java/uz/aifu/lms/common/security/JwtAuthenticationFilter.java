package uz.aifu.lms.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Turns a valid {@code Authorization: Bearer} access token into an authenticated {@link
 * AuthenticatedUser} with permission authorities.
 *
 * <p>Deliberately silent on failure: it does NOT reject the request, it simply leaves the context
 * unauthenticated and lets Spring Security's entry point produce the 401. That keeps error
 * rendering in one place and avoids leaking why a token was refused.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final AccessTokenAuthenticator tokenAuthenticator;

    public JwtAuthenticationFilter(AccessTokenAuthenticator tokenAuthenticator) {
        this.tokenAuthenticator = tokenAuthenticator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length()).trim();
            tokenAuthenticator
                    .parseAccessToken(token)
                    .ifPresent(
                            user -> {
                                var authorities =
                                        java.util.stream.Stream.concat(
                                                        user.permissions().stream()
                                                                .map(SimpleGrantedAuthority::new),
                                                        user.roles().stream()
                                                                .map(
                                                                        role ->
                                                                                new SimpleGrantedAuthority(
                                                                                        "ROLE_"
                                                                                                + role)))
                                                .collect(Collectors.toSet());
                                var authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                user, null, authorities);
                                SecurityContextHolder.getContext()
                                        .setAuthentication(authentication);
                                // Correlates log lines with the acting user. Never log the token
                                // itself.
                                MDC.put("userId", user.id().toString());
                            });
        }
        chain.doFilter(request, response);
    }
}
