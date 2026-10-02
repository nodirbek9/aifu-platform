package uz.aifu.lms.grading.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.academics.domain.AssessmentComponentKind;

/**
 * The `IAKTT1105` rubric (JN 30 = 10+10+10, ON 30, YaN 40, 60% gates), verbatim from
 * syllabus-analysis.md §2 finding F4 — and the seeded `AIFU_100_5` band boundaries (V005).
 */
class FinalScoreCalculatorTest {

    private final FinalScoreCalculator calculator = new FinalScoreCalculator();

    private static final UUID JN = UUID.randomUUID();
    private static final UUID JN_ATTENDANCE = UUID.randomUUID();
    private static final UUID JN_PRACTICAL = UUID.randomUUID();
    private static final UUID JN_INDEPENDENT = UUID.randomUUID();
    private static final UUID ON = UUID.randomUUID();
    private static final UUID YAN = UUID.randomUUID();

    private static final List<ComponentSpec> IAKTT1105_COMPONENTS =
            List.of(
                    new ComponentSpec(
                            JN, null, AssessmentComponentKind.CONTINUOUS, bd(30), bd(60), true),
                    new ComponentSpec(
                            JN_ATTENDANCE,
                            JN,
                            AssessmentComponentKind.ATTENDANCE,
                            bd(10),
                            null,
                            false),
                    new ComponentSpec(
                            JN_PRACTICAL,
                            JN,
                            AssessmentComponentKind.PRACTICAL_ACTIVITY,
                            bd(10),
                            null,
                            false),
                    new ComponentSpec(
                            JN_INDEPENDENT,
                            JN,
                            AssessmentComponentKind.INDEPENDENT_WORK,
                            bd(10),
                            null,
                            false),
                    new ComponentSpec(
                            ON, null, AssessmentComponentKind.MIDTERM, bd(30), bd(60), true),
                    new ComponentSpec(
                            YAN, null, AssessmentComponentKind.FINAL, bd(40), bd(60), false));

    private static final List<GradeBand> AIFU_100_5_BANDS =
            List.of(
                    new GradeBand(bd(90), bd(100), "5", true),
                    new GradeBand(bd(80), bd("89.99"), "4", true),
                    new GradeBand(bd(60), bd("79.99"), "3", true),
                    new GradeBand(bd(0), bd("59.99"), "2", false));

    @Test
    void severalWorksUnderOneComponent_weighByTheirOwnMaximums_neverReplaceEachOther() {
        // three practical tasks marked out of 100, in a component worth 10 points:
        // (100 + 50 + 90) / 300 of 10 = 8 — not 9, the last one, and not 240
        List<EarnedEntry> earned =
                List.of(
                        new EarnedEntry(JN_PRACTICAL, bd(100), bd(100)),
                        new EarnedEntry(JN_PRACTICAL, bd(50), bd(100)),
                        new EarnedEntry(JN_PRACTICAL, bd(90), bd(100)),
                        new EarnedEntry(JN_ATTENDANCE, bd(10), bd(10)));

        var perComponent = calculator.earnedPerComponent(IAKTT1105_COMPONENTS, earned);

        assertThat(perComponent.get(JN_PRACTICAL)).isEqualByComparingTo("8.00");
        assertThat(perComponent.get(JN_ATTENDANCE)).isEqualByComparingTo("10.00");
        assertThat(perComponent.get(JN_INDEPENDENT)).isEqualByComparingTo("0");
        // the parent earns what its children earned
        assertThat(perComponent.get(JN)).isEqualByComparingTo("18.00");
        assertThat(calculator.compute(IAKTT1105_COMPONENTS, earned, AIFU_100_5_BANDS).finalScore())
                .isEqualByComparingTo("18.00");
    }

