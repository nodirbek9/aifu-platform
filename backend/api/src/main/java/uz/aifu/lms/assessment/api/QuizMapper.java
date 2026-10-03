package uz.aifu.lms.assessment.api;

import java.util.List;
import uz.aifu.lms.assessment.api.dto.QuizDtos.QuizDetail;
import uz.aifu.lms.assessment.api.dto.QuizDtos.QuizSummary;
import uz.aifu.lms.assessment.api.dto.QuizDtos.QuizTranslationView;
import uz.aifu.lms.assessment.domain.Quiz;
import uz.aifu.lms.assessment.domain.QuizTranslation;

/** Shared entity-to-DTO mapping for the quiz controllers. */
final class QuizMapper {

    private QuizMapper() {}

    private record TranslationResolution(
            String title, String description, String instructions, boolean fallback) {}

    /** Same three-step resolution as {@code curriculum.api.ModuleMapper} (ADR-005). */
    private static TranslationResolution resolve(
            List<QuizTranslation> translations, String requestedLocale, String defaultLocale) {
        return translations.stream()
                .filter(t -> t.getLocale().equals(requestedLocale))
                .findFirst()
                .map(
                        t ->
                                new TranslationResolution(
                                        t.getTitle(),
                                        t.getDescription(),
                                        t.getInstructions(),
                                        false))
                .or(
                        () ->
                                translations.stream()
                                        .filter(t -> t.getLocale().equals(defaultLocale))
                                        .findFirst()
                                        .map(
                                                t ->
                                                        new TranslationResolution(
                                                                t.getTitle(),
                                                                t.getDescription(),
                                                                t.getInstructions(),
                                                                true)))
                .or(
                        () ->
                                translations.stream()
                                        .findFirst()
                                        .map(
                                                t ->
                                                        new TranslationResolution(
                                                                t.getTitle(),
                                                                t.getDescription(),
                                                                t.getInstructions(),
                                                                true)))
                .orElse(new TranslationResolution(null, null, null, true));
    }

    static QuizSummary toSummary(
            Quiz quiz, List<QuizTranslation> translations, String resolvedLocale) {
        TranslationResolution t =
                resolve(
                        translations,
                        resolvedLocale,
                        quiz.getCourse().getInstructionLanguage().code());
        return new QuizSummary(
                quiz.getId(),
                quiz.getCourse().getId(),
                quiz.getCode(),
                quiz.getStatus(),
                quiz.getMaxPoints(),
                quiz.getMaxAttempts(),
                t.title(),
                t.fallback());
    }

    static QuizDetail toDetail(
            Quiz quiz, List<QuizTranslation> translations, String resolvedLocale) {
        TranslationResolution t =
                resolve(
                        translations,
                        resolvedLocale,
                        quiz.getCourse().getInstructionLanguage().code());
        return new QuizDetail(
                quiz.getId(),
                quiz.getCourse().getId(),
                quiz.getCode(),
                quiz.getStatus(),
                quiz.getMaxPoints(),
                quiz.getPassingPercent(),
                quiz.getTimeLimitSeconds(),
                quiz.getMaxAttempts(),
                quiz.getQuestionPoolSize(),
                quiz.isShuffleQuestions(),
                quiz.isShuffleOptions(),
                quiz.getRevealPolicy(),
                quiz.getPublishedAt(),
                t.title(),
                t.description(),
                t.instructions(),
                t.fallback(),
                translations.stream()
                        .map(
                                tr ->
                                        new QuizTranslationView(
                                                tr.getLocale(),
                                                tr.getTitle(),
                                                tr.getDescription(),
                                                tr.getInstructions()))
                        .toList());
    }
}
