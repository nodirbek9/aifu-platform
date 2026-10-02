package uz.aifu.lms.curriculum.api;

import java.util.List;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.ActivityDetail;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.ActivitySummary;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.ActivityTranslationView;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.ActivityVariantView;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.curriculum.domain.ActivityTranslation;

/** Shared entity-to-DTO mapping for the activity controllers. */
final class ActivityMapper {

    private ActivityMapper() {}

    static ModuleMapper.TitleResolution resolveTitle(
            List<ActivityTranslation> translations, String requestedLocale, String defaultLocale) {
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

    static ActivitySummary toSummary(
            Activity activity, List<ActivityTranslation> translations, String resolvedLocale) {
        ModuleMapper.TitleResolution title =
                resolveTitle(
                        translations,
                        resolvedLocale,
                        activity.getCourse().getInstructionLanguage().code());
        return new ActivitySummary(
                activity.getId(),
                activity.getCourse().getId(),
                activity.getModule() == null ? null : activity.getModule().getId(),
                activity.getLesson() == null ? null : activity.getLesson().getId(),
                activity.getPosition(),
                activity.getActivityType(),
                activity.getStatus(),
                activity.isGraded(),
                title.text(),
                title.fallback());
    }

    static ActivityDetail toDetail(Activity activity, ActivityService activities) {
        List<ActivityTranslationView> translationViews =
                activities.listTranslations(activity.getId()).stream()
                        .map(
                                t ->
                                        new ActivityTranslationView(
                                                t.getLocale(), t.getTitle(), t.getInstructions()))
                        .toList();
        List<ActivityVariantView> variantViews =
                activities.listVariants(activity.getId()).stream()
                        .map(
                                v ->
                                        new ActivityVariantView(
                                                v.getId(), v.getVariantNo(), v.getReferenceId()))
                        .toList();
        return new ActivityDetail(
                activity.getId(),
                activity.getCourse().getId(),
                activity.getModule() == null ? null : activity.getModule().getId(),
                activity.getLesson() == null ? null : activity.getLesson().getId(),
                activity.getPosition(),
                activity.getActivityType(),
                activity.getReferenceId(),
                activity.getAssessmentComponent() == null
                        ? null
                        : activity.getAssessmentComponent().getId(),
                activity.isGraded(),
                activity.getMaxPoints(),
                activity.getWeight(),
                activity.getAttemptLimit(),
                activity.getCollaborationMode(),
                activity.getVariantStrategy(),
                activity.getAvailableFrom(),
                activity.getDueAt(),
                activity.getStatus(),
                translationViews,
                variantViews);
    }
}
