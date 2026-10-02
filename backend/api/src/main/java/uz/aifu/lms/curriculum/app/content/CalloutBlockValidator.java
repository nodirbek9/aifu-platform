package uz.aifu.lms.curriculum.app.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Set;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.curriculum.domain.ContentBlockType;

/**
 * {@code CALLOUT}: sanitized rich text plus a required {@code variant} setting (V007 comment:
 * "callout variant").
 */
@Component
public class CalloutBlockValidator implements ContentBlockPayloadValidator {

    public enum Variant {
        INFO,
        WARNING,
        TIP,
        DANGER
    }

    private final ContentSanitizer sanitizer;

    public CalloutBlockValidator(ContentSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    @Override
    public Set<ContentBlockType> supports() {
        return Set.of(ContentBlockType.CALLOUT);
    }

    @Override
    public JsonNode validateSettings(JsonNode settings) {
        Variant variant = ContentBlockJson.requireEnum(settings, "variant", Variant.class);
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("variant", variant.name());
        return result;
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
