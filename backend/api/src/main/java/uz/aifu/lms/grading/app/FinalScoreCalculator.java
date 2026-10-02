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
 *
 * <p>What a component has earned is the share of its own points that its entries earned of theirs:
 * {@code maxPoints × Σ score / Σ maxScore}. One entry marked out of the component's own maximum is
 * simply its score; several graded works under one component (five practical tasks out of 100 each,
 * in a component worth 10 points) weigh by their own maximums — never one of them replacing the
 * others, and never more than the component is worth.
 */
@Component
public class FinalScoreCalculator {

    public FinalScoreResult compute(
            List<ComponentSpec> components,
            List<EarnedEntry> earnedEntries,
            List<GradeBand> bands) {
        Map<UUID, BigDecimal> earnedByComponent = earnedPerComponent(components, earnedEntries);

        List<ComponentSpec> topLevel =
                components.stream().filter(ComponentSpec::isTopLevel).toList();

        Map<UUID, BigDecimal> earnedByTopLevel = new HashMap<>();
        BigDecimal finalScore = BigDecimal.ZERO;
        boolean admittedToFinal = true;
        boolean academicDebt = false;

        for (ComponentSpec top : topLevel) {
            BigDecimal earned = earnedByComponent.getOrDefault(top.id(), BigDecimal.ZERO);
            earnedByTopLevel.put(top.id(), earned);
            finalScore = finalScore.add(earned);

            if (top.minPassPercent() != null) {
                boolean passedGate = passesGate(earned, top);
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

    /**
     * What every component has earned: a component that is graded directly by its entries, a
     * component with children by the sum of its children. A component nobody has graded yet has
     * earned zero.
     */
    public Map<UUID, BigDecimal> earnedPerComponent(
            List<ComponentSpec> components, List<EarnedEntry> earnedEntries) {
        Map<UUID, BigDecimal> scoreSums = new HashMap<>();
        Map<UUID, BigDecimal> maxSums = new HashMap<>();
        for (EarnedEntry entry : earnedEntries) {
            scoreSums.merge(entry.componentId(), entry.score(), BigDecimal::add);
            maxSums.merge(entry.componentId(), entry.maxScore(), BigDecimal::add);
        }

        Map<UUID, BigDecimal> direct = new HashMap<>();
        for (ComponentSpec component : components) {
            BigDecimal maxSum = maxSums.get(component.id());
            if (maxSum == null || maxSum.signum() == 0 || component.maxPoints() == null) {
                direct.put(component.id(), BigDecimal.ZERO);
                continue;
            }
            direct.put(
                    component.id(),
                    component
                            .maxPoints()
                            .multiply(scoreSums.get(component.id()))
                            .divide(maxSum, 2, RoundingMode.HALF_UP));
        }

        Map<UUID, BigDecimal> earned = new HashMap<>();
        for (ComponentSpec component : components) {
            List<ComponentSpec> children =
                    components.stream().filter(c -> component.id().equals(c.parentId())).toList();
            earned.put(
                    component.id(),
                    children.isEmpty()
                            ? direct.get(component.id())
                            : children.stream()
                                    .map(c -> direct.get(c.id()))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return earned;
    }

    /** Whether what was earned reaches the component's own pass mark. */
    public boolean passesGate(BigDecimal earned, ComponentSpec component) {
        return percentOf(earned, component.maxPoints()).compareTo(component.minPassPercent()) >= 0;
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
