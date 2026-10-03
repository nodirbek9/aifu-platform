package uz.aifu.lms.execution.domain;

import java.util.UUID;

/** What the review queue is narrowed to; a null field does not narrow it. */
public record ReviewQueueFilter(
        UUID taskId, String verdict, String groupCode, boolean needsFeedback, boolean latestOnly) {}
