package uz.aifu.lms.coding.app;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CreateHintRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CreateTestCaseRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpdateTestCaseRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpsertHintTranslationRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpsertLanguageRequest;
import uz.aifu.lms.coding.domain.CodingTask;
import uz.aifu.lms.coding.domain.CodingTaskHint;
import uz.aifu.lms.coding.domain.CodingTaskHintTranslation;
import uz.aifu.lms.coding.domain.CodingTaskLanguage;
import uz.aifu.lms.coding.domain.CodingTaskTestCase;
import uz.aifu.lms.coding.domain.ProgrammingLanguage;
import uz.aifu.lms.coding.persistence.CodingTaskHintRepository;
import uz.aifu.lms.coding.persistence.CodingTaskHintTranslationRepository;
import uz.aifu.lms.coding.persistence.CodingTaskLanguageRepository;
import uz.aifu.lms.coding.persistence.CodingTaskTestCaseRepository;
import uz.aifu.lms.coding.persistence.ProgrammingLanguageRepository;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;

/**
 * Languages, hints and test cases for a {@link CodingTask} — bundled in one service the same way
 * {@code QuestionService} bundles answer options and answer keys, rather than one service per child
 * table.
 */
@Service
public class CodingTaskContentService {

    private final CodingTaskService codingTasks;
    private final ProgrammingLanguageRepository languages;
    private final CodingTaskLanguageRepository taskLanguages;
    private final CodingTaskHintRepository hints;
    private final CodingTaskHintTranslationRepository hintTranslations;
    private final CodingTaskTestCaseRepository testCases;
    private final AuditService audit;

    public CodingTaskContentService(
            CodingTaskService codingTasks,
            ProgrammingLanguageRepository languages,
            CodingTaskLanguageRepository taskLanguages,
            CodingTaskHintRepository hints,
            CodingTaskHintTranslationRepository hintTranslations,
            CodingTaskTestCaseRepository testCases,
            AuditService audit) {
        this.codingTasks = codingTasks;
        this.languages = languages;
        this.taskLanguages = taskLanguages;
        this.hints = hints;
        this.hintTranslations = hintTranslations;
        this.testCases = testCases;
        this.audit = audit;
    }

    // ---------------------------------------------------------------- languages

    @Transactional(readOnly = true)
    public List<CodingTaskLanguage> listLanguages(UUID codingTaskId) {
        return taskLanguages.findByCodingTask_Id(codingTaskId);
    }

    /**
     * Resolves the owning {@link CodingTask}'s id for a child row by its own id — a plain scalar
     * query, not the entity, so the controller can run its {@code AccessGuard} ownership check
     * BEFORE mutating and never needs to touch a lazy association after the transactional call
     * returns ({@code spring.jpa.open-in-view: false} means that throws {@code
     * LazyInitializationException} — found by {@code CodingSubmissionIT}, Phase 8).
     */
    @Transactional(readOnly = true)
    public UUID codingTaskIdOfLanguage(UUID codingTaskLanguageId) {
        return taskLanguages
                .findCodingTaskIdById(codingTaskLanguageId)
                .orElseThrow(() -> Exceptions.notFound("CodingTaskLanguage", codingTaskLanguageId));
    }

