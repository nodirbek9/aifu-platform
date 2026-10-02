package uz.aifu.lms.assessment.app.grading;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Small shared helper for graders reading {@code questions.settings} (V009 column comment). */
final class QuestionSettingsJson {

    private QuestionSettingsJson() {}

    static boolean partialCredit(ObjectMapper objectMapper, String settingsJson) {
        try {
            JsonNode node = objectMapper.readTree(settingsJson);
            return node.path("partialCredit").asBoolean(false);
        } catch (Exception e) {
            return false;
        }
    }
}
