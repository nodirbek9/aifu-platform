package uz.aifu.lms.grading.app;

import java.math.BigDecimal;
import java.util.UUID;
import uz.aifu.lms.academics.domain.AssessmentComponentKind;

/**
 * The subset of one {@code academics.domain.AssessmentComponent} {@link FinalScoreCalculator} needs
 * — a plain record, not the JPA entity itself, so the calculator stays a pure function testable
 * without a database (mirrors {@code execution-worker}'s {@code TestCaseRecord}/{@code Scorer}
 * split, Phase 8).
 */
public record ComponentSpec(
        UUID id,
        UUID parentId,
        AssessmentComponentKind kind,
        BigDecimal maxPoints,
        BigDecimal minPassPercent,
        boolean blocksFinal) {

    public boolean isTopLevel() {
        return parentId == null;
    }
}
