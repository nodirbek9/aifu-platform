package uz.aifu.lms.curriculum.app.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.curriculum.domain.ContentBlockType;

/**
 * {@code QUIZ_REF}, {@code CODING_TASK_REF}, {@code ACTIVITY_REF}: a pointer to a graded artifact
 * defined elsewhere in the lesson/course, resolved by the owning module's handler at render time
 * (V007: "resolved by the ActivityHandler registered for the activity_type"). {@code
 * assessment}/{@code coding} (owning QUIZ_REF/CODING_TASK_REF) do not exist yet (Phases 7/8), so
 * only the reference's UUID *shape* is validated today, uniformly across all three — existence
 * checking is added when the referenced module lands.
 */
@Component
public class ReferenceBlockValidator implements ContentBlockPayloadValidator {

    @Override
    public Set<ContentBlockType> supports() {
        return Set.of(
                ContentBlockType.QUIZ_REF,
                ContentBlockType.CODING_TASK_REF,
                ContentBlockType.ACTIVITY_REF);
    }

    @Override
    public JsonNode validateSettings(JsonNode settings) {
        String raw = ContentBlockJson.requireText(settings, "refId");
        UUID refId;
        try {
            refId = UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw Exceptions.validation("\"refId\" must be a valid UUID");
        }
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("refId", refId.toString());
        return result;
    }

    @Override
    public JsonNode validatePayload(JsonNode payload) {
        return JsonNodeFactory.instance.objectNode();
    }

    @Override
    public String plainText(JsonNode validatedPayload) {
        return null;
    }
}
