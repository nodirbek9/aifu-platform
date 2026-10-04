package uz.aifu.lms.curriculum.app;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.content.TranslationCompletenessValidator;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.CreateModuleRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.UpdateModuleRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.UpsertModuleTranslationRequest;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.curriculum.domain.CourseModuleTranslation;
import uz.aifu.lms.curriculum.persistence.CourseModuleRepository;
import uz.aifu.lms.curriculum.persistence.CourseModuleTranslationRepository;

/** Course module administration (docs/domain/domain-model.md §1, Phase 5). */
@Service
public class ModuleService {

    private final CourseModuleRepository modules;
    private final CourseModuleTranslationRepository translations;
    private final CourseService courses;
    private final TranslationCompletenessValidator translationValidator;
    private final AuditService audit;
    private final Clock clock;

    public ModuleService(
            CourseModuleRepository modules,
            CourseModuleTranslationRepository translations,
            CourseService courses,
            TranslationCompletenessValidator translationValidator,
            AuditService audit,
            Clock clock) {
        this.modules = modules;
        this.translations = translations;
        this.courses = courses;
        this.translationValidator = translationValidator;
        this.audit = audit;
        this.clock = clock;
    }

    /**
     * Unpaginated — internal use only (translation batching etc). List ENDPOINTS use the paginated
     * overloads below (CLAUDE.md §9).
     */
    @Transactional(readOnly = true)
    public List<CourseModule> listByCourse(UUID courseId) {
        return modules.findByCourse_IdAndDeletedAtIsNullOrderByPosition(courseId);
    }

