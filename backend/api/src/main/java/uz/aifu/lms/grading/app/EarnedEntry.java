package uz.aifu.lms.grading.app;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One published {@code GradeEntry}'s contribution to a component — the subset {@link
 * FinalScoreCalculator} needs.
 */
public record EarnedEntry(UUID componentId, BigDecimal score, BigDecimal maxScore) {}
