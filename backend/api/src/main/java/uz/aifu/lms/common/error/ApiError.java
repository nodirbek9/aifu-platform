package uz.aifu.lms.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

/**
 * The single error envelope for every failed request. Stable by contract.
 *
 * <p>Never carries a stack trace, SQL, exception class name, or any other internal detail.
 */
@Schema(name = "ApiError", description = "Uniform error response returned by every endpoint")
public record ApiError(
        @Schema(
                        description = "Stable machine-readable code the client may branch on",
                        example = "ENROLLMENT_DUPLICATE")
                String code,
        @Schema(
                        description = "Human-readable, safe-to-display message",
                        example = "Student is already enrolled in this course")
                String message,
        @Schema(description = "Structured context; field errors appear under 'fields'")
                Map<String, Object> details,
        @Schema(
                        description = "Correlates this response with server logs",
                        example = "6f1c2f0a4b8e4d51")
                String requestId,
        @Schema(example = "2026-09-21T09:14:33Z") Instant timestamp,
        @Schema(example = "/api/v1/admin/enrollments") String path) {

    public static ApiError of(
            ErrorCode code,
            String message,
            Map<String, Object> details,
            String requestId,
            String path) {
        return new ApiError(
                code.name(),
                message,
                details == null || details.isEmpty() ? null : details,
                requestId,
                Instant.now(),
                path);
    }
}
