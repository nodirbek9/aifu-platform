package uz.aifu.lms.common.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.aifu.lms.common.config.SecurityProperties;

/**
 * Per-IP token bucket on the authentication endpoints — the brute-force control that complements
 * per-account lockout (ADR-003). Without it, an attacker simply spreads attempts across accounts.
 *
 * <p>In-process by design: we run a single API instance (ADR-002). If the API is ever scaled
 * horizontally this must move to a shared store, because N instances would multiply the limit by N.
 * That is recorded in {@code docs/security/README.md}.
 */
@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RateLimitFilter.class);

    private final SecurityProperties properties;
    private final Cache<String, Bucket> buckets =
            Caffeine.newBuilder()
                    .maximumSize(50_000)
                    .expireAfterAccess(Duration.ofMinutes(30))
                    .build();

    public RateLimitFilter(SecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/refresh")
                || path.startsWith("/api/v1/auth/password-reset"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = clientIp(request) + ":" + request.getRequestURI();
        Bucket bucket = buckets.get(key, ignored -> newBucket());
        if (bucket != null && bucket.tryConsume(1)) {
            chain.doFilter(request, response);
            return;
        }
        LOG.warn("Rate limit exceeded for {} on {}", clientIp(request), request.getRequestURI());
        response.setStatus(429);
        response.setContentType("application/json");
        response.setHeader("Retry-After", "60");
        response.getWriter()
                .write(
                        "{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many attempts. Try again later.\"}");
    }

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(properties.authRateLimitPerMinute())
                                .refillGreedy(
                                        properties.authRateLimitPerMinute(), Duration.ofMinutes(1))
                                .build())
                .build();
    }

    private String clientIp(HttpServletRequest request) {
        // Trust X-Forwarded-For only because a reverse proxy sets it
        // (server.forward-headers-strategy).
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
