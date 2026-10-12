package uz.aifu.lms.common.error;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Assigns a request id, exposes it on the response and in the logging MDC, so a user-reported
 * {@code requestId} from an {@link ApiError} can be grepped straight out of the logs.
 *
 * <p>Runs first — ahead of the Spring Security chain, which registers itself at order -100 — so
 * even a request that security rejects carries its id in the 401 / 403 body and in the log.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_KEY = "requestId";
    public static final String HEADER = "X-Request-Id";
    private static final int MAX_LENGTH = 64;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = sanitize(request.getHeader(HEADER));
        if (requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        MDC.put(REQUEST_ID_KEY, requestId);
        response.setHeader(HEADER, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID_KEY);
            MDC.remove("userId");
            MDC.remove("userLabel");
        }
    }

    /**
     * Never trust a client-supplied id verbatim: strip anything but safe characters, so it cannot
     * be used for log injection, then cap what is left. Empty when nothing usable was sent.
     */
    static String sanitize(String incoming) {
        if (!StringUtils.hasText(incoming)) {
            return "";
        }
        String safe = incoming.replaceAll("[^A-Za-z0-9._-]", "");
        return safe.substring(0, Math.min(MAX_LENGTH, safe.length()));
    }
}
