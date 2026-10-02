package uz.aifu.lms.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import uz.aifu.lms.common.error.RequestIdFilter;

/** Produces the ApiError envelope for unauthenticated requests instead of Spring's HTML page. */
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter()
                .write(
                        """
                        {"code":"AUTH_TOKEN_MISSING","message":"Authentication is required",\
                        "requestId":"%s","path":"%s"}"""
                                .formatted(
                                        String.valueOf(MDC.get(RequestIdFilter.REQUEST_ID_KEY)),
                                        request.getRequestURI()));
    }
}
