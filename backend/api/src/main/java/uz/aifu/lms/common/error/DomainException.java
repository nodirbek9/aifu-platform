package uz.aifu.lms.common.error;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for every expected failure. Carries an {@link ErrorCode} so the HTTP status and the
 * client-visible code are decided once, at the throw site.
 *
 * <p>Never throw a bare {@code RuntimeException} or {@code IllegalStateException} for a business
 * rule: it would surface as a 500 and leak nothing useful to the caller.
 */
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details = new HashMap<>();

    public DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public DomainException with(String key, Object value) {
        details.put(key, value);
        return this;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public Map<String, Object> details() {
        return Map.copyOf(details);
    }
}
