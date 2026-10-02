package uz.aifu.lms.enrollment.app;

import java.util.UUID;

/**
 * Per-row result of {@link EnrollmentService#importByStudentNumbers} — same shape as {@link
 * BulkEnrollOutcome}, except the identifier is the raw CSV cell (a student number) rather than an
 * already-known {@code UUID}, since resolution itself can fail (unknown student number).
 */
public record CsvEnrollOutcome(
        String identifier,
        boolean success,
        UUID studentUserId,
        UUID enrollmentId,
        String errorCode,
        String errorMessage) {

    public static CsvEnrollOutcome success(
            String identifier, UUID studentUserId, UUID enrollmentId) {
        return new CsvEnrollOutcome(identifier, true, studentUserId, enrollmentId, null, null);
    }

    public static CsvEnrollOutcome failure(
            String identifier, UUID studentUserId, String errorCode, String errorMessage) {
        return new CsvEnrollOutcome(
                identifier, false, studentUserId, null, errorCode, errorMessage);
    }
}
