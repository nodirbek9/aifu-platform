package uz.aifu.lms.common.error;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Assigns a request id, exposes it on the response and in the logging MDC, so a user-reported
 * {@code requestId} from an {@link ApiError} can be grepped straight out of the logs.
 *
 * <p>Runs first, before authentication, so even rejected requests are traceable.
 */
@Component
@Order(1)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_KEY = "requestId";
    public static final String HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String incoming = request.getHeader(HEADER);
        // Never trust a client-supplied id verbatim: cap it and strip anything but safe characters
        // so it cannot be used for log injection.
        String requestId =
                StringUtils.hasText(incoming)
                        ? incoming.replaceAll("[^A-Za-z0-9._-]", "")
                                .substring(0, Math.min(64, incoming.length()))
                        : UUID.randomUUID().toString().replace("-", "").substring(0, 16);
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
        }
    }
}
