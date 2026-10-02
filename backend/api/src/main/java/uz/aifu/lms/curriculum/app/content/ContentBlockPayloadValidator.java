package uz.aifu.lms.curriculum.app.content;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Set;
import uz.aifu.lms.curriculum.domain.ContentBlockType;

/**
 * One handler per {@link ContentBlockType} (or a small family of closely related ones). Adding a
 * new block type means: add the enum value, add or extend a handler, add a test — never touch a
 * central switch statement (phase-plan.md's DoD for this phase).
 *
 * <p>Both {@code settings} (non-localized, e.g. code-sample language) and {@code payload}
 * (localized, teacher-authored) arrive here UNSANITIZED. Implementations must sanitize/validate
 * before returning — nothing downstream re-checks.
 */
public interface ContentBlockPayloadValidator {

    Set<ContentBlockType> supports();

    /**
     * @return the settings JSON to persist, validated/normalized. Throws on an invalid shape.
     */
    JsonNode validateSettings(JsonNode settings);

    /**
     * @return the payload JSON to persist, sanitized. Throws on an invalid shape or hostile
     *     content.
     */
    JsonNode validatePayload(JsonNode payload);

    /** Flattened text for the FTS column only — never returned to a client. */
    String plainText(JsonNode validatedPayload);

    /** Whether this type requires {@code content_blocks.file_id} to be set. */
    default boolean requiresFile() {
        return false;
    }
}
