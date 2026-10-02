package uz.aifu.lms.curriculum.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonDetail;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonSummary;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonTranslationView;
import uz.aifu.lms.curriculum.app.ContentBlockService;
import uz.aifu.lms.curriculum.app.LessonService;
import uz.aifu.lms.curriculum.domain.ContentBlock;
import uz.aifu.lms.curriculum.domain.ContentBlockTranslation;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.curriculum.domain.LessonTranslation;

/** Shared entity-to-DTO mapping for the lesson controllers. */
final class LessonMapper {

    private LessonMapper() {}

    static ModuleMapper.TitleResolution resolveTitle(
            List<LessonTranslation> translations, String requestedLocale, String defaultLocale) {
        return translations.stream()
                .filter(t -> t.getLocale().equals(requestedLocale))
                .findFirst()
                .map(t -> new ModuleMapper.TitleResolution(t.getTitle(), false))
                .or(
                        () ->
                                translations.stream()
                                        .filter(t -> t.getLocale().equals(defaultLocale))
                                        .findFirst()
                                        .map(
                                                t ->
                                                        new ModuleMapper.TitleResolution(
                                                                t.getTitle(), true)))
                .or(
                        () ->
                                translations.stream()
                                        .findFirst()
                                        .map(
                                                t ->
                                                        new ModuleMapper.TitleResolution(
                                                                t.getTitle(), true)))
                .orElse(new ModuleMapper.TitleResolution(null, true));
    }

    static LessonSummary toSummary(
            Lesson lesson, List<LessonTranslation> translations, String resolvedLocale) {
        ModuleMapper.TitleResolution title =
                resolveTitle(
                        translations,
                        resolvedLocale,
                        lesson.getModule().getCourse().getInstructionLanguage().code());
        return new LessonSummary(
                lesson.getId(),
                lesson.getModule().getId(),
                lesson.getPosition(),
                lesson.getCode(),
                lesson.getSessionType(),
                lesson.getStatus(),
                title.text(),
                title.fallback());
    }

    static LessonDetail toDetail(
            Lesson lesson,
            LessonService lessons,
            ContentBlockService blocks,
            String resolvedLocale,
            ObjectMapper objectMapper) {
        List<LessonTranslationView> translationViews =
                lessons.listTranslations(lesson.getId()).stream()
                        .map(
                                t ->
                                        new LessonTranslationView(
                                                t.getLocale(), t.getTitle(), t.getSummary()))
                        .toList();
        List<java.util.UUID> outcomeIds =
                lessons.listOutcomes(lesson.getId()).stream()
                        .map(o -> o.getLearningOutcome().getId())
                        .toList();
        List<java.util.UUID> prerequisiteIds =
                lessons.listPrerequisites(lesson.getId()).stream()
                        .map(p -> p.getRequiredLesson().getId())
                        .toList();
        String defaultLocale = lesson.getModule().getCourse().getInstructionLanguage().code();
        List<ContentBlock> blockEntities = blocks.listByLesson(lesson.getId());
        Map<java.util.UUID, List<ContentBlockTranslation>> blockTranslations =
                blocks.listTranslationsForBlocks(
                        blockEntities.stream().map(ContentBlock::getId).toList());
        List<uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.ContentBlockView> blockViews =
                blockEntities.stream()
                        .map(
                                b ->
                                        ContentBlockMapper.toView(
                                                b,
                                                blockTranslations.getOrDefault(
                                                        b.getId(), List.of()),
                                                resolvedLocale,
                                                defaultLocale,
                                                objectMapper))
                        .toList();
        return new LessonDetail(
                lesson.getId(),
                lesson.getModule().getId(),
                lesson.getPosition(),
                lesson.getCode(),
                lesson.getSessionType(),
                lesson.getAllocatedHours(),
                lesson.getEstimatedMinutes(),
                lesson.getDifficulty(),
                lesson.isRequired(),
                lesson.getStatus(),
                lesson.getAvailableFrom(),
                lesson.getPublishedAt(),
                translationViews,
                outcomeIds,
                prerequisiteIds,
                blockViews);
    }
}
