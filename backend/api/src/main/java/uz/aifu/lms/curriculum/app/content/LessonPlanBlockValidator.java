package uz.aifu.lms.curriculum.app.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.curriculum.domain.ContentBlockType;

/**
 * {@code LESSON_PLAN}: the teacher's plan of a class (product spec T-03.4) — never shown to
 * students. The payload is structured so the class page can lay it out and total the minutes:
 *
 * <pre>{
 *   "objectives": "&lt;rich text&gt;",
 *   "timeline":   [{"minutes": 10, "title": "Revision", "notes": "&lt;rich text&gt;"}],
 *   "materials":  "&lt;rich text&gt;",
 *   "questions":  "&lt;rich text&gt;",
 *   "homework":   "&lt;rich text&gt;"
 * }</pre>
 *
 * Every part is optional, but an empty plan is refused.
 */
@Component
public class LessonPlanBlockValidator implements ContentBlockPayloadValidator {

    private static final List<String> RICH_TEXT_PARTS =
            List.of("objectives", "materials", "questions", "homework");
    private static final int MAX_TIMELINE_STEPS = 30;

    private final ContentSanitizer sanitizer;

    public LessonPlanBlockValidator(ContentSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    @Override
    public Set<ContentBlockType> supports() {
        return Set.of(ContentBlockType.LESSON_PLAN);
    }

    @Override
    public JsonNode validateSettings(JsonNode settings) {
        return JsonNodeFactory.instance.objectNode();
    }

    @Override
    public JsonNode validatePayload(JsonNode payload) {
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        for (String part : RICH_TEXT_PARTS) {
            String text = ContentBlockJson.optionalText(payload, part);
            if (text != null) {
                result.put(part, sanitizer.sanitizeRichText(text));
            }
        }
        JsonNode timeline = payload == null ? null : payload.get("timeline");
        if (timeline != null && !timeline.isNull()) {
            if (!timeline.isArray() || timeline.size() > MAX_TIMELINE_STEPS) {
                throw Exceptions.validation(
                        "\"timeline\" must be a list of at most " + MAX_TIMELINE_STEPS + " steps");
            }
            ArrayNode steps = result.putArray("timeline");
            for (JsonNode step : timeline) {
                JsonNode minutes = step.get("minutes");
                if (minutes == null || !minutes.isInt() || minutes.asInt() < 1) {
                    throw Exceptions.validation(
                            "Each timeline step needs \"minutes\" as a whole number above zero");
                }
                ObjectNode clean = steps.addObject();
                clean.put("minutes", minutes.asInt());
                clean.put(
                        "title",
                        sanitizer.sanitizePlainText(ContentBlockJson.requireText(step, "title")));
                String notes = ContentBlockJson.optionalText(step, "notes");
                if (notes != null) {
                    clean.put("notes", sanitizer.sanitizeRichText(notes));
                }
            }
        }
        if (result.isEmpty()) {
            throw Exceptions.validation(
                    "A lesson plan needs at least one of: objectives, timeline, materials,"
                            + " questions, homework");
        }
        return result;
    }

    /** A teacher's private plan is not searchable content. */
    @Override
    public String plainText(JsonNode validatedPayload) {
        return "";
    }
}
