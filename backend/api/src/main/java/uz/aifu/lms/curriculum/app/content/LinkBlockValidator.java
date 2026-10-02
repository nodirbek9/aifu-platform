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
 * {@code LINK}: an external URL plus a label. Same protocol allowlist as {@link ContentSanitizer}
 * ({@code http}/{@code https} only — no {@code javascript:}/{@code data:}).
 */
@Component
public class LinkBlockValidator implements ContentBlockPayloadValidator {

    private static final Pattern SAFE_URL =
            Pattern.compile("^https?://[^\\s<>\"]+$", Pattern.CASE_INSENSITIVE);

    private final ContentSanitizer sanitizer;

    public LinkBlockValidator(ContentSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    @Override
    public Set<ContentBlockType> supports() {
        return Set.of(ContentBlockType.LINK);
    }

    @Override
    public JsonNode validateSettings(JsonNode settings) {
        String url = ContentBlockJson.requireText(settings, "url");
        if (!SAFE_URL.matcher(url).matches()) {
            throw Exceptions.rule(
                    uz.aifu.lms.common.error.ErrorCode.UNSAFE_CONTENT_REJECTED,
                    "\"url\" must be an http(s) URL");
        }
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("url", url);
        return result;
    }

    @Override
    public JsonNode validatePayload(JsonNode payload) {
        String label = sanitizer.sanitizePlainText(ContentBlockJson.requireText(payload, "label"));
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.set("label", ContentBlockJson.text(label));
        return result;
    }

    @Override
    public String plainText(JsonNode validatedPayload) {
        return validatedPayload.get("label").asText();
    }
}
