package uz.aifu.lms.academics.app;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.api.dto.SubjectDtos.AddLearningOutcomeRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.AddPrerequisiteRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.CreateSubjectRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.UpdateSubjectRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.UpsertSubjectTranslationRequest;
import uz.aifu.lms.academics.domain.LearningOutcome;
import uz.aifu.lms.academics.domain.LearningOutcomeTranslation;
import uz.aifu.lms.academics.domain.Subject;
import uz.aifu.lms.academics.domain.SubjectPrerequisite;
import uz.aifu.lms.academics.domain.SubjectTranslation;
import uz.aifu.lms.academics.persistence.LearningOutcomeRepository;
import uz.aifu.lms.academics.persistence.LearningOutcomeTranslationRepository;
import uz.aifu.lms.academics.persistence.SubjectPrerequisiteRepository;
import uz.aifu.lms.academics.persistence.SubjectRepository;
import uz.aifu.lms.academics.persistence.SubjectTranslationRepository;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.common.content.TranslationCompletenessValidator;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.iam.app.org.OrgReferenceDataService;
import uz.aifu.lms.iam.domain.Department;

/** "Fan" catalogue administration (docs/domain/domain-model.md §1, Phase 3). */
@Service
public class SubjectService {

    private final SubjectRepository subjects;
    private final SubjectTranslationRepository translations;
    private final LearningOutcomeRepository outcomes;
    private final LearningOutcomeTranslationRepository outcomeTranslations;
    private final SubjectPrerequisiteRepository prerequisites;
    private final OrgReferenceDataService orgReferenceData;
    private final ContentSanitizer sanitizer;
    private final TranslationCompletenessValidator translationValidator;
    private final AuditService audit;
    private final Clock clock;

    public SubjectService(
            SubjectRepository subjects,
            SubjectTranslationRepository translations,
            LearningOutcomeRepository outcomes,
            LearningOutcomeTranslationRepository outcomeTranslations,
            SubjectPrerequisiteRepository prerequisites,
            OrgReferenceDataService orgReferenceData,
            ContentSanitizer sanitizer,
            TranslationCompletenessValidator translationValidator,
            AuditService audit,
            Clock clock) {
        this.subjects = subjects;
        this.translations = translations;
        this.outcomes = outcomes;
        this.outcomeTranslations = outcomeTranslations;
        this.prerequisites = prerequisites;
        this.orgReferenceData = orgReferenceData;
        this.sanitizer = sanitizer;
        this.translationValidator = translationValidator;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<Subject> search(
            String query,
            PublicationStatus status,
            UUID departmentId,
            String locale,
            Pageable pageable) {
        String pattern = normalizePattern(query);
        LocaleCode resolvedLocale = LocaleCode.parseOrDefault(locale);
        return subjects.search(status, departmentId, resolvedLocale.code(), pattern, pageable);
    }

    @Transactional(readOnly = true)
    public Subject require(UUID id) {
        return subjects.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> Exceptions.notFound("Subject", id));
    }

