package uz.aifu.lms.curriculum.app.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Set;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.curriculum.domain.ContentBlockType;

/**
 * {@code CODE_EXAMPLE}: raw (never HTML-escaped) code plus an optional sanitized explanation.
 * {@code settings.language} is required (V007 comment: "language of a code sample"). Not sized like
 * a coding-task submission (Phase 8) — a teaching example, capped modestly.
 */
@Component
public class CodeExampleBlockValidator implements ContentBlockPayloadValidator {

    private static final int MAX_CODE_BYTES = 20_000;

    private final ContentSanitizer sanitizer;

    public CodeExampleBlockValidator(ContentSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    @Override
    public Set<ContentBlockType> supports() {
        return Set.of(ContentBlockType.CODE_EXAMPLE);
    }

    @Override
    public JsonNode validateSettings(JsonNode settings) {
        String language = ContentBlockJson.requireText(settings, "language");
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("language", language.toLowerCase());
        return result;
    }

    @Override
    public JsonNode validatePayload(JsonNode payload) {
        String code =
                sanitizer.acceptCodeSample(
                        ContentBlockJson.requireText(payload, "code"), MAX_CODE_BYTES);
        String explanation = ContentBlockJson.optionalText(payload, "explanation");
        String sanitizedExplanation =
                explanation == null ? null : sanitizer.sanitizeRichText(explanation);
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.set("code", ContentBlockJson.text(code));
        result.set("explanation", ContentBlockJson.text(sanitizedExplanation));
        return result;
    }

    @Override
    public String plainText(JsonNode validatedPayload) {
        JsonNode explanation = validatedPayload.get("explanation");
        return explanation == null || explanation.isNull()
                ? null
                : sanitizer.toSearchText(explanation.asText());
    }
}
