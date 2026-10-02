package uz.aifu.lms.curriculum.app.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.curriculum.domain.ContentBlockType;

/**
 * The DoD for this phase: "adding a block type... touches enum + handler + test only." This only
 * holds if the registry actually fails when a type is left unhandled — proven here, and also
 * enforced live at application startup (see the registry's constructor).
 */
class ContentBlockValidatorRegistryTest {

    private final ContentSanitizer sanitizer = new ContentSanitizer();

    @Test
    void everyContentBlockType_hasExactlyOneRegisteredValidator() {
        ContentBlockValidatorRegistry registry = new ContentBlockValidatorRegistry(allValidators());

        for (ContentBlockType type : ContentBlockType.values()) {
            assertThat(registry.forType(type)).as("validator for %s", type).isNotNull();
            assertThat(registry.forType(type).supports()).contains(type);
        }
    }

    @Test
    void missingValidator_failsFastAtConstruction() {
        List<ContentBlockPayloadValidator> incomplete =
                allValidators().stream()
                        .filter(v -> !(v instanceof ReferenceBlockValidator))
                        .toList();

        assertThatThrownBy(() -> new ContentBlockValidatorRegistry(incomplete))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No ContentBlockPayloadValidator registered for");
    }

    @Test
    void duplicateClaimOnSameType_failsFastAtConstruction() {
        List<ContentBlockPayloadValidator> duplicated = new java.util.ArrayList<>(allValidators());
        duplicated.add(new RichTextBlockValidator(sanitizer));

        assertThatThrownBy(() -> new ContentBlockValidatorRegistry(duplicated))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Two ContentBlockPayloadValidators claim");
    }

    private List<ContentBlockPayloadValidator> allValidators() {
        return List.of(
                new RichTextBlockValidator(sanitizer),
                new CalloutBlockValidator(sanitizer),
                new CodeExampleBlockValidator(sanitizer),
                new FileBackedBlockValidator(sanitizer),
                new LinkBlockValidator(sanitizer),
                new EmbedBlockValidator(sanitizer),
                new ReferenceBlockValidator());
    }
}
