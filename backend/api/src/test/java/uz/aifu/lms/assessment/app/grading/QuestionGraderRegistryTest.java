package uz.aifu.lms.assessment.app.grading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.assessment.domain.QuestionType;

/**
 * The DoD implied by Phase 7's grader extension point (mirrors {@code
 * ContentBlockValidatorRegistryTest}): every {@link QuestionType} must have exactly one grader,
 * enforced at construction, not first use.
 */
class QuestionGraderRegistryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void everyQuestionType_hasExactlyOneRegisteredGrader() {
        QuestionGraderRegistry registry = new QuestionGraderRegistry(allGraders());

        for (QuestionType type : QuestionType.values()) {
            assertThat(registry.forType(type)).as("grader for %s", type).isNotNull();
            assertThat(registry.forType(type).supports()).contains(type);
        }
    }

    @Test
    void missingGrader_failsFastAtConstruction() {
        List<QuestionGrader> incomplete =
                allGraders().stream().filter(g -> !(g instanceof MatchingGrader)).toList();

        assertThatThrownBy(() -> new QuestionGraderRegistry(incomplete))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No QuestionGrader registered for");
    }

    @Test
    void duplicateClaimOnSameType_failsFastAtConstruction() {
        List<QuestionGrader> duplicated = new ArrayList<>(allGraders());
        duplicated.add(new SingleSelectionGrader());

        assertThatThrownBy(() -> new QuestionGraderRegistry(duplicated))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Two QuestionGraders claim");
    }

    private List<QuestionGrader> allGraders() {
        return List.of(
                new SingleSelectionGrader(),
                new MultipleChoiceGrader(objectMapper),
                new ShortAnswerGrader(),
                new NumericGrader(),
                new MatchingGrader(),
                new OrderingGrader(objectMapper));
    }
}
