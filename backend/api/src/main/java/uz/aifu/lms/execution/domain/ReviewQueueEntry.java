package uz.aifu.lms.execution.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** One submission as the review queue of a course lists it (read model, no entity). */
public record ReviewQueueEntry(
        UUID submissionId,
        UUID codingTaskId,
        String taskTitle,
        UUID studentUserId,
        String studentFullName,
        String groupCode,
        int attemptNumber,
        String languageCode,
        String status,
        String verdict,
        BigDecimal score,
        BigDecimal maxScore,
        Instant submittedAt,
        boolean hasFeedback) {}