    @Transactional
    public CodingTaskLanguage addLanguage(
            UUID codingTaskId,
            String languageCode,
            UpsertLanguageRequest request,
            UUID actorId,
            String ip) {
        CodingTask task = codingTasks.require(codingTaskId);
        ProgrammingLanguage language =
                languages
                        .findById(languageCode)
                        .filter(ProgrammingLanguage::isEnabled)
                        .orElseThrow(
                                () ->
                                        Exceptions.rule(
                                                ErrorCode.LANGUAGE_NOT_ENABLED,
                                                "Language " + languageCode + " is not enabled"));
        if (taskLanguages
                .findByCodingTask_IdAndLanguage_Code(codingTaskId, languageCode)
                .isPresent()) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "This language is already attached to the task");
        }
        CodingTaskLanguage saved =
                taskLanguages.save(
                        new CodingTaskLanguage(
                                task, language, request.starterCode(), request.solutionCode()));
        audit.record(
                AuditAction.CODING_TASK_UPDATED,
                actorId,
                "coding_task",
                codingTaskId,
                ip,
                null,
                Map.of("transition", "language-added", "language", languageCode));
        return saved;
    }

    @Transactional
    public CodingTaskLanguage updateLanguage(
            UUID codingTaskLanguageId, UpsertLanguageRequest request, UUID actorId, String ip) {
        CodingTaskLanguage entry = requireLanguage(codingTaskLanguageId);
        entry.update(request.starterCode(), request.solutionCode());
        audit.record(
                AuditAction.CODING_TASK_UPDATED,
                actorId,
                "coding_task",
                entry.getCodingTask().getId(),
                ip,
                null,
                Map.of("transition", "language-updated"));
        return entry;
    }

    @Transactional
    public void removeLanguage(UUID codingTaskLanguageId, UUID actorId, String ip) {
        CodingTaskLanguage entry = requireLanguage(codingTaskLanguageId);
        UUID taskId = entry.getCodingTask().getId();
        taskLanguages.delete(entry);
        audit.record(
                AuditAction.CODING_TASK_UPDATED,
                actorId,
                "coding_task",
                taskId,
                ip,
                null,
                Map.of("transition", "language-removed"));
    }

    private CodingTaskLanguage requireLanguage(UUID id) {
        return taskLanguages
                .findById(id)
                .orElseThrow(() -> Exceptions.notFound("CodingTaskLanguage", id));
    }

    // ---------------------------------------------------------------- hints

    @Transactional(readOnly = true)
    public List<CodingTaskHint> listHints(UUID codingTaskId) {
        return hints.findByCodingTask_IdOrderByPosition(codingTaskId);
    }

    /** See {@link #codingTaskIdOfLanguage}'s javadoc. */
    @Transactional(readOnly = true)
    public UUID codingTaskIdOfHint(UUID hintId) {
        return hints.findCodingTaskIdById(hintId)
                .orElseThrow(() -> Exceptions.notFound("CodingTaskHint", hintId));
    }

    @Transactional
    public CodingTaskHint addHint(
            UUID codingTaskId, CreateHintRequest request, UUID actorId, String ip) {
        CodingTask task = codingTasks.require(codingTaskId);
        int position = hints.nextPosition(codingTaskId);
        CodingTaskHint saved =
                hints.save(new CodingTaskHint(task, position, request.penaltyPercent()));
        audit.record(
                AuditAction.CODING_TASK_UPDATED,
                actorId,
                "coding_task",
                codingTaskId,
                ip,
                null,
                Map.of("transition", "hint-added"));
        return saved;
    }

    @Transactional
    public void removeHint(UUID hintId, UUID actorId, String ip) {
        CodingTaskHint hint = requireHint(hintId);
        UUID taskId = hint.getCodingTask().getId();
        hints.delete(hint);
        audit.record(
                AuditAction.CODING_TASK_UPDATED,
                actorId,
                "coding_task",
                taskId,
                ip,
                null,
                Map.of("transition", "hint-removed"));
    }

    @Transactional(readOnly = true)
    public List<CodingTaskHintTranslation> listHintTranslations(UUID hintId) {
        return hintTranslations.findAllByHintId(hintId);
    }

    @Transactional
    public CodingTaskHintTranslation upsertHintTranslation(
            UUID hintId,
            String locale,
            UpsertHintTranslationRequest request,
            UUID actorId,
            String ip) {
        CodingTaskHint hint = requireHint(hintId);
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        CodingTaskHintTranslation translation =
                hintTranslations
                        .findByHintIdAndLocale(hintId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new CodingTaskHintTranslation(
                                                hint, resolvedLocale.code(), request.body()));
        translation.update(request.body());
        CodingTaskHintTranslation saved = hintTranslations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "coding_task_hint",
                hintId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    private CodingTaskHint requireHint(UUID id) {
        return hints.findById(id).orElseThrow(() -> Exceptions.notFound("CodingTaskHint", id));
    }

    // ---------------------------------------------------------------- test cases

    @Transactional(readOnly = true)
    public List<CodingTaskTestCase> listTestCases(UUID codingTaskId) {
        return testCases.findByCodingTask_IdOrderByPosition(codingTaskId);
    }

    /** See {@link #codingTaskIdOfLanguage}'s javadoc. */
    @Transactional(readOnly = true)
    public UUID codingTaskIdOfTestCase(UUID testCaseId) {
        return testCases
                .findCodingTaskIdById(testCaseId)
                .orElseThrow(() -> Exceptions.notFound("CodingTaskTestCase", testCaseId));
    }

    @Transactional
    public CodingTaskTestCase addTestCase(
            UUID codingTaskId, CreateTestCaseRequest request, UUID actorId, String ip) {
        CodingTask task = codingTasks.require(codingTaskId);
        int position = testCases.nextPosition(codingTaskId);
        CodingTaskTestCase saved =
                testCases.save(
                        new CodingTaskTestCase(
                                task, position, request.stdin(), request.expectedStdout()));
        audit.record(
                AuditAction.TEST_CASE_CREATED,
                actorId,
                "coding_task",
                codingTaskId,
                ip,
                null,
                Map.of("testCaseId", saved.getId().toString()));
        return saved;
    }

    @Transactional
    public CodingTaskTestCase updateTestCase(
            UUID testCaseId, UpdateTestCaseRequest request, UUID actorId, String ip) {
        CodingTaskTestCase testCase = requireTestCase(testCaseId);
        testCase.update(
                request.name(),
                request.visibility(),
                request.isExample(),
                request.weight(),
                request.stdin(),
                request.expectedStdout(),
                request.comparisonMode(),
                request.numericTolerance(),
                request.cpuTimeLimitMs());
        audit.record(
                AuditAction.TEST_CASE_UPDATED,
                actorId,
                "coding_task",
                testCase.getCodingTask().getId(),
                ip,
                null);
        return testCase;
    }

    @Transactional
    public void removeTestCase(UUID testCaseId, UUID actorId, String ip) {
        CodingTaskTestCase testCase = requireTestCase(testCaseId);
        UUID taskId = testCase.getCodingTask().getId();
        testCases.delete(testCase);
        audit.record(AuditAction.TEST_CASE_DELETED, actorId, "coding_task", taskId, ip, null);
    }

    private CodingTaskTestCase requireTestCase(UUID id) {
        return testCases
                .findById(id)
                .orElseThrow(() -> Exceptions.notFound("CodingTaskTestCase", id));
    }
}
