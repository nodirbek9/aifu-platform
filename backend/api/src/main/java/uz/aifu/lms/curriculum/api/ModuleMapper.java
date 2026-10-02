package uz.aifu.lms.curriculum.api;

import java.util.List;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ModuleDetail;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ModuleSummary;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ModuleTranslationView;
import uz.aifu.lms.curriculum.app.ModuleService;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.curriculum.domain.CourseModuleTranslation;

/** Shared entity-to-DTO mapping for the module controllers. */
final class ModuleMapper {

    private ModuleMapper() {}

    record TitleResolution(String text, boolean fallback) {}

    /**
     * Requested locale, else the course's default, else the first available, flagging fallback —
     * same three-step resolution as {@code academics.api.SubjectMapper} (ADR-005).
     */
    static TitleResolution resolveTitle(
            List<CourseModuleTranslation> translations,
            String requestedLocale,
            String defaultLocale) {
        return translations.stream()
                .filter(t -> t.getLocale().equals(requestedLocale))
                .findFirst()
                .map(t -> new TitleResolution(t.getTitle(), false))
                .or(
                        () ->
                                translations.stream()
                                        .filter(t -> t.getLocale().equals(defaultLocale))
                                        .findFirst()
                                        .map(t -> new TitleResolution(t.getTitle(), true)))
                .or(
                        () ->
                                translations.stream()
                                        .findFirst()
                                        .map(t -> new TitleResolution(t.getTitle(), true)))
                .orElse(new TitleResolution(null, true));
    }

    static ModuleSummary toSummary(
            CourseModule module,
            List<CourseModuleTranslation> translations,
            String resolvedLocale) {
        TitleResolution title =
                resolveTitle(
                        translations,
                        resolvedLocale,
                        module.getCourse().getInstructionLanguage().code());
        return new ModuleSummary(
                module.getId(),
                module.getCourse().getId(),
                module.getPosition(),
                module.getCode(),
                module.getStatus(),
                module.isRequired(),
                title.text(),
                title.fallback());
    }

    static ModuleDetail toDetail(CourseModule module, ModuleService modules) {
        List<ModuleTranslationView> translationViews =
                modules.listTranslations(module.getId()).stream()
                        .map(
                                t ->
                                        new ModuleTranslationView(
                                                t.getLocale(), t.getTitle(), t.getDescription()))
                        .toList();
        return new ModuleDetail(
                module.getId(),
                module.getCourse().getId(),
                module.getPosition(),
                module.getCode(),
                module.getStatus(),
                module.isRequired(),
                module.getEstimatedMinutes(),
                module.getAvailableFrom(),
                module.getGroupingSource(),
                module.getPublishedAt(),
                translationViews);
    }
}
