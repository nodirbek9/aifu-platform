package uz.aifu.lms.grading.app;

import java.math.BigDecimal;

/** The subset of one {@code academics.domain.GradeScaleBand} {@link FinalScoreCalculator} needs. */
public record GradeBand(BigDecimal minScore, BigDecimal maxScore, String symbol, boolean passing) {}
