package uz.aifu.lms.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import uz.aifu.lms.common.error.RequestIdFilter;

/** Produces the ApiError envelope for authenticated-but-forbidden requests. */
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter()
                .write(
                        """
                        {"code":"AUTH_FORBIDDEN","message":"You do not have permission to perform this action",\
                        "requestId":"%s","path":"%s"}"""
                                .formatted(
                                        String.valueOf(MDC.get(RequestIdFilter.REQUEST_ID_KEY)),
                                        request.getRequestURI()));
    }
}
