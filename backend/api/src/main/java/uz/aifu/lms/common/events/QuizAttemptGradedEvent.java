package uz.aifu.lms.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Published by {@code assessment.app.AttemptService} after deterministic grading completes,
 * consumed by {@code progress.app.ProgressService} to update {@code activity_progress}.
 *
 * <p>A plain event record in {@code common}, not a cross-module method call: {@code assessment} may
 * not depend on {@code progress} (module map — the dependency runs the other way, {@code progress
 * -> assessment}), so the only direction assessment can safely notify progress is via Spring's
 * event bus, with the payload type living somewhere both modules already may depend on. Progress
 * listens with {@code @TransactionalEventListener(phase = AFTER_COMMIT)} so it never acts on a
 * grade that the attempt's own transaction later rolls back.
 */
public record QuizAttemptGradedEvent(
        UUID attemptId,
        UUID quizId,
        UUID enrollmentId,
        UUID activityId,
        BigDecimal score,
        BigDecimal maxScore,
        BigDecimal percent,
        boolean passed,
        Instant gradedAt) {}
