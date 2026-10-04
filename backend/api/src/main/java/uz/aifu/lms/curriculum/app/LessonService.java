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
import uz.aifu.lms.academics.app.SubjectService;
import uz.aifu.lms.academics.domain.LearningOutcome;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.content.TranslationCompletenessValidator;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.CreateLessonRequest;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.UpdateLessonRequest;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.UpsertLessonTranslationRequest;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.curriculum.domain.LessonLearningOutcome;
import uz.aifu.lms.curriculum.domain.LessonPrerequisite;
import uz.aifu.lms.curriculum.domain.LessonTranslation;
import uz.aifu.lms.curriculum.persistence.LessonLearningOutcomeRepository;
import uz.aifu.lms.curriculum.persistence.LessonPrerequisiteRepository;
import uz.aifu.lms.curriculum.persistence.LessonRepository;
import uz.aifu.lms.curriculum.persistence.LessonTranslationRepository;

/** Lesson administration (docs/domain/domain-model.md §1, Phase 5). */
@Service
public class LessonService {

    private final LessonRepository lessons;
    private final LessonTranslationRepository translations;
    private final LessonLearningOutcomeRepository outcomeLinks;
    private final LessonPrerequisiteRepository prerequisites;
    private final ModuleService modules;
    private final SubjectService subjects;
    private final TranslationCompletenessValidator translationValidator;
    private final AuditService audit;
    private final Clock clock;

    public LessonService(
            LessonRepository lessons,
            LessonTranslationRepository translations,
            LessonLearningOutcomeRepository outcomeLinks,
            LessonPrerequisiteRepository prerequisites,
            ModuleService modules,
            SubjectService subjects,
            TranslationCompletenessValidator translationValidator,
            AuditService audit,
            Clock clock) {
        this.lessons = lessons;
        this.translations = translations;
        this.outcomeLinks = outcomeLinks;
        this.prerequisites = prerequisites;
        this.modules = modules;
        this.subjects = subjects;
        this.translationValidator = translationValidator;
        this.audit = audit;
        this.clock = clock;
    }

    /**
     * Unpaginated — internal use only. List ENDPOINTS use the paginated overloads below (CLAUDE.md
     * §9).
     */
    @Transactional(readOnly = true)
    public List<Lesson> listByModule(UUID moduleId) {
        return lessons.findByModule_IdAndDeletedAtIsNullOrderByPosition(moduleId);
    }

