package uz.aifu.lms.curriculum.app.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import uz.aifu.lms.common.error.Exceptions;

/**
 * Small shared field-extraction helpers so the seven validators don't each repeat the same {@code
 * JsonNode} null/type-checking boilerplate.
 */
final class ContentBlockJson {

    private ContentBlockJson() {}

    static String requireText(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw Exceptions.validation("\"" + field + "\" is required and must be non-blank text");
        }
        return value.asText().trim();
    }

    static String optionalText(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value != null && value.isTextual() && !value.asText().isBlank()
                ? value.asText().trim()
                : null;
    }

    static <E extends Enum<E>> E requireEnum(JsonNode node, String field, Class<E> type) {
        String raw = requireText(node, field);
        try {
            return Enum.valueOf(type, raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw Exceptions.validation(
                    "\""
                            + field
                            + "\" must be one of "
                            + java.util.Arrays.toString(type.getEnumConstants()));
        }
    }

    static com.fasterxml.jackson.databind.node.ObjectNode object(JsonNode existing) {
        return existing != null && existing.isObject()
                ? (com.fasterxml.jackson.databind.node.ObjectNode) existing
                : com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
    }

    static JsonNode text(String value) {
        return value == null ? null : TextNode.valueOf(value);
    }
}