    @Test
    void fullMarks_scoresOneHundred_bandFive_admitted_noDebt() {
        List<EarnedEntry> earned =
                List.of(
                        new EarnedEntry(JN_ATTENDANCE, bd(10), bd(10)),
                        new EarnedEntry(JN_PRACTICAL, bd(10), bd(10)),
                        new EarnedEntry(JN_INDEPENDENT, bd(10), bd(10)),
                        new EarnedEntry(ON, bd(30), bd(30)),
                        new EarnedEntry(YAN, bd(40), bd(40)));

        FinalScoreResult result =
                calculator.compute(IAKTT1105_COMPONENTS, earned, AIFU_100_5_BANDS);

        assertThat(result.finalScore()).isEqualByComparingTo("100.00");
        assertThat(result.bandSymbol()).isEqualTo("5");
        assertThat(result.passing()).isTrue();
        assertThat(result.admittedToFinal()).isTrue();
        assertThat(result.academicDebt()).isFalse();
        assertThat(result.earnedByTopLevelComponent().get(JN)).isEqualByComparingTo("30");
    }

    @Test
    void jnChildrenSumBelowSixtyPercent_blocksAdmissionToFinal() {
        // JN children sum to 17/30 = 56.67% < 60% -> JN's own gate fails, blocksFinal=true.
        List<EarnedEntry> earned =
                List.of(
                        new EarnedEntry(JN_ATTENDANCE, bd(7), bd(10)),
                        new EarnedEntry(JN_PRACTICAL, bd(5), bd(10)),
                        new EarnedEntry(JN_INDEPENDENT, bd(5), bd(10)),
                        new EarnedEntry(ON, bd(30), bd(30)),
                        new EarnedEntry(YAN, bd(40), bd(40)));

        FinalScoreResult result =
                calculator.compute(IAKTT1105_COMPONENTS, earned, AIFU_100_5_BANDS);

        assertThat(result.admittedToFinal()).isFalse();
        assertThat(result.academicDebt()).isFalse();
    }

    @Test
    void onBelowSixtyPercent_blocksAdmissionToFinal() {
        List<EarnedEntry> earned =
                List.of(
                        new EarnedEntry(JN_ATTENDANCE, bd(10), bd(10)),
                        new EarnedEntry(JN_PRACTICAL, bd(10), bd(10)),
                        new EarnedEntry(JN_INDEPENDENT, bd(10), bd(10)),
                        new EarnedEntry(ON, bd(17), bd(30)), // 56.67% < 60%
                        new EarnedEntry(YAN, bd(40), bd(40)));

        FinalScoreResult result =
                calculator.compute(IAKTT1105_COMPONENTS, earned, AIFU_100_5_BANDS);

        assertThat(result.admittedToFinal()).isFalse();
    }

    @Test
    void yanBelowSixtyPercent_createsAcademicDebtButStillAdmitted() {
        // YaN does not block admission (blocksFinal=false) — it creates debt instead, per F4.
        List<EarnedEntry> earned =
                List.of(
                        new EarnedEntry(JN_ATTENDANCE, bd(10), bd(10)),
                        new EarnedEntry(JN_PRACTICAL, bd(10), bd(10)),
                        new EarnedEntry(JN_INDEPENDENT, bd(10), bd(10)),
                        new EarnedEntry(ON, bd(30), bd(30)),
                        new EarnedEntry(YAN, bd(23), bd(40))); // 57.5% < 60%

        FinalScoreResult result =
                calculator.compute(IAKTT1105_COMPONENTS, earned, AIFU_100_5_BANDS);

        assertThat(result.admittedToFinal()).isTrue();
        assertThat(result.academicDebt()).isTrue();
    }

    @Test
    void bandBoundaries_matchTheSeededAifu100_5Scale() {
        assertThat(bandFor(59)).isEqualTo("2");
        assertThat(bandFor(60)).isEqualTo("3");
        assertThat(bandFor(79)).isEqualTo("3");
        assertThat(bandFor(80)).isEqualTo("4");
        assertThat(bandFor(89)).isEqualTo("4");
        assertThat(bandFor(90)).isEqualTo("5");
    }

    private String bandFor(int totalScore) {
        // A single 100-point top-level component, isolated from the gate scenarios above, purely
        // to exercise band-boundary resolution.
        UUID overall = UUID.randomUUID();
        List<ComponentSpec> singleComponent =
                List.of(
                        new ComponentSpec(
                                overall,
                                null,
                                AssessmentComponentKind.FINAL,
                                bd(100),
                                null,
                                false));
        List<EarnedEntry> earned = List.of(new EarnedEntry(overall, bd(totalScore), bd(100)));
        return calculator.compute(singleComponent, earned, AIFU_100_5_BANDS).bandSymbol();
    }

    private static BigDecimal bd(int value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
