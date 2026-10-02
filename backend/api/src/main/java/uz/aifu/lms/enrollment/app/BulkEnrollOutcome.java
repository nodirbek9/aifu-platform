package uz.aifu.lms.enrollment.app;

import java.util.UUID;

/**
 * Per-student result of {@link EnrollmentService#bulkEnroll} — one bad row must not void the rest
 * of the batch, so each student's outcome is reported independently.
 */
public record BulkEnrollOutcome(
        UUID studentUserId,
        boolean success,
        UUID enrollmentId,
        String errorCode,
        String errorMessage) {

    public static BulkEnrollOutcome success(UUID studentUserId, UUID enrollmentId) {
        return new BulkEnrollOutcome(studentUserId, true, enrollmentId, null, null);
    }

    public static BulkEnrollOutcome failure(
            UUID studentUserId, String errorCode, String errorMessage) {
        return new BulkEnrollOutcome(studentUserId, false, null, errorCode, errorMessage);
    }
}
