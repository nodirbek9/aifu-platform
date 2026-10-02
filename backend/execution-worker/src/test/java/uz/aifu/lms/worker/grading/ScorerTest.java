package uz.aifu.lms.worker.grading;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.worker.domain.TestCaseRecord;

class ScorerTest {

    private static TestCaseRecord testCase(UUID id, BigDecimal weight) {
        return new TestCaseRecord(id, 0, "", "", "TRIMMED", null, weight, null);
    }

    private static TestOutcome outcome(UUID testCaseId, boolean passed) {
        return new TestOutcome(
                testCaseId, passed, passed ? "ACCEPTED" : "WRONG_ANSWER", 10, null, 0, "", "");
    }

    @Test
    void allOrNothingAwardsFullPointsOnlyWhenEveryTestPasses() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        List<TestCaseRecord> testCases =
                List.of(testCase(a, BigDecimal.ONE), testCase(b, BigDecimal.ONE));

        ScoringResult allPassed =
                Scorer.score(
                        List.of(outcome(a, true), outcome(b, true)),
                        testCases,
                        "ALL_OR_NOTHING",
                        new BigDecimal("100"));
        assertThat(allPassed.score()).isEqualByComparingTo("100");
        assertThat(allPassed.verdict()).isEqualTo("ACCEPTED");

        ScoringResult onePassed =
                Scorer.score(
                        List.of(outcome(a, true), outcome(b, false)),
                        testCases,
                        "ALL_OR_NOTHING",
                        new BigDecimal("100"));
        assertThat(onePassed.score()).isEqualByComparingTo("0");
        assertThat(onePassed.verdict()).isEqualTo("PARTIAL");
    }

    @Test
    void perTestEqualSplitsPointsEvenlyAcrossTestCases() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        List<TestCaseRecord> testCases =
                List.of(
                        testCase(a, BigDecimal.ONE),
                        testCase(b, BigDecimal.ONE),
                        testCase(c, BigDecimal.ONE));

        ScoringResult result =
                Scorer.score(
                        List.of(outcome(a, true), outcome(b, true), outcome(c, false)),
                        testCases,
                        "PER_TEST_EQUAL",
                        new BigDecimal("90"));
        assertThat(result.score()).isEqualByComparingTo("60.00");
        assertThat(result.testsPassed()).isEqualTo(2);
        assertThat(result.testsTotal()).isEqualTo(3);
        assertThat(result.verdict()).isEqualTo("PARTIAL");
    }

    @Test
    void perTestWeightedDistributesProportionallyToWeight() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        List<TestCaseRecord> testCases =
                List.of(testCase(a, new BigDecimal("1")), testCase(b, new BigDecimal("3")));

        ScoringResult result =
                Scorer.score(
                        List.of(outcome(a, true), outcome(b, false)),
                        testCases,
                        "PER_TEST_WEIGHTED",
                        new BigDecimal("100"));
        assertThat(result.score()).isEqualByComparingTo("25.00");
        assertThat(result.pointsPerTest().get(a)).isEqualByComparingTo("25.00");
        assertThat(result.pointsPerTest().get(b)).isEqualByComparingTo("0.00");
    }

    @Test
    void noPassesWithNoInfrastructureFailureIsWrongAnswer() {
        UUID a = UUID.randomUUID();
        List<TestCaseRecord> testCases = List.of(testCase(a, BigDecimal.ONE));
        ScoringResult result =
                Scorer.score(
                        List.of(outcome(a, false)),
                        testCases,
                        "PER_TEST_EQUAL",
                        new BigDecimal("100"));
        assertThat(result.verdict()).isEqualTo("WRONG_ANSWER");
    }

    @Test
    void timeLimitExceededOutranksWrongAnswerWhenNothingPassed() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        List<TestCaseRecord> testCases =
                List.of(testCase(a, BigDecimal.ONE), testCase(b, BigDecimal.ONE));
        TestOutcome wrong = outcome(a, false);
        TestOutcome timedOut =
                new TestOutcome(b, false, "TIME_LIMIT_EXCEEDED", 5000, null, null, "", "");
        ScoringResult result =
                Scorer.score(
                        List.of(wrong, timedOut),
                        testCases,
                        "PER_TEST_EQUAL",
                        new BigDecimal("100"));
        assertThat(result.verdict()).isEqualTo("TIME_LIMIT_EXCEEDED");
    }

    @Test
    void internalErrorAlwaysWinsRegardlessOfOtherOutcomes() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        List<TestCaseRecord> testCases =
                List.of(testCase(a, BigDecimal.ONE), testCase(b, BigDecimal.ONE));
        TestOutcome passed = outcome(a, true);
        TestOutcome broken = new TestOutcome(b, false, "INTERNAL_ERROR", 0, null, null, "", "");
        ScoringResult result =
                Scorer.score(
                        List.of(passed, broken),
                        testCases,
                        "PER_TEST_EQUAL",
                        new BigDecimal("100"));
        assertThat(result.verdict()).isEqualTo("INTERNAL_ERROR");
    }
}
