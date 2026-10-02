package uz.aifu.lms.curriculum.app;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.AssessmentComponent;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.content.TranslationCompletenessValidator;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.CreateActivityRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.UpdateActivityRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.UpsertActivityTranslationRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.UpsertActivityVariantRequest;
import uz.aifu.lms.curriculum.app.activity.ActivityTypeHandler;
import uz.aifu.lms.curriculum.app.activity.ActivityTypeHandlerRegistry;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.curriculum.domain.ActivityTranslation;
import uz.aifu.lms.curriculum.domain.ActivityVariant;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.curriculum.persistence.ActivityRepository;
import uz.aifu.lms.curriculum.persistence.ActivityTranslationRepository;
import uz.aifu.lms.curriculum.persistence.ActivityVariantRepository;

/**
 * Activity administration — "anything a student DOES" (docs/domain/domain-model.md §1, Phase 5).
 * {@code activities} has neither a position-uniqueness constraint nor {@code deleted_at} (V007):
 * reorder is a plain field update (no deferred-constraint dance needed) and removal is always
 * {@link #archive} — real deletion is forbidden (CLAUDE.md §5 default) since a future grading
 * record may reference an activity by id.
 */
@Service
public class ActivityService {

    private final ActivityRepository activities;
    private final ActivityTranslationRepository translations;
    private final ActivityVariantRepository variants;
    private final CourseService courses;
    private final ModuleService modules;
    private final LessonService lessons;
    private final ActivityTypeHandlerRegistry activityTypeHandlers;
    private final TranslationCompletenessValidator translationValidator;
    private final AuditService audit;
    private final Clock clock;

    public ActivityService(
            ActivityRepository activities,
            ActivityTranslationRepository translations,
            ActivityVariantRepository variants,
            CourseService courses,
            ModuleService modules,
            LessonService lessons,
            ActivityTypeHandlerRegistry activityTypeHandlers,
            TranslationCompletenessValidator translationValidator,
            AuditService audit,
            Clock clock) {
        this.activities = activities;
        this.translations = translations;
        this.variants = variants;
        this.courses = courses;
        this.modules = modules;
        this.lessons = lessons;
        this.activityTypeHandlers = activityTypeHandlers;
        this.translationValidator = translationValidator;
        this.audit = audit;
        this.clock = clock;
    }

    /**
     * Unpaginated — internal use only. The teacher-facing activity LIST endpoint uses the paginated
     * overload below (CLAUDE.md §9).
     */
    @Transactional(readOnly = true)
    public List<Activity> listByCourse(UUID courseId) {
        return activities.findByCourse_IdOrderByPosition(courseId);
    }

