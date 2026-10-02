package uz.aifu.lms.common.error;

import org.springframework.http.HttpStatus;

/**
 * The closed set of machine-readable error codes. This is part of the frontend contract: the
 * OpenAPI file lists them as the values of {@code ApiError.code}, and {@code docs/03-API-GUIDE.md}
 * §2 says what a client does with each; a client may branch on these values.
 *
 * <p>Adding a code is additive and safe. Renaming or removing one is a BREAKING change.
 */
public enum ErrorCode {

    // --- validation / request shape ---
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST),
    UNSUPPORTED_LOCALE(HttpStatus.BAD_REQUEST),
    PAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST),

    // --- authentication ---
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
    AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED),
    AUTH_REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_NOT_ACTIVE(HttpStatus.UNAUTHORIZED),
    AUTH_PASSWORD_CHANGE_REQUIRED(HttpStatus.FORBIDDEN),

    // --- authorization ---
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN),
    NOT_ENROLLED(HttpStatus.FORBIDDEN),
    NOT_COURSE_TEACHER(HttpStatus.FORBIDDEN),

    // --- resources ---
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),

    // --- conflicts ---
    RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT),
    IDEMPOTENCY_KEY_REUSED(HttpStatus.CONFLICT),
    ENROLLMENT_DUPLICATE(HttpStatus.CONFLICT),
    ENROLLMENT_CLOSED(HttpStatus.CONFLICT),
    ENROLLMENT_INVALID_TRANSITION(HttpStatus.CONFLICT),
    ATTEMPT_ALREADY_OPEN(HttpStatus.CONFLICT),
    ATTEMPT_ALREADY_SUBMITTED(HttpStatus.CONFLICT),
    TIMETABLE_CONFLICT(HttpStatus.CONFLICT),

    // --- domain rules (semantically valid request, refused by a business rule) ---
    ATTEMPT_LIMIT_REACHED(HttpStatus.UNPROCESSABLE_ENTITY),
    ATTEMPT_EXPIRED(HttpStatus.UNPROCESSABLE_ENTITY),
    ACTIVITY_NOT_AVAILABLE(HttpStatus.UNPROCESSABLE_ENTITY),
    LESSON_PREREQUISITE_NOT_MET(HttpStatus.UNPROCESSABLE_ENTITY),
    CONTENT_NOT_PUBLISHED(HttpStatus.UNPROCESSABLE_ENTITY),
    PUBLICATION_VALIDATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY),
    TRANSLATION_INCOMPLETE(HttpStatus.UNPROCESSABLE_ENTITY),
    ASSESSMENT_WEIGHTS_INVALID(HttpStatus.UNPROCESSABLE_ENTITY),
    GRADE_IMMUTABLE(HttpStatus.UNPROCESSABLE_ENTITY),
    LANGUAGE_NOT_ENABLED(HttpStatus.UNPROCESSABLE_ENTITY),
    SOURCE_TOO_LARGE(HttpStatus.UNPROCESSABLE_ENTITY),
    UNSAFE_CONTENT_REJECTED(HttpStatus.UNPROCESSABLE_ENTITY),
    FILE_TYPE_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_STATE_TRANSITION(HttpStatus.UNPROCESSABLE_ENTITY),

    // --- throttling ---
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS),
    TOO_MANY_CONCURRENT_SUBMISSIONS(HttpStatus.TOO_MANY_REQUESTS),

    // --- server ---
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    EXECUTION_ENGINE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    STORAGE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
