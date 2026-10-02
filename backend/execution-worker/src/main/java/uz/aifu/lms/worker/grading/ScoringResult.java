package uz.aifu.lms.worker.grading;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ScoringResult(
        BigDecimal score,
        BigDecimal maxScore,
        int testsPassed,
        int testsTotal,
        String verdict,
        Map<UUID, BigDecimal> pointsPerTest) {}