    @Transactional(readOnly = true)
    public Page<Activity> listByCourse(UUID courseId, Pageable pageable) {
        return activities.findByCourse_Id(courseId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Activity> listByModule(UUID moduleId) {
        return activities.findByModule_IdOrderByPosition(moduleId);
    }

    @Transactional(readOnly = true)
    public List<Activity> listByLesson(UUID lessonId) {
        return activities.findByLesson_IdOrderByPosition(lessonId);
    }

    @Transactional(readOnly = true)
    public Activity require(UUID id) {
        return activities
                .findWithDetailsById(id)
                .orElseThrow(() -> Exceptions.notFound("Activity", id));
    }

    @Transactional(readOnly = true)
    public Activity requirePublished(UUID id) {
        Activity activity = require(id);
        if (activity.getStatus() != PublicationStatus.PUBLISHED) {
            throw Exceptions.notFound("Activity", id);
        }
        return activity;
    }

    /**
     * The denominator for course-level progress roll-up ({@code progress} module, Phase 6): every
     * published activity in the course. Read-only — {@code progress} may not import {@link
     * ActivityRepository} directly (CLAUDE.md §2).
     */
    @Transactional(readOnly = true)
    public long countPublished(UUID courseId) {
        return activities.countByCourse_IdAndStatus(courseId, PublicationStatus.PUBLISHED);
    }

    @Transactional
    public Activity create(UUID courseId, CreateActivityRequest request, UUID actorId, String ip) {
        Course course = courses.require(courseId);
        CourseModule module =
                request.moduleId() == null ? null : modules.require(request.moduleId());
        Lesson lesson = request.lessonId() == null ? null : lessons.require(request.lessonId());
        AssessmentComponent component =
                request.assessmentComponentId() == null
                        ? null
                        : courses.requireAssessmentComponent(request.assessmentComponentId());
        int position =
                (int)
                        (lesson != null
                                ? activities.countByLesson_Id(lesson.getId())
                                : activities.countByModule_Id(module.getId()));
        Activity activity =
                new Activity(
                        course,
                        module,
                        lesson,
                        request.activityType(),
                        position,
                        request.isGraded(),
                        component,
                        request.maxPoints());
        Activity saved = activities.save(activity);
        audit.record(
                AuditAction.ACTIVITY_CREATED,
                actorId,
                "activity",
                saved.getId(),
                ip,
                null,
                Map.of("courseId", courseId.toString()));
        return saved;
    }

    @Transactional
    public Activity updateDetails(UUID id, UpdateActivityRequest request, UUID actorId, String ip) {
        Activity activity = require(id);
        ActivityTypeHandler handler = activityTypeHandlers.forType(activity.getActivityType());
        if (handler.requiresReference() && request.referenceId() == null) {
            throw Exceptions.validation(
                    activity.getActivityType() + " activities require a referenceId");
        }
        activity.updateDetails(
                request.referenceId(),
                request.weight(),
                request.attemptLimit(),
                request.collaborationMode(),
                request.variantStrategy(),
                request.availableFrom(),
                request.dueAt());
        audit.record(AuditAction.ACTIVITY_UPDATED, actorId, "activity", id, ip, null);
        return activity;
    }

    /**
     * Plain field update — {@code activities.position} has no uniqueness constraint, unlike
     * modules/lessons/blocks, so no deferred-constraint technique is needed here.
     */
    @Transactional
    public void reorderWithinModule(
            UUID moduleId, List<UUID> orderedActivityIds, UUID actorId, String ip) {
        reorder(
                activities.findByModule_IdOrderByPosition(moduleId),
                orderedActivityIds,
                "module",
                moduleId,
                actorId,
                ip);
    }

    @Transactional
    public void reorderWithinLesson(
            UUID lessonId, List<UUID> orderedActivityIds, UUID actorId, String ip) {
        reorder(
                activities.findByLesson_IdOrderByPosition(lessonId),
                orderedActivityIds,
                "lesson",
                lessonId,
                actorId,
                ip);
    }

    private void reorder(
            List<Activity> existing,
            List<UUID> orderedIds,
            String parentType,
            UUID parentId,
            UUID actorId,
            String ip) {
        if (existing.size() != orderedIds.size()
                || !existing.stream()
                        .map(Activity::getId)
                        .collect(Collectors.toSet())
                        .equals(Set.copyOf(orderedIds))) {
            throw Exceptions.validation(
                    "orderedActivityIds must be exactly the "
                            + parentType
                            + "'s current activity ids");
        }
        Map<UUID, Activity> byId =
                existing.stream().collect(Collectors.toMap(Activity::getId, a -> a));
        for (int i = 0; i < orderedIds.size(); i++) {
            byId.get(orderedIds.get(i)).reorderTo(i);
        }
        audit.record(
                AuditAction.ACTIVITY_UPDATED,
                actorId,
                parentType,
                parentId,
                ip,
                null,
                Map.of("transition", "activities-reordered"));
    }

    @Transactional
    public Activity publish(UUID id, UUID actorId, String ip) {
        Activity activity = require(id);
        translationValidator.requireLocales(
                Set.of(activity.getCourse().getRequiredLocales()),
                availableLocales(id),
                "Activity " + id);
        activity.publish(clock.instant());
        audit.record(AuditAction.CONTENT_PUBLISHED, actorId, "activity", id, ip, null);
        return activity;
    }

    @Transactional
    public Activity archive(UUID id, UUID actorId, String ip) {
        Activity activity = require(id);
        activity.archive();
        audit.record(
                AuditAction.ACTIVITY_UPDATED,
                actorId,
                "activity",
                id,
                ip,
                null,
                Map.of("transition", "archived"));
        return activity;
    }

    @Transactional
    public Activity returnToDraft(UUID id, UUID actorId, String ip) {
        Activity activity = require(id);
        activity.returnToDraft();
        audit.record(
                AuditAction.ACTIVITY_UPDATED,
                actorId,
                "activity",
                id,
                ip,
                null,
                Map.of("transition", "returned-to-draft"));
        return activity;
    }

    // ---------------------------------------------------------------- translations

    @Transactional(readOnly = true)
    public List<ActivityTranslation> listTranslations(UUID activityId) {
        return translations.findAllByActivityId(activityId);
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<ActivityTranslation>> listTranslationsForActivities(
            Collection<UUID> activityIds) {
        if (activityIds.isEmpty()) {
            return Map.of();
        }
        return translations.findAllByActivityIds(activityIds).stream()
                .collect(Collectors.groupingBy(t -> t.getActivity().getId()));
    }

    @Transactional
    public ActivityTranslation upsertTranslation(
            UUID activityId,
            String locale,
            UpsertActivityTranslationRequest request,
            UUID actorId,
            String ip) {
        Activity activity = require(activityId);
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        ActivityTranslation translation =
                translations
                        .findByActivityIdAndLocale(activityId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new ActivityTranslation(
                                                activity,
                                                resolvedLocale.code(),
                                                request.title(),
                                                request.instructions()));
        translation.update(request.title(), request.instructions());
        ActivityTranslation saved = translations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "activity",
                activityId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    // ---------------------------------------------------------------- variants

    @Transactional(readOnly = true)
    public List<ActivityVariant> listVariants(UUID activityId) {
        return variants.findByActivity_IdOrderByVariantNo(activityId);
    }

    @Transactional
    public ActivityVariant upsertVariant(
            UUID activityId,
            int variantNo,
            UpsertActivityVariantRequest request,
            UUID actorId,
            String ip) {
        Activity activity = require(activityId);
        String payloadJson = request.payload() == null ? "{}" : request.payload().toString();
        ActivityVariant variant =
                variants.findByActivity_IdAndVariantNo(activityId, variantNo)
                        .orElseGet(
                                () ->
                                        new ActivityVariant(
                                                activity,
                                                variantNo,
                                                request.referenceId(),
                                                payloadJson));
        variant.update(request.referenceId(), payloadJson);
        ActivityVariant saved = variants.save(variant);
        audit.record(
                AuditAction.ACTIVITY_UPDATED,
                actorId,
                "activity",
                activityId,
                ip,
                null,
                Map.of("transition", "variant-upserted"));
        return saved;
    }

    private Set<String> availableLocales(UUID activityId) {
        return translations.findAllByActivityId(activityId).stream()
                .map(ActivityTranslation::getLocale)
                .collect(Collectors.toSet());
    }
}
