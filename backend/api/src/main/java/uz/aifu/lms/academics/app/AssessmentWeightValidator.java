package uz.aifu.lms.academics.app;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;
import uz.aifu.lms.academics.domain.AssessmentComponent;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;

/**
 * "Assessment components are a tree and their weights must sum to the scheme total"
 * (skills/educational-domain/SKILL.md). Only TOP-LEVEL components (no parent) count toward the
 * scheme total — {@code JN}'s own children (attendance/practical/independent) sum to {@code JN}'s
 * points, not to 100 again.
 */
@Component
public class AssessmentWeightValidator {

    public static final BigDecimal SCHEME_TOTAL = BigDecimal.valueOf(100);

    public void validateTopLevelWeights(List<AssessmentComponent> topLevelComponents) {
        if (topLevelComponents.isEmpty()) {
            throw Exceptions.rule(
                    ErrorCode.ASSESSMENT_WEIGHTS_INVALID,
                    "The course has no top-level assessment components; weights cannot sum to "
                            + SCHEME_TOTAL);
        }
        BigDecimal sum =
                topLevelComponents.stream()
                        .map(AssessmentComponent::getMaxPoints)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(SCHEME_TOTAL) != 0) {
            throw Exceptions.rule(
                            ErrorCode.ASSESSMENT_WEIGHTS_INVALID,
                            "Top-level assessment component weights must sum to "
                                    + SCHEME_TOTAL
                                    + ", found "
                                    + sum)
                    .with("sum", sum.toPlainString());
        }
    }
}
