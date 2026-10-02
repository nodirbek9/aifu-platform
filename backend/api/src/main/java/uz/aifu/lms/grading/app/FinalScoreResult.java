package uz.aifu.lms.grading.app;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * {@code finalScore} is 0-100 by construction (F4, syllabus-analysis.md §2): top-level component
 * {@code maxPoints} already sum to 100 (enforced by {@code
 * academics.app.AssessmentWeightValidator}), so summing each top-level component's own earned
 * points needs no separate weighting step.
 */
public record FinalScoreResult(
        BigDecimal finalScore,
        String bandSymbol,
        boolean passing,
        boolean admittedToFinal,
        boolean academicDebt,
        Map<UUID, BigDecimal> earnedByTopLevelComponent) {}
