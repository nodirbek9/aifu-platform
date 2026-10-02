package uz.aifu.lms.curriculum.app.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Set;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.curriculum.domain.ContentBlockType;

/**
 * {@code IMAGE}, {@code VIDEO}, {@code AUDIO}, {@code DIAGRAM}, {@code FILE}: an uploaded file
 * (validated at the service layer against {@code content_blocks.file_id}, a real column — see
 * {@link #requiresFile()}) plus an optional caption. {@code files} itself is Phase 11 and does not
 * exist yet; the reference is a bare id today.
 */
@Component
public class FileBackedBlockValidator implements ContentBlockPayloadValidator {

    private final ContentSanitizer sanitizer;

    public FileBackedBlockValidator(ContentSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    @Override
    public Set<ContentBlockType> supports() {
        return Set.of(
                ContentBlockType.IMAGE,
                ContentBlockType.VIDEO,
                ContentBlockType.AUDIO,
                ContentBlockType.DIAGRAM,
                ContentBlockType.FILE);
    }

    @Override
    public boolean requiresFile() {
        return true;
    }

    @Override
    public JsonNode validateSettings(JsonNode settings) {
        return JsonNodeFactory.instance.objectNode();
    }

    @Override
    public JsonNode validatePayload(JsonNode payload) {
        String caption = ContentBlockJson.optionalText(payload, "caption");
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.set(
                "caption",
                ContentBlockJson.text(
                        caption == null ? null : sanitizer.sanitizePlainText(caption)));
        return result;
    }

    @Override
    public String plainText(JsonNode validatedPayload) {
        JsonNode caption = validatedPayload.get("caption");
        return caption == null || caption.isNull() ? null : caption.asText();
    }
}
