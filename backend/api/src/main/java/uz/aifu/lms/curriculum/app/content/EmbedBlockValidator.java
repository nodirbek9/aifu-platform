package uz.aifu.lms.curriculum.app.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.curriculum.domain.ContentBlockType;

/**
 * {@code EMBED}: NOT free-form HTML — a provider + id, with the frontend building the iframe from
 * its own allowlist (documented in {@code ContentSanitizer}'s javadoc, which is exactly why {@code
 * <iframe>} can be banned there). {@code externalId} is restricted to a safe charset since it ends
 * up in a frontend-constructed URL.
 */
@Component
public class EmbedBlockValidator implements ContentBlockPayloadValidator {

    public enum Provider {
        YOUTUBE,
        VIMEO
    }

    private static final Pattern SAFE_EXTERNAL_ID = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private final ContentSanitizer sanitizer;

    public EmbedBlockValidator(ContentSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    @Override
    public Set<ContentBlockType> supports() {
        return Set.of(ContentBlockType.EMBED);
    }

    @Override
    public JsonNode validateSettings(JsonNode settings) {
        Provider provider = ContentBlockJson.requireEnum(settings, "provider", Provider.class);
        String externalId = ContentBlockJson.requireText(settings, "externalId");
        if (!SAFE_EXTERNAL_ID.matcher(externalId).matches()) {
            throw Exceptions.validation(
                    "\"externalId\" must be alphanumeric (with '-'/'_'), up to 64 characters");
        }
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("provider", provider.name());
        result.put("externalId", externalId);
        return result;
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