    @Transactional
    public Subject create(CreateSubjectRequest request, UUID actorId, String ip) {
        if (subjects.existsByCodeIgnoreCase(request.code())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "Subject code is already in use");
        }
        Department department = requireDepartment(request.departmentId());
        Subject subject =
                new Subject(
                        request.code(),
                        department,
                        request.subjectKind(),
                        LocaleCode.parseOrDefault(request.defaultInstructionLanguage()));
        Subject saved = subjects.save(subject);
        audit.record(
                AuditAction.SUBJECT_CREATED,
                actorId,
                "subject",
                saved.getId(),
                ip,
                null,
                Map.of("code", saved.getCode()));
        return saved;
    }

    @Transactional
    public Subject update(UUID id, UpdateSubjectRequest request, UUID actorId, String ip) {
        Subject subject = require(id);
        Department department =
                request.departmentId() == null
                        ? subject.getDepartment()
                        : requireDepartment(request.departmentId());
        subject.updateCatalogueFields(
                department,
                request.subjectKind(),
                LocaleCode.find(request.defaultInstructionLanguage()).orElse(null));
        audit.record(AuditAction.SUBJECT_UPDATED, actorId, "subject", id, ip, null);
        return subject;
    }

    @Transactional
    public Subject publish(UUID id, UUID actorId, String ip) {
        Subject subject = require(id);
        translationValidator.requireLocales(
                Set.of(subject.getDefaultInstructionLanguage().code()),
                availableLocales(id),
                "Subject " + subject.getCode());
        subject.publish(clock.instant());
        audit.record(AuditAction.SUBJECT_PUBLISHED, actorId, "subject", id, ip, null);
        return subject;
    }

    @Transactional
    public Subject archive(UUID id, UUID actorId, String ip) {
        Subject subject = require(id);
        subject.archive();
        audit.record(AuditAction.SUBJECT_ARCHIVED, actorId, "subject", id, ip, null);
        return subject;
    }

    @Transactional
    public Subject returnToDraft(UUID id, UUID actorId, String ip) {
        Subject subject = require(id);
        subject.returnToDraft();
        audit.record(
                AuditAction.SUBJECT_UPDATED,
                actorId,
                "subject",
                id,
                ip,
                null,
                Map.of("transition", "to-draft"));
        return subject;
    }

    @Transactional
    public void softDelete(UUID id, UUID actorId, String ip) {
        Subject subject = require(id);
        subject.softDelete(clock.instant());
        audit.record(
                AuditAction.SUBJECT_UPDATED,
                actorId,
                "subject",
                id,
                ip,
                null,
                Map.of("transition", "deleted"));
    }

    // ---------------------------------------------------------------- translations

    @Transactional(readOnly = true)
    public List<SubjectTranslation> listTranslations(UUID subjectId) {
        return translations.findAllBySubjectId(subjectId);
    }

    /**
     * Batched for catalogue lists — controllers must not call the repository directly
     * (skills/architecture/SKILL.md rule 6).
     */
    @Transactional(readOnly = true)
    public Map<UUID, List<SubjectTranslation>> listTranslationsForSubjects(
            java.util.Collection<UUID> subjectIds) {
        if (subjectIds.isEmpty()) {
            return Map.of();
        }
        return translations.findAllBySubjectIds(subjectIds).stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> t.getSubject().getId()));
    }

    @Transactional(readOnly = true)
    public java.util.Map<UUID, List<uz.aifu.lms.academics.domain.LearningOutcomeTranslation>>
            listOutcomeTranslations(java.util.Collection<UUID> outcomeIds) {
        if (outcomeIds.isEmpty()) {
            return Map.of();
        }
        return outcomeTranslations.findAllByLearningOutcomeIds(outcomeIds).stream()
                .collect(
                        java.util.stream.Collectors.groupingBy(
                                t -> t.getLearningOutcome().getId()));
    }

    @Transactional
    public SubjectTranslation upsertTranslation(
            UUID subjectId,
            String locale,
            UpsertSubjectTranslationRequest request,
            UUID actorId,
            String ip) {
        Subject subject = require(subjectId);
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        String title = sanitizer.sanitizePlainText(request.title());
        String shortTitle = sanitizer.sanitizePlainText(request.shortTitle());
        String summary = sanitizer.sanitizeRichText(request.summary());
        String description = sanitizer.sanitizeRichText(request.description());
        String aim = sanitizer.sanitizeRichText(request.aim());
        String objectives = sanitizer.sanitizeRichText(request.objectives());

        SubjectTranslation translation =
                translations
                        .findBySubjectIdAndLocale(subjectId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new SubjectTranslation(
                                                subject, resolvedLocale.code(), title));
        translation.update(title, shortTitle, summary, description, aim, objectives);
        SubjectTranslation saved = translations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "subject",
                subjectId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    // ---------------------------------------------------------------- prerequisites

    @Transactional(readOnly = true)
    public List<SubjectPrerequisite> listPrerequisites(UUID subjectId) {
        return prerequisites.findBySubject_Id(subjectId);
    }

    @Transactional
    public SubjectPrerequisite addPrerequisite(
            UUID subjectId, AddPrerequisiteRequest request, UUID actorId, String ip) {
        Subject subject = require(subjectId);
        Subject prerequisiteSubject =
                request.prerequisiteSubjectId() == null
                        ? null
                        : require(request.prerequisiteSubjectId());
        if (prerequisiteSubject != null
                && prerequisites.existsBySubject_IdAndPrerequisiteSubject_Id(
                        subjectId, prerequisiteSubject.getId())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "This prerequisite is already recorded");
        }
        SubjectPrerequisite prerequisite =
                new SubjectPrerequisite(
                        subject,
                        prerequisiteSubject,
                        request.requirementType(),
                        request.freeTextNote());
        SubjectPrerequisite saved = prerequisites.save(prerequisite);
        audit.record(
                AuditAction.SUBJECT_UPDATED,
                actorId,
                "subject",
                subjectId,
                ip,
                null,
                Map.of("transition", "prerequisite-added"));
        return saved;
    }

    @Transactional
    public void removePrerequisite(UUID subjectId, UUID prerequisiteId, UUID actorId, String ip) {
        SubjectPrerequisite prerequisite =
                prerequisites
                        .findById(prerequisiteId)
                        .orElseThrow(
                                () -> Exceptions.notFound("SubjectPrerequisite", prerequisiteId));
        if (!prerequisite.getSubject().getId().equals(subjectId)) {
            throw Exceptions.notFound("SubjectPrerequisite", prerequisiteId);
        }
        prerequisites.delete(prerequisite);
        audit.record(
                AuditAction.SUBJECT_UPDATED,
                actorId,
                "subject",
                subjectId,
                ip,
                null,
                Map.of("transition", "prerequisite-removed"));
    }

    // ---------------------------------------------------------------- learning outcomes

    @Transactional(readOnly = true)
    public List<LearningOutcome> listOutcomes(UUID subjectId) {
        return outcomes.findAllBySubjectIdOrderByDisplayOrder(subjectId);
    }

    /**
     * Read-only lookup for other modules (e.g. {@code curriculum} linking a lesson to the outcomes
     * it advances) — never inject {@code LearningOutcomeRepository} directly (CLAUDE.md §2).
     */
    @Transactional(readOnly = true)
    public LearningOutcome requireOutcome(UUID outcomeId) {
        return outcomes.findById(outcomeId)
                .orElseThrow(() -> Exceptions.notFound("LearningOutcome", outcomeId));
    }

    @Transactional
    public LearningOutcome addOutcome(
            UUID subjectId, AddLearningOutcomeRequest request, UUID actorId, String ip) {
        Subject subject = require(subjectId);
        if (outcomes.existsBySubject_IdAndCodeIgnoreCase(subjectId, request.code())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "A learning outcome with this code already exists");
        }
        LearningOutcome outcome =
                new LearningOutcome(
                        subject, request.code(), request.category(), request.displayOrder());
        LearningOutcome saved = outcomes.save(outcome);
        audit.record(
                AuditAction.SUBJECT_UPDATED,
                actorId,
                "subject",
                subjectId,
                ip,
                null,
                Map.of("transition", "outcome-added"));
        return saved;
    }

    @Transactional
    public LearningOutcomeTranslation upsertOutcomeTranslation(
            UUID outcomeId, String locale, String statement, UUID actorId, String ip) {
        LearningOutcome outcome =
                outcomes.findById(outcomeId)
                        .orElseThrow(() -> Exceptions.notFound("LearningOutcome", outcomeId));
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        String sanitizedStatement = sanitizer.sanitizePlainText(statement);
        LearningOutcomeTranslation translation =
                outcomeTranslations
                        .findByOutcomeIdAndLocale(outcomeId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new LearningOutcomeTranslation(
                                                outcome,
                                                resolvedLocale.code(),
                                                sanitizedStatement));
        translation.update(sanitizedStatement);
        LearningOutcomeTranslation saved = outcomeTranslations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "learning_outcome",
                outcomeId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    // ---------------------------------------------------------------- helpers

    private Set<String> availableLocales(UUID subjectId) {
        return translations.findAllBySubjectId(subjectId).stream()
                .map(SubjectTranslation::getLocale)
                .collect(java.util.stream.Collectors.toSet());
    }

    private Department requireDepartment(UUID departmentId) {
        return orgReferenceData.requireDepartment(departmentId);
    }

    private static String normalizePattern(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return "%" + query.trim().toLowerCase() + "%";
    }
}
