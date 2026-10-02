package uz.aifu.lms.worker.domain;

import java.math.BigDecimal;
import java.util.UUID;

/** One {@code coding_task_test_cases} row (V010). */
public record TestCaseRecord(
        UUID id,
        int position,
        String stdin,
        String expectedStdout,
        String comparisonMode,
        BigDecimal numericTolerance,
        BigDecimal weight,
        Integer cpuTimeLimitMs) {}
