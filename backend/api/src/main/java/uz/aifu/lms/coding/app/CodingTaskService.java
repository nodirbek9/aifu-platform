package uz.aifu.lms.coding.app;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CreateCodingTaskRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpdateCodingTaskRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpsertCodingTaskTranslationRequest;
import uz.aifu.lms.coding.domain.CodingTask;
import uz.aifu.lms.coding.domain.CodingTaskTranslation;
import uz.aifu.lms.coding.persistence.CodingTaskRepository;
import uz.aifu.lms.coding.persistence.CodingTaskTestCaseRepository;
import uz.aifu.lms.coding.persistence.CodingTaskTranslationRepository;
import uz.aifu.lms.common.content.TranslationCompletenessValidator;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.curriculum.domain.ActivityType;

/** Coding task administration (docs/planning/phase-plan.md Phase 8). */
@Service
public class CodingTaskService {

    private final CodingTaskRepository codingTasks;
    private final CodingTaskTranslationRepository translations;
    private final CodingTaskTestCaseRepository testCases;
    private final CourseService courses;
    private final TranslationCompletenessValidator translationValidator;
    private final AuditService audit;
    private final Clock clock;
    private final Optional<ActivityService> activities;

    public CodingTaskService(
            CodingTaskRepository codingTasks,
            CodingTaskTranslationRepository translations,
            CodingTaskTestCaseRepository testCases,
            CourseService courses,
            TranslationCompletenessValidator translationValidator,
            AuditService audit,
            Clock clock,
            Optional<ActivityService> activities) {
        this.codingTasks = codingTasks;
        this.translations = translations;
        this.testCases = testCases;
        this.courses = courses;
        this.translationValidator = translationValidator;
        this.audit = audit;
        this.clock = clock;
        this.activities = activities;
    }

    @Transactional(readOnly = true)
    public Optional<UUID> findPublishedActivityId(UUID codingTaskId) {
        return activities
                .flatMap(a -> a.findSolePublishedFor(ActivityType.CODING_TASK, codingTaskId))
                .map(Activity::getId);
    }

    @Transactional(readOnly = true)
    public CodingTask require(UUID id) {
        return codingTasks
                .findWithCourseById(id)
                .orElseThrow(() -> Exceptions.notFound("CodingTask", id));
    }

    @Transactional(readOnly = true)
    public CodingTask requirePublished(UUID id) {
        CodingTask task = require(id);
        if (task.getStatus() != PublicationStatus.PUBLISHED) {
            throw Exceptions.notFound("CodingTask", id);
        }
        return task;
    }

    @Transactional(readOnly = true)
    public Page<CodingTask> listByCourse(UUID courseId, Pageable pageable) {
        return codingTasks.findByCourse_Id(courseId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CodingTask> listPublishedByCourse(UUID courseId, Pageable pageable) {
        return codingTasks.findByCourse_IdAndStatus(
                courseId, PublicationStatus.PUBLISHED, pageable);
    }

    @Transactional
    public CodingTask create(
            UUID courseId, CreateCodingTaskRequest request, UUID actorId, String ip) {
        Course course = courses.require(courseId);
        CodingTask task = new CodingTask(course, request.code());
        CodingTask saved = codingTasks.save(task);
        audit.record(
                AuditAction.CODING_TASK_CREATED,
                actorId,
                "coding_task",
                saved.getId(),
                ip,
                null,
                Map.of("courseId", courseId.toString()));
        return saved;
    }

    @Transactional
    public CodingTask updateDetails(
            UUID id, UpdateCodingTaskRequest request, UUID actorId, String ip) {
        CodingTask task = require(id);
        task.updateDetails(
                request.difficulty(),
                request.maxPoints(),
                request.scoringStrategy(),
                request.maxAttempts(),
                request.cpuTimeLimitMs(),
                request.wallTimeLimitMs(),
                request.memoryLimitKb());
        audit.record(AuditAction.CODING_TASK_UPDATED, actorId, "coding_task", id, ip, null);
        return task;
    }

    @Transactional
    public CodingTask publish(UUID id, UUID actorId, String ip) {
        CodingTask task = require(id);
        if (testCases.countByCodingTask_Id(id) == 0) {
            throw Exceptions.rule(
                    ErrorCode.PUBLICATION_VALIDATION_FAILED,
                    "A coding task needs at least one test case to publish");
        }
        translationValidator.requireLocales(
                Set.of(task.getCourse().getRequiredLocales()),
                availableLocales(id),
                "CodingTask " + id);
        task.publish(clock.instant());
        audit.record(AuditAction.CODING_TASK_PUBLISHED, actorId, "coding_task", id, ip, null);
        return task;
    }

    @Transactional
    public CodingTask archive(UUID id, UUID actorId, String ip) {
        CodingTask task = require(id);
        task.archive();
        audit.record(
                AuditAction.CODING_TASK_UPDATED,
                actorId,
                "coding_task",
                id,
                ip,
                null,
                Map.of("transition", "archived"));
        return task;
    }

    @Transactional
    public CodingTask returnToDraft(UUID id, UUID actorId, String ip) {
        CodingTask task = require(id);
        task.returnToDraft();
        audit.record(
                AuditAction.CODING_TASK_UPDATED,
                actorId,
                "coding_task",
                id,
                ip,
                null,
                Map.of("transition", "returned-to-draft"));
        return task;
    }

    // ---------------------------------------------------------------- translations

    @Transactional(readOnly = true)
    public List<CodingTaskTranslation> listTranslations(UUID codingTaskId) {
        return translations.findAllByCodingTaskId(codingTaskId);
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<CodingTaskTranslation>> listTranslationsForTasks(
            Collection<UUID> codingTaskIds) {
        if (codingTaskIds.isEmpty()) {
            return Map.of();
        }
        return translations.findAllByCodingTaskIds(codingTaskIds).stream()
                .collect(Collectors.groupingBy(t -> t.getCodingTask().getId()));
    }

    @Transactional
    public CodingTaskTranslation upsertTranslation(
            UUID codingTaskId,
            String locale,
            UpsertCodingTaskTranslationRequest request,
            UUID actorId,
            String ip) {
        CodingTask task = require(codingTaskId);
        LocaleCode resolvedLocale = LocaleCode.require(locale);
        CodingTaskTranslation translation =
                translations
                        .findByCodingTaskIdAndLocale(codingTaskId, resolvedLocale.code())
                        .orElseGet(
                                () ->
                                        new CodingTaskTranslation(
                                                task,
                                                resolvedLocale.code(),
                                                request.title(),
                                                request.description(),
                                                request.instructions(),
                                                request.constraintsText(),
                                                request.explanation()));
        translation.update(
                request.title(),
                request.description(),
                request.instructions(),
                request.constraintsText(),
                request.explanation());
        CodingTaskTranslation saved = translations.save(translation);
        audit.record(
                AuditAction.TRANSLATION_CHANGED,
                actorId,
                "coding_task",
                codingTaskId,
                ip,
                null,
                Map.of("locale", resolvedLocale.code()));
        return saved;
    }

    private Set<String> availableLocales(UUID codingTaskId) {
        return translations.findAllByCodingTaskId(codingTaskId).stream()
                .map(CodingTaskTranslation::getLocale)
                .collect(Collectors.toSet());
    }
}
