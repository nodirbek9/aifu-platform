package uz.aifu.lms.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * The ONE place HTTP error responses are produced. Do not add a second
 * {@code @RestControllerAdvice}.
 *
 * <p>Rules: every response is an {@link ApiError}; nothing internal is ever leaked (no stack
 * traces, SQL fragments, class names or constraint text); unexpected failures are logged at ERROR
 * with the request id, and the client only receives {@code INTERNAL_ERROR}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(DomainException ex, HttpServletRequest request) {
        ErrorCode code = ex.errorCode();
        if (code.status().is5xxServerError()) {
            LOG.error("Domain failure {}: {}", code, ex.getMessage(), ex);
        } else {
            LOG.debug("Domain rule {}: {}", code, ex.getMessage());
        }
        return respond(code, ex.getMessage(), ex.details(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleBeanValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> fields = new LinkedHashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ex.getBindingResult()
                .getGlobalErrors()
                .forEach(
                        error ->
                                fields.putIfAbsent(
                                        error.getObjectName(), error.getDefaultMessage()));
        return respond(
                ErrorCode.VALIDATION_FAILED,
                "Request validation failed",
                Map.of("fields", fields),
                request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, Object> fields = new LinkedHashMap<>();
        ex.getConstraintViolations()
                .forEach(v -> fields.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return respond(
                ErrorCode.VALIDATION_FAILED,
                "Request validation failed",
                Map.of("fields", fields),
                request);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> handleMalformed(Exception ex, HttpServletRequest request) {
        // Deliberately generic: the parser message can echo request internals.
        LOG.debug("Malformed request: {}", ex.getMessage());
        return respond(
                ErrorCode.MALFORMED_REQUEST, "Request could not be parsed", Map.of(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        return respond(ErrorCode.AUTH_TOKEN_INVALID, "Authentication required", Map.of(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        LOG.info("Access denied for {} {}", request.getMethod(), request.getRequestURI());
        return respond(
                ErrorCode.AUTH_FORBIDDEN,
                "You do not have permission to perform this action",
                Map.of(),
                request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(
            OptimisticLockingFailureException ex, HttpServletRequest request) {
        return respond(
                ErrorCode.CONCURRENT_MODIFICATION,
                "The resource was modified concurrently. Reload and retry.",
                Map.of(),
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        // A DB constraint fired. Log the detail; tell the client only that there was a conflict.
        // Constraint names can disclose schema internals, so they never reach the response.
        LOG.warn(
                "Integrity violation on {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return respond(
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                "The operation conflicts with existing data",
                Map.of(),
                request);
    }

    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<ApiError> handleNoHandler(Exception ex, HttpServletRequest request) {
        return respond(ErrorCode.RESOURCE_NOT_FOUND, "Endpoint not found", Map.of(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethod(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return respond(ErrorCode.MALFORMED_REQUEST, "HTTP method not supported", Map.of(), request);
    }

    /** Last resort. The client learns nothing beyond the request id. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        LOG.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return respond(
                ErrorCode.INTERNAL_ERROR,
                "An unexpected error occurred. Quote the requestId when reporting it.",
                Map.of(),
                request);
    }

    private ResponseEntity<ApiError> respond(
            ErrorCode code,
            String message,
            Map<String, Object> details,
            HttpServletRequest request) {
        HttpStatus status = code.status();
        ApiError body =
                ApiError.of(
                        code,
                        message,
                        details,
                        MDC.get(RequestIdFilter.REQUEST_ID_KEY),
                        request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
