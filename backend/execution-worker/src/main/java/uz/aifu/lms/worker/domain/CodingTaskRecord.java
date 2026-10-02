package uz.aifu.lms.worker.domain;

import java.math.BigDecimal;
import java.util.UUID;

/** The subset of one {@code coding_tasks} row the worker needs to run and score a submission. */
public record CodingTaskRecord(
        UUID id,
        String scoringStrategy,
        BigDecimal maxPoints,
        Integer cpuTimeLimitMs,
        Integer wallTimeLimitMs,
        Integer memoryLimitKb) {}
