package uz.aifu.lms.academics.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import uz.aifu.lms.academics.api.dto.SubjectDtos.LearningOutcomeView;
import uz.aifu.lms.academics.api.dto.SubjectDtos.OutcomeTranslationView;
import uz.aifu.lms.academics.api.dto.SubjectDtos.PrerequisiteView;
import uz.aifu.lms.academics.api.dto.SubjectDtos.SubjectDetail;
import uz.aifu.lms.academics.api.dto.SubjectDtos.SubjectSummary;
import uz.aifu.lms.academics.api.dto.SubjectDtos.SubjectTranslationView;
import uz.aifu.lms.academics.app.SubjectService;
import uz.aifu.lms.academics.domain.LearningOutcome;
import uz.aifu.lms.academics.domain.LearningOutcomeTranslation;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.academics.domain.SubjectPrerequisite;
import uz.aifu.lms.academics.domain.SubjectTranslation;

/**
 * Shared entity-to-DTO mapping for {@link SubjectController} and {@link AdminSubjectController}.
 */
final class SubjectMapper {

    private SubjectMapper() {}

    record TitleResolution(String text, boolean fallback) {}

    /**
     * Requested locale, else the subject's default, else the first available, flagging fallback.
     */
    static TitleResolution resolveTitle(
            List<SubjectTranslation> translations, String requestedLocale, String defaultLocale) {
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

    static SubjectSummary toSummary(
            Subject subject, List<SubjectTranslation> translations, String resolvedLocale) {
        TitleResolution title =
                resolveTitle(
                        translations,
                        resolvedLocale,
                        subject.getDefaultInstructionLanguage().code());
        return new SubjectSummary(
                subject.getId(),
                subject.getCode(),
                subject.getDepartment().getName(),
                subject.getSubjectKind(),
                subject.getStatus(),
                title.text(),
                title.fallback());
    }

    static SubjectDetail toDetail(Subject subject, SubjectService subjects) {
        List<SubjectTranslationView> translationViews =
                subjects.listTranslations(subject.getId()).stream()
                        .map(SubjectMapper::toTranslationView)
                        .toList();
        List<LearningOutcome> outcomes = subjects.listOutcomes(subject.getId());
        Map<UUID, List<LearningOutcomeTranslation>> rawOutcomeTranslations =
                subjects.listOutcomeTranslations(
                        outcomes.stream().map(LearningOutcome::getId).toList());
        Map<UUID, List<OutcomeTranslationView>> outcomeTranslationsByOutcome =
                rawOutcomeTranslations.entrySet().stream()
                        .collect(
                                java.util.stream.Collectors.toMap(
                                        Map.Entry::getKey,
                                        e ->
                                                e.getValue().stream()
                                                        .map(
                                                                t ->
                                                                        new OutcomeTranslationView(
                                                                                t.getLocale(),
                                                                                t.getStatement()))
                                                        .toList()));
        List<LearningOutcomeView> outcomeViews =
                outcomes.stream()
                        .map(
                                o ->
                                        new LearningOutcomeView(
                                                o.getId(),
                                                o.getCode(),
                                                o.getCategory(),
                                                o.getDisplayOrder(),
                                                outcomeTranslationsByOutcome.getOrDefault(
                                                        o.getId(), List.of())))
                        .toList();
        List<PrerequisiteView> prerequisiteViews =
                subjects.listPrerequisites(subject.getId()).stream()
                        .map(SubjectMapper::toPrerequisiteView)
                        .toList();
        return new SubjectDetail(
                subject.getId(),
                subject.getCode(),
                subject.getDepartment().getId(),
                subject.getDepartment().getName(),
                subject.getSubjectKind(),
                subject.getDefaultInstructionLanguage().code(),
                subject.getStatus(),
                subject.getPublishedAt(),
                translationViews,
                outcomeViews,
                prerequisiteViews);
    }

    private static SubjectTranslationView toTranslationView(SubjectTranslation t) {
        return new SubjectTranslationView(
                t.getLocale(),
                t.getTitle(),
                t.getShortTitle(),
                t.getSummary(),
                t.getDescription(),
                t.getAim(),
                t.getObjectives());
    }

    private static PrerequisiteView toPrerequisiteView(SubjectPrerequisite p) {
        return new PrerequisiteView(
                p.getId(),
                p.getPrerequisiteSubject() == null ? null : p.getPrerequisiteSubject().getId(),
                p.getPrerequisiteSubject() == null ? null : p.getPrerequisiteSubject().getCode(),
                p.getRequirementType(),
                p.getFreeTextNote());
    }
}
