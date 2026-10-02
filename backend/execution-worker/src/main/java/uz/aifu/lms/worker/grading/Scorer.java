package uz.aifu.lms.worker.grading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uz.aifu.lms.worker.domain.TestCaseRecord;

/**
 * Applies one {@code coding_tasks.scoring_strategy} (V010) to a set of {@link TestOutcome}s. The
 * submission-level {@code verdict} is resolved from the same outcomes: infrastructure/limit
 * failures (timeout, memory, output-limit) outrank a plain wrong-answer, since a task that never
 * ran to completion was never "just wrong" (mirrors {@link Grader}'s own priority order).
 */
public final class Scorer {

    private static final List<String> FAILURE_PRIORITY =
            List.of(
                    "TIME_LIMIT_EXCEEDED",
                    "MEMORY_LIMIT_EXCEEDED",
                    "OUTPUT_LIMIT_EXCEEDED",
                    "RUNTIME_ERROR");

    private Scorer() {}

    public static ScoringResult score(
            List<TestOutcome> outcomes,
            List<TestCaseRecord> testCases,
            String strategy,
            BigDecimal maxPoints) {
        int total = outcomes.size();
        int passed = (int) outcomes.stream().filter(TestOutcome::passed).count();

        Map<java.util.UUID, BigDecimal> pointsPerTest = new HashMap<>();
        BigDecimal score =
                switch (strategy) {
                    case "ALL_OR_NOTHING" -> {
                        boolean allPassed = total > 0 && passed == total;
                        yield allPassed ? maxPoints : BigDecimal.ZERO;
                    }
                    case "PER_TEST_EQUAL" -> {
                        BigDecimal perTest =
                                total == 0
                                        ? BigDecimal.ZERO
                                        : maxPoints.divide(
                                                BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP);
                        BigDecimal sum = BigDecimal.ZERO;
                        for (TestOutcome o : outcomes) {
                            BigDecimal awarded = o.passed() ? perTest : BigDecimal.ZERO;
                            pointsPerTest.put(
                                    o.testCaseId(), awarded.setScale(2, RoundingMode.HALF_UP));
                            sum = sum.add(awarded);
                        }
                        yield sum.setScale(2, RoundingMode.HALF_UP);
                    }
                    case "PER_TEST_WEIGHTED" -> {
                        BigDecimal totalWeight =
                                testCases.stream()
                                        .map(TestCaseRecord::weight)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        Map<java.util.UUID, BigDecimal> weightById = new HashMap<>();
                        for (TestCaseRecord tc : testCases) {
                            weightById.put(tc.id(), tc.weight());
                        }
                        BigDecimal sum = BigDecimal.ZERO;
                        for (TestOutcome o : outcomes) {
                            BigDecimal weight =
                                    weightById.getOrDefault(o.testCaseId(), BigDecimal.ZERO);
                            BigDecimal share =
                                    totalWeight.signum() == 0
                                            ? BigDecimal.ZERO
                                            : maxPoints
                                                    .multiply(weight)
                                                    .divide(totalWeight, 6, RoundingMode.HALF_UP);
                            BigDecimal awarded = o.passed() ? share : BigDecimal.ZERO;
                            pointsPerTest.put(
                                    o.testCaseId(), awarded.setScale(2, RoundingMode.HALF_UP));
                            sum = sum.add(awarded);
                        }
                        yield sum.setScale(2, RoundingMode.HALF_UP);
                    }
                    default ->
                            throw new IllegalArgumentException(
                                    "Unknown scoring strategy: " + strategy);
                };

        return new ScoringResult(
                score,
                maxPoints.setScale(2, RoundingMode.HALF_UP),
                passed,
                total,
                resolveVerdict(outcomes, passed, total),
                pointsPerTest);
    }

    private static String resolveVerdict(List<TestOutcome> outcomes, int passed, int total) {
        if (outcomes.stream().anyMatch(o -> "INTERNAL_ERROR".equals(o.verdict()))) {
            return "INTERNAL_ERROR";
        }
        if (total > 0 && passed == total) {
            return "ACCEPTED";
        }
        if (passed > 0) {
            return "PARTIAL";
        }
        for (String failure : FAILURE_PRIORITY) {
            if (outcomes.stream().anyMatch(o -> failure.equals(o.verdict()))) {
                return failure;
            }
        }
        return "WRONG_ANSWER";
    }
}