    @Transactional(readOnly = true)
    public List<CourseModule> listPublishedByCourse(UUID courseId) {
        return modules.findByCourse_IdAndStatusAndDeletedAtIsNullOrderByPosition(
                courseId, PublicationStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public Page<CourseModule> listByCourse(UUID courseId, Pageable pageable) {
        return modules.findByCourse_IdAndDeletedAtIsNull(courseId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CourseModule> listPublishedByCourse(UUID courseId, Pageable pageable) {
        return modules.findByCourse_IdAndStatusAndDeletedAtIsNull(
                courseId, PublicationStatus.PUBLISHED, pageable);
    }

    @Transactional(readOnly = true)
    public CourseModule require(UUID id) {
        return modules.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> Exceptions.notFound("Module", id));
    }

    /**
     * Same as {@link #require} but additionally invisible unless PUBLISHED — the student-facing
     * path.
     */
    @Transactional(readOnly = true)
    public CourseModule requirePublished(UUID id) {
        CourseModule module = require(id);
        if (module.getStatus() != PublicationStatus.PUBLISHED) {
            throw Exceptions.notFound("Module", id);
        }
        return module;
    }

    @Transactional
    public CourseModule create(
            UUID courseId, CreateModuleRequest request, UUID actorId, String ip) {
        Course course = courses.require(courseId);
        int position = modules.nextPosition(courseId);
        CourseModule module =
                new CourseModule(course, position, request.code(), request.groupingSource());
        CourseModule saved = modules.save(module);
        audit.record(
                AuditAction.MODULE_CREATED,
                actorId,
                "module",
                saved.getId(),
                ip,
                null,
                Map.of("courseId", courseId.toString()));
        return saved;
    }

    @Transactional
    public CourseModule updateDetails(
            UUID id, UpdateModuleRequest request, UUID actorId, String ip) {
        CourseModule module = require(id);
        module.updateDetails(
                request.code(),
                request.isRequired(),
                request.estimatedMinutes(),
                request.availableFrom(),
                request.groupingSource());
        audit.record(AuditAction.MODULE_UPDATED, actorId, "module", id, ip, null);
        return module;
    }

    /**
     * Reassigns positions 0..n-1 in the given order. Relies on {@code uq_module_position}'s {@code
     * DEFERRABLE INITIALLY DEFERRED} to allow intermediate "collisions" within this one transaction
     * — the constraint is only checked at commit.
     */
    @Transactional
    public void reorder(UUID courseId, List<UUID> orderedModuleIds, UUID actorId, String ip) {
        List<CourseModule> existing =
                modules.findByCourse_IdAndDeletedAtIsNullOrderByPosition(courseId);
        if (existing.size() != orderedModuleIds.size()
                || !existing.stream()
                        .map(CourseModule::getId)
                        .collect(java.util.stream.Collectors.toSet())
                        .equals(java.util.Set.copyOf(orderedModuleIds))) {
            throw Exceptions.validation(
                    "orderedModuleIds must be exactly the course's current, non-deleted module ids");
        }
        Map<UUID, CourseModule> byId =
                existing.stream()
                        .collect(java.util.stream.Collectors.toMap(CourseModule::getId, m -> m));
        for (int i = 0; i < orderedModuleIds.size(); i++) {
            byId.get(orderedModuleIds.get(i)).reorderTo(i);
        }
        audit.record(
                AuditAction.MODULE_UPDATED,
                actorId,
                "course",
                courseId,
                ip,
                null,
                Map.of("transition", "modules-reordered"));
    }

    @Transactional
    public CourseModule publish(UUID id, UUID actorId, String ip) {
        CourseModule module = require(id);
        translationValidator.requireLocales(
                java.util.Set.of(module.getCourse().getRequiredLocales()),
                availableLocales(id),
                "Module " + id);
        module.publish(clock.instant());
        audit.record(AuditAction.CONTENT_PUBLISHED, actorId, "module", id, ip, null);
        return module;
    }

    @Transactional
    public CourseModule archive(UUID id, UUID actorId, String ip) {
        CourseModule module = require(id);
        module.archive();
        audit.record(
                AuditAction.MODULE_UPDATED,
                actorId,
                "module",
                id,
                ip,
                null,
                Map.of("transition", "archived"));
        return module;
    }

    @Transactional
    public CourseModule returnToDraft(UUID id, UUID actorId, String ip) {
        CourseModule module = require(id);
        module.returnToDraft();
        audit.record(
                AuditAction.MODULE_UPDATED,
                actorId,
                "module",
                id,
                ip,
                null,
                Map.of("transition", "returned-to-draft"));
        return module;
    }

    @Transactional
    public void softDelete(UUID id, UUID actorId, String ip) {
        CourseModule module = require(id);
        module.softDelete(clock.instant());
        audit.record(AuditAction.MODULE_DELETED, actorId, "module", id, ip, null);
    }

    // ---------------------------------------------------------------- translations

    @Transactional(readOnly = true)
    public List<CourseModuleTranslation> listTranslations(UUID moduleId) {
        return translations.findAllByModuleId(moduleId);
    }

    /**
     * Batched for module-list rendering — controllers must not call the repository directly
     * (skills/architecture/SKILL.md rule 6).
     */
    @Transactional(readOnly = true)
    public Map<UUID, List<CourseModuleTranslation>> listTranslationsForModules(
            Collection<UUID> moduleIds) {
        if (moduleIds.isEmpty()) {
            return Map.of();
        }
        return translations.findAllByModuleIds(moduleIds).stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> t.getModule().getId()));
    }

    @Transactional
    public CourseModuleTranslation upsertTranslation(
            UUID moduleId,
            String locale,
            UpsertModuleTranslationRequest request,
            UUID actorId,
            String ip) {
        CourseModule module = require(moduleId);
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        CourseModuleTranslation translation =
                translations
                        .findByModuleIdAndLocale(moduleId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new CourseModuleTranslation(
                                                module,
                                                resolvedLocale.code(),
                                                request.title(),
                                                request.description()));
        translation.update(request.title(), request.description());
        CourseModuleTranslation saved = translations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "module",
                moduleId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    private java.util.Set<String> availableLocales(UUID moduleId) {
        return translations.findAllByModuleId(moduleId).stream()
                .map(CourseModuleTranslation::getLocale)
                .collect(java.util.stream.Collectors.toSet());
    }
}
