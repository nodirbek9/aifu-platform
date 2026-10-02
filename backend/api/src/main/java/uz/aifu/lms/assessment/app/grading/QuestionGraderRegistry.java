package uz.aifu.lms.assessment.app.grading;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import uz.aifu.lms.assessment.domain.QuestionType;

/**
 * Same fail-fast completeness guarantee as {@code ContentBlockValidatorRegistry}/{@code
 * ActivityTypeHandlerRegistry}: every {@link QuestionType} must have exactly one grader, checked at
 * application startup, not on first use.
 */
@Component
public class QuestionGraderRegistry {

    private final Map<QuestionType, QuestionGrader> byType = new EnumMap<>(QuestionType.class);

    public QuestionGraderRegistry(List<QuestionGrader> graders) {
        for (QuestionGrader grader : graders) {
            for (QuestionType type : grader.supports()) {
                QuestionGrader existing = byType.put(type, grader);
                if (existing != null) {
                    throw new IllegalStateException(
                            "Two QuestionGraders claim "
                                    + type
                                    + ": "
                                    + existing.getClass().getSimpleName()
                                    + " and "
                                    + grader.getClass().getSimpleName());
                }
            }
        }
        for (QuestionType type : QuestionType.values()) {
            if (!byType.containsKey(type)) {
                throw new IllegalStateException("No QuestionGrader registered for " + type);
            }
        }
    }

    public QuestionGrader forType(QuestionType type) {
        return byType.get(type);
    }
}
