package uz.aifu.lms.curriculum.app.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Set;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.curriculum.domain.ContentBlockType;

/**
 * {@code TEXT}, {@code CASE_STUDY}, {@code TABLE}: a single sanitized rich-text field. {@code
 * ContentSanitizer}'s allowlist already includes table/thead/tbody/tr/th/td, so {@code TABLE} needs
 * no special handling beyond the shared rich-text path.
 */
@Component
public class RichTextBlockValidator implements ContentBlockPayloadValidator {

    private final ContentSanitizer sanitizer;

    public RichTextBlockValidator(ContentSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    @Override
    public Set<ContentBlockType> supports() {
        return Set.of(ContentBlockType.TEXT, ContentBlockType.CASE_STUDY, ContentBlockType.TABLE);
    }

    @Override
    public JsonNode validateSettings(JsonNode settings) {
        return JsonNodeFactory.instance.objectNode();
    }

    @Override
    public JsonNode validatePayload(JsonNode payload) {
        String html = sanitizer.sanitizeRichText(ContentBlockJson.requireText(payload, "html"));
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.set("html", ContentBlockJson.text(html));
        return result;
    }

    @Override
    public String plainText(JsonNode validatedPayload) {
        return sanitizer.toSearchText(validatedPayload.get("html").asText());
    }
}
