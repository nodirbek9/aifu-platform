package uz.aifu.lms.assessment.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.AnswerKeyView;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.AnswerOptionView;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.OptionTranslationView;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.QuestionDetail;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.QuestionSummary;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.QuestionTranslationView;
import uz.aifu.lms.assessment.domain.AnswerOption;
import uz.aifu.lms.assessment.domain.AnswerOptionTranslation;
import uz.aifu.lms.assessment.domain.Question;
import uz.aifu.lms.assessment.domain.QuestionAnswerKey;
import uz.aifu.lms.assessment.domain.QuestionTranslation;

/**
 * Shared entity-to-DTO mapping for the (teacher-only) question controllers — every field, including
 * correct answers, is safe here since only a course editor ever reaches these endpoints ({@code
 * AccessGuard.requireCourseEditor}, never the student-facing quiz surface).
 */
final class QuestionMapper {

    private QuestionMapper() {}

    private record PromptResolution(String prompt, String explanation, String hint) {}

    private static PromptResolution resolve(
            List<QuestionTranslation> translations, String requestedLocale, String defaultLocale) {
        return translations.stream()
                .filter(t -> t.getLocale().equals(requestedLocale))
                .findFirst()
                .or(
                        () ->
                                translations.stream()
                                        .filter(t -> t.getLocale().equals(defaultLocale))
                                        .findFirst())
                .or(() -> translations.stream().findFirst())
                .map(t -> new PromptResolution(t.getPrompt(), t.getExplanation(), t.getHint()))
                .orElse(new PromptResolution(null, null, null));
    }

    static QuestionSummary toSummary(
            Question question,
            List<QuestionTranslation> translations,
            String resolvedLocale,
            String defaultLocale) {
        PromptResolution p = resolve(translations, resolvedLocale, defaultLocale);
        return new QuestionSummary(
                question.getId(),
                question.getQuiz().getId(),
                question.getPosition(),
                question.getQuestionType(),
                question.getPoints(),
                question.getDifficulty(),
                question.isRequired(),
                p.prompt());
    }

    static QuestionDetail toDetail(
            Question question,
            List<QuestionTranslation> translations,
            String resolvedLocale,
            String defaultLocale,
            List<AnswerOption> options,
            Map<java.util.UUID, List<AnswerOptionTranslation>> optionTranslationsById,
            List<QuestionAnswerKey> answerKeys,
            ObjectMapper objectMapper) {
        PromptResolution p = resolve(translations, resolvedLocale, defaultLocale);
        List<AnswerOptionView> optionViews =
                options.stream()
                        .map(
                                o -> {
                                    var t =
                                            resolveOptionText(
                                                    optionTranslationsById.getOrDefault(
                                                            o.getId(), List.of()),
                                                    resolvedLocale,
                                                    defaultLocale);
                                    return new AnswerOptionView(
                                            o.getId(),
                                            o.getPosition(),
                                            o.isCorrect(),
                                            o.getMatchKey(),
                                            t[0],
                                            t[1],
                                            optionTranslations(
                                                    optionTranslationsById.getOrDefault(
                                                            o.getId(), List.of())));
                                })
                        .toList();
        List<AnswerKeyView> keyViews =
                answerKeys.stream()
                        .map(
                                k ->
                                        new AnswerKeyView(
                                                k.getId(),
                                                k.getMatchMode(),
                                                k.getExpectedValue(),
                                                k.getTolerance(),
                                                k.getLocale()))
                        .toList();
        JsonNode settings;
        try {
            settings = objectMapper.readTree(question.getSettings());
        } catch (Exception e) {
            settings = objectMapper.createObjectNode();
        }
        return new QuestionDetail(
                question.getId(),
                question.getQuiz().getId(),
                question.getPosition(),
                question.getQuestionType(),
                question.getPoints(),
                question.getDifficulty(),
                question.isRequired(),
                settings,
                p.prompt(),
                p.explanation(),
                p.hint(),
                optionViews,
                keyViews,
                translations.stream()
                        .map(
                                tr ->
                                        new QuestionTranslationView(
                                                tr.getLocale(),
                                                tr.getPrompt(),
                                                tr.getHint(),
                                                tr.getExplanation()))
                        .toList());
    }

    static List<OptionTranslationView> optionTranslations(
            List<AnswerOptionTranslation> translations) {
        return translations.stream()
                .map(
                        tr ->
                                new OptionTranslationView(
                                        tr.getLocale(), tr.getText(), tr.getFeedback()))
                .toList();
    }

    private static String[] resolveOptionText(
            List<AnswerOptionTranslation> translations,
            String resolvedLocale,
            String defaultLocale) {
        return translations.stream()
                .filter(t -> t.getLocale().equals(resolvedLocale))
                .findFirst()
                .or(
                        () ->
                                translations.stream()
                                        .filter(t -> t.getLocale().equals(defaultLocale))
                                        .findFirst())
                .or(() -> translations.stream().findFirst())
                .map(t -> new String[] {t.getText(), t.getFeedback()})
                .orElse(new String[] {null, null});
    }
}
