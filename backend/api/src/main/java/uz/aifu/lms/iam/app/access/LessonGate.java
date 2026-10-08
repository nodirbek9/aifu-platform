package uz.aifu.lms.iam.app.access;

import java.util.UUID;

/**
 * Port: Learning path gate (Phase 5, ADR-015).
 *
 * <p>Implemented by {@code progress.app.LessonGateAdapter} to enforce lesson sequencing and midterm
 * gates across domain modules without violating architectural boundaries.
 */
public interface LessonGate {

    void requireLessonOpen(UUID lessonId, UUID enrollmentId);

    void requireActivityOpen(UUID activityId, UUID enrollmentId);
}