    @Transactional(readOnly = true)
    public List<Lesson> listPublishedByModule(UUID moduleId) {
        return lessons.findByModule_IdAndStatusAndDeletedAtIsNullOrderByPosition(
                moduleId, PublicationStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public Page<Lesson> listByModule(UUID moduleId, Pageable pageable) {
        return lessons.findByModule_IdAndDeletedAtIsNull(moduleId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Lesson> listPublishedByModule(UUID moduleId, Pageable pageable) {
        return lessons.findByModule_IdAndStatusAndDeletedAtIsNull(
                moduleId, PublicationStatus.PUBLISHED, pageable);
    }

    @Transactional(readOnly = true)
    public Lesson require(UUID id) {
        return lessons.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> Exceptions.notFound("Lesson", id));
    }

    @Transactional(readOnly = true)
    public Lesson requirePublished(UUID id) {
        Lesson lesson = require(id);
        if (lesson.getStatus() != PublicationStatus.PUBLISHED) {
            throw Exceptions.notFound("Lesson", id);
        }
        return lesson;
    }

    /**
     * The denominator for module-level progress roll-up ({@code progress} module, Phase 6): every
     * required, published, non-deleted lesson in the module. Read-only — {@code progress} may not
     * import {@link LessonRepository} directly (CLAUDE.md §2).
     */
    @Transactional(readOnly = true)
    public long countRequiredPublished(UUID moduleId) {
        return lessons.countByModule_IdAndRequiredTrueAndStatusAndDeletedAtIsNull(
                moduleId, PublicationStatus.PUBLISHED);
    }

    /** Same, at course level — the denominator for course-level progress roll-up. */
    @Transactional(readOnly = true)
    public long countRequiredPublishedInCourse(UUID courseId) {
        return lessons.countByModule_Course_IdAndRequiredTrueAndStatusAndDeletedAtIsNull(
                courseId, PublicationStatus.PUBLISHED);
    }

    /**
     * A plain {@code UUID}, never the {@code academics.domain.Course} entity itself: {@code
     * progress} may depend on {@code curriculum} but not on {@code academics}
     * (ArchitectureBoundaryTest), so this is how it learns which course a lesson belongs to.
     */
    @Transactional(readOnly = true)
    public UUID courseIdOf(UUID lessonId) {
        return lessons.findCourseIdById(lessonId)
                .orElseThrow(() -> Exceptions.notFound("Lesson", lessonId));
    }

    @Transactional
    public Lesson create(UUID moduleId, CreateLessonRequest request, UUID actorId, String ip) {
        CourseModule module = modules.require(moduleId);
        int position = lessons.nextPosition(moduleId);
        Lesson lesson = new Lesson(module, position, request.code(), request.sessionType());
        Lesson saved = lessons.save(lesson);
        audit.record(
                AuditAction.LESSON_CREATED,
                actorId,
                "lesson",
                saved.getId(),
                ip,
                null,
                Map.of("moduleId", moduleId.toString()));
        return saved;
    }

    @Transactional
    public Lesson updateDetails(UUID id, UpdateLessonRequest request, UUID actorId, String ip) {
        Lesson lesson = require(id);
        lesson.updateDetails(
                request.code(),
                request.sessionType(),
                request.allocatedHours(),
                request.estimatedMinutes(),
                request.difficulty(),
                request.isRequired(),
                request.availableFrom());
        audit.record(AuditAction.LESSON_UPDATED, actorId, "lesson", id, ip, null);
        return lesson;
    }

    /** See {@link ModuleService#reorder} — identical deferred-constraint technique. */
    @Transactional
    public void reorder(UUID moduleId, List<UUID> orderedLessonIds, UUID actorId, String ip) {
        List<Lesson> existing = lessons.findByModule_IdAndDeletedAtIsNullOrderByPosition(moduleId);
        if (existing.size() != orderedLessonIds.size()
                || !existing.stream()
                        .map(Lesson::getId)
                        .collect(Collectors.toSet())
                        .equals(Set.copyOf(orderedLessonIds))) {
            throw Exceptions.validation(
                    "orderedLessonIds must be exactly the module's current, non-deleted lesson ids");
        }
        Map<UUID, Lesson> byId = existing.stream().collect(Collectors.toMap(Lesson::getId, l -> l));
        for (int i = 0; i < orderedLessonIds.size(); i++) {
            byId.get(orderedLessonIds.get(i)).reorderTo(i);
        }
        audit.record(
                AuditAction.LESSON_UPDATED,
                actorId,
                "module",
                moduleId,
                ip,
                null,
                Map.of("transition", "lessons-reordered"));
    }

    @Transactional
    public Lesson publish(UUID id, UUID actorId, String ip) {
        Lesson lesson = require(id);
        translationValidator.requireLocales(
                Set.of(lesson.getModule().getCourse().getRequiredLocales()),
                availableLocales(id),
                "Lesson " + id);
        lesson.publish(clock.instant());
        audit.record(AuditAction.CONTENT_PUBLISHED, actorId, "lesson", id, ip, null);
        return lesson;
    }

    @Transactional
    public Lesson archive(UUID id, UUID actorId, String ip) {
        Lesson lesson = require(id);
        lesson.archive();
        audit.record(
                AuditAction.LESSON_UPDATED,
                actorId,
                "lesson",
                id,
                ip,
                null,
                Map.of("transition", "archived"));
        return lesson;
    }

    @Transactional
    public Lesson returnToDraft(UUID id, UUID actorId, String ip) {
        Lesson lesson = require(id);
        lesson.returnToDraft();
        audit.record(
                AuditAction.LESSON_UPDATED,
                actorId,
                "lesson",
                id,
                ip,
                null,
                Map.of("transition", "returned-to-draft"));
        return lesson;
    }

    @Transactional
    public void softDelete(UUID id, UUID actorId, String ip) {
        Lesson lesson = require(id);
        lesson.softDelete(clock.instant());
        audit.record(AuditAction.LESSON_DELETED, actorId, "lesson", id, ip, null);
    }

    // ---------------------------------------------------------------- translations

    @Transactional(readOnly = true)
    public List<LessonTranslation> listTranslations(UUID lessonId) {
        return translations.findAllByLessonId(lessonId);
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<LessonTranslation>> listTranslationsForLessons(
            Collection<UUID> lessonIds) {
        if (lessonIds.isEmpty()) {
            return Map.of();
        }
        return translations.findAllByLessonIds(lessonIds).stream()
                .collect(Collectors.groupingBy(t -> t.getLesson().getId()));
    }

    @Transactional
    public LessonTranslation upsertTranslation(
            UUID lessonId,
            String locale,
            UpsertLessonTranslationRequest request,
            UUID actorId,
            String ip) {
        Lesson lesson = require(lessonId);
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        LessonTranslation translation =
                translations
                        .findByLessonIdAndLocale(lessonId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new LessonTranslation(
                                                lesson,
                                                resolvedLocale.code(),
                                                request.title(),
                                                request.summary()));
        translation.update(request.title(), request.summary());
        LessonTranslation saved = translations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "lesson",
                lessonId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    // ---------------------------------------------------------------- learning outcomes

    @Transactional(readOnly = true)
    public List<LessonLearningOutcome> listOutcomes(UUID lessonId) {
        return outcomeLinks.findByLesson_Id(lessonId);
    }

    @Transactional
    public void addOutcome(UUID lessonId, UUID outcomeId, UUID actorId, String ip) {
        Lesson lesson = require(lessonId);
        LearningOutcome outcome = subjects.requireOutcome(outcomeId);
        if (outcomeLinks.existsByLesson_IdAndLearningOutcome_Id(lessonId, outcomeId)) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "This outcome is already linked to the lesson");
        }
        outcomeLinks.save(new LessonLearningOutcome(lesson, outcome));
        audit.record(
                AuditAction.LESSON_UPDATED,
                actorId,
                "lesson",
                lessonId,
                ip,
                null,
                Map.of("transition", "outcome-linked"));
    }

    @Transactional
    public void removeOutcome(UUID lessonId, UUID outcomeId, UUID actorId, String ip) {
        outcomeLinks.deleteByLesson_IdAndLearningOutcome_Id(lessonId, outcomeId);
        audit.record(
                AuditAction.LESSON_UPDATED,
                actorId,
                "lesson",
                lessonId,
                ip,
                null,
                Map.of("transition", "outcome-unlinked"));
    }

    // ---------------------------------------------------------------- prerequisites

    @Transactional(readOnly = true)
    public List<LessonPrerequisite> listPrerequisites(UUID lessonId) {
        return prerequisites.findByLesson_Id(lessonId);
    }

    @Transactional
    public void addPrerequisite(UUID lessonId, UUID requiredLessonId, UUID actorId, String ip) {
        Lesson lesson = require(lessonId);
        Lesson requiredLesson = require(requiredLessonId);
        if (prerequisites.existsByLesson_IdAndRequiredLesson_Id(lessonId, requiredLessonId)) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "This prerequisite is already recorded");
        }
        prerequisites.save(new LessonPrerequisite(lesson, requiredLesson));
        audit.record(
                AuditAction.LESSON_UPDATED,
                actorId,
                "lesson",
                lessonId,
                ip,
                null,
                Map.of("transition", "prerequisite-added"));
    }

    @Transactional
    public void removePrerequisite(UUID lessonId, UUID requiredLessonId, UUID actorId, String ip) {
        prerequisites.deleteByLesson_IdAndRequiredLesson_Id(lessonId, requiredLessonId);
        audit.record(
                AuditAction.LESSON_UPDATED,
                actorId,
                "lesson",
                lessonId,
                ip,
                null,
                Map.of("transition", "prerequisite-removed"));
    }

    private Set<String> availableLocales(UUID lessonId) {
        return translations.findAllByLessonId(lessonId).stream()
                .map(LessonTranslation::getLocale)
                .collect(Collectors.toSet());
    }
}
