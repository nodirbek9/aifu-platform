package uz.aifu.lms.grading.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import uz.aifu.lms.academics.domain.AssessmentComponentKind;

/**
 * `IAKTT1105`'s "Talabalar bilimini baholash" rubric, made machine-computable (ADR-011,
 * skills/educational-domain/SKILL.md rule 7): {@code YaB = JN + ON + YaN}, where a student who
 * earns under {@code min_pass_percent} of a {@code blocksFinal} component is not admitted to the
 * final, and under {@code min_pass_percent} of the FINAL-kind component becomes an academic debtor.
 * A pure function — no repository, no entity, easily unit-tested (mirrors {@code
 * execution-worker.grading.Scorer}'s split from Phase 8).
 */
@Component
public class FinalScoreCalculator {

    public FinalScoreResult compute(
            List<ComponentSpec> components,
            List<EarnedEntry> earnedEntries,
            List<GradeBand> bands) {
        Map<UUID, BigDecimal> earnedByComponent = new HashMap<>();
        for (EarnedEntry entry : earnedEntries) {
            earnedByComponent.put(entry.componentId(), entry.score());
        }

        List<ComponentSpec> topLevel =
                components.stream().filter(ComponentSpec::isTopLevel).toList();
        Map<UUID, BigDecimal> earnedByTopLevel = new HashMap<>();
        BigDecimal finalScore = BigDecimal.ZERO;
        boolean admittedToFinal = true;
        boolean academicDebt = false;

        for (ComponentSpec top : topLevel) {
            List<ComponentSpec> children =
                    components.stream().filter(c -> top.id().equals(c.parentId())).toList();
            BigDecimal earned =
                    children.isEmpty()
                            ? earnedByComponent.getOrDefault(top.id(), BigDecimal.ZERO)
                            : children.stream()
                                    .map(
                                            c ->
                                                    earnedByComponent.getOrDefault(
                                                            c.id(), BigDecimal.ZERO))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            earnedByTopLevel.put(top.id(), earned);
            finalScore = finalScore.add(earned);

            if (top.minPassPercent() != null) {
                BigDecimal earnedPercent = percentOf(earned, top.maxPoints());
                boolean passedGate = earnedPercent.compareTo(top.minPassPercent()) >= 0;
                if (top.blocksFinal() && !passedGate) {
                    admittedToFinal = false;
                }
                if (top.kind() == AssessmentComponentKind.FINAL && !passedGate) {
                    academicDebt = true;
                }
            }
        }

        GradeBand band = resolveBand(bands, finalScore);
        return new FinalScoreResult(
                finalScore.setScale(2, RoundingMode.HALF_UP),
                band == null ? null : band.symbol(),
                band != null && band.passing(),
                admittedToFinal,
                academicDebt,
                earnedByTopLevel);
    }

    private static BigDecimal percentOf(BigDecimal earned, BigDecimal maxPoints) {
        if (maxPoints == null || maxPoints.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return earned.multiply(BigDecimal.valueOf(100)).divide(maxPoints, 4, RoundingMode.HALF_UP);
    }

    private static GradeBand resolveBand(List<GradeBand> bands, BigDecimal finalScore) {
        return bands.stream()
                .filter(
                        b ->
                                finalScore.compareTo(b.minScore()) >= 0
                                        && finalScore.compareTo(b.maxScore()) <= 0)
                .findFirst()
                .orElse(null);
    }
}
