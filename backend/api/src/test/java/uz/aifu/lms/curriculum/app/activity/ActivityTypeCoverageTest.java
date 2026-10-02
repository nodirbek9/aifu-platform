package uz.aifu.lms.curriculum.app.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.curriculum.domain.ActivityType;

/**
 * Named to match the phase-plan.md Tests list verbatim ("ActivityTypeCoverageTest"). Same
 * completeness guarantee as {@code ContentBlockValidatorRegistryTest} — every {@link ActivityType}
 * must resolve to exactly one handler.
 */
class ActivityTypeCoverageTest {

    @Test
    void everyActivityType_hasExactlyOneRegisteredHandler() {
        ActivityTypeHandlerRegistry registry = new ActivityTypeHandlerRegistry(allHandlers());

        for (ActivityType type : ActivityType.values()) {
            assertThat(registry.forType(type)).as("handler for %s", type).isNotNull();
            assertThat(registry.forType(type).supports()).contains(type);
        }
    }

    @Test
    void referencedTypes_requireAReference() {
        ActivityTypeHandlerRegistry registry = new ActivityTypeHandlerRegistry(allHandlers());

        assertThat(registry.forType(ActivityType.QUIZ).requiresReference()).isTrue();
        assertThat(registry.forType(ActivityType.CODING_TASK).requiresReference()).isTrue();
        assertThat(registry.forType(ActivityType.MANUAL_TASK).requiresReference()).isFalse();
        assertThat(registry.forType(ActivityType.DISCUSSION).requiresReference()).isFalse();
    }

    @Test
    void missingHandler_failsFastAtConstruction() {
        List<ActivityTypeHandler> incomplete = List.of(new ReferencedActivityTypeHandler());

        assertThatThrownBy(() -> new ActivityTypeHandlerRegistry(incomplete))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No ActivityTypeHandler registered for");
    }

    private List<ActivityTypeHandler> allHandlers() {
        return List.of(new ReferencedActivityTypeHandler(), new SelfContainedActivityTypeHandler());
    }
}
