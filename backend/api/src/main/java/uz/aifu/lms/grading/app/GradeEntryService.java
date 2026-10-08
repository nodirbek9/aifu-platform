package uz.aifu.lms.grading.app;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.AssessmentComponent;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.coding.app.SubmissionService;
import uz.aifu.lms.coding.domain.Submission;
import uz.aifu.lms.coding.domain.SubmissionStatus;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.events.NotificationRequestedEvent;
import uz.aifu.lms.common.events.QuizAttemptGradedEvent;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.grading.domain.GradeEntry;
import uz.aifu.lms.grading.domain.GradeEntryStatus;
import uz.aifu.lms.grading.domain.GradeSource;
import uz.aifu.lms.grading.domain.GradeSourceRefType;
import uz.aifu.lms.grading.persistence.GradeEntryRepository;

/**
 * The append-only gradebook (V012, ADR-011). Every write here either inserts a fresh row or moves
 * an existing row along its one legal path (DRAFT→PUBLISHED, PUBLISHED→REVISED/VOIDED) — never an
 * `UPDATE` of score/max/enrollment/component on a published row, matching {@code
 * trg_grade_entry_immutable}'s own enforcement (defense in depth, CLAUDE.md §8).
 */
@Service
public class GradeEntryService {

    private static final Logger log = LoggerFactory.getLogger(GradeEntryService.class);

    private final GradeEntryRepository gradeEntries;
    private final EnrollmentService enrollments;
    private final CourseService courses;
    private final ActivityService activities;
    private final SubmissionService submissions;
    private final FinalScoreCalculator calculator;
    private final AuditService audit;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final GradeEntryService self;

    public GradeEntryService(
            GradeEntryRepository gradeEntries,
            EnrollmentService enrollments,
            CourseService courses,
            ActivityService activities,
            SubmissionService submissions,
            FinalScoreCalculator calculator,
            AuditService audit,
            ApplicationEventPublisher eventPublisher,
            Clock clock,
            @Lazy GradeEntryService self) {
        this.gradeEntries = gradeEntries;
        this.enrollments = enrollments;
        this.courses = courses;
        this.activities = activities;
        this.submissions = submissions;
        this.calculator = calculator;
        this.audit = audit;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.self = self;
    }

    @Transactional(readOnly = true)
    public GradeEntry require(UUID id) {
        return gradeEntries.findById(id).orElseThrow(() -> Exceptions.notFound("GradeEntry", id));
    }

    @Transactional(readOnly = true)
    public List<GradeEntry> listCurrentForEnrollment(UUID enrollmentId) {
        return gradeEntries.findByEnrollment_IdAndStatusOrderByEvaluatedAtDesc(
                enrollmentId, GradeEntryStatus.PUBLISHED);
    }

    /**
     * A student's grades in one course, laid out by the course's assessment scheme (spec S-08).
     * What each component has earned comes from the same calculator that derives the final score,
     * so the page and the final score can never disagree.
     */
    @Transactional(readOnly = true)
    public StudentGradebook gradebookFor(UUID enrollmentId, String locale) {
        Enrollment enrollment = enrollments.require(enrollmentId);
        List<AssessmentComponent> components =
                courses.listAssessmentComponents(enrollment.getCourse().getId());
        List<GradeEntry> current = listCurrentForEnrollment(enrollmentId);
        List<GradeEntry> all =
                gradeEntries.findByEnrollment_IdAndStatusInOrderByEvaluatedAtDesc(
                        enrollmentId,
                        List.of(
                                GradeEntryStatus.PUBLISHED,
                                GradeEntryStatus.REVISED,
                                GradeEntryStatus.VOIDED));

        List<ComponentSpec> specs = components.stream().map(GradeEntryService::toSpec).toList();
        Map<UUID, BigDecimal> earned = calculator.earnedPerComponent(specs, toEarned(current));
        Set<UUID> gradedDirectly =
                current.stream()
                        .map(GradeEntry::getAssessmentComponentId)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());

        List<StudentGradebook.ComponentLine> lines = new ArrayList<>();
        boolean complete = false;
        List<AssessmentComponent> topLevel = ordered(components, null);
        if (!topLevel.isEmpty()) {
            complete = true;
            for (AssessmentComponent top : topLevel) {
                List<AssessmentComponent> children = ordered(components, top.getId());
                boolean graded =
                        gradedDirectly.contains(top.getId())
                                || children.stream()
                                        .anyMatch(c -> gradedDirectly.contains(c.getId()));
                complete = complete && graded;
                lines.add(toLine(top, earned, graded));
                for (AssessmentComponent child : children) {
                    lines.add(toLine(child, earned, gradedDirectly.contains(child.getId())));
                }
            }
        }

        Map<UUID, String> codes =
                components.stream()
                        .filter(c -> c.getCode() != null)
                        .collect(
                                Collectors.toMap(
                                        AssessmentComponent::getId, AssessmentComponent::getCode));
        Map<UUID, String> titles = activityTitles(all, locale);
        return new StudentGradebook(
                enrollment,
                lines,
                current.stream().map(e -> toEntryLine(e, titles, codes)).toList(),
                all.stream().map(e -> toEntryLine(e, titles, codes)).toList(),
                complete);
    }

    private static List<AssessmentComponent> ordered(
            List<AssessmentComponent> components, UUID parentId) {
        return components.stream()
                .filter(
                        c ->
                                parentId == null
                                        ? c.getParent() == null
                                        : c.getParent() != null
                                                && parentId.equals(c.getParent().getId()))
                .sorted(Comparator.comparingInt(AssessmentComponent::getDisplayOrder))
                .toList();
    }

    private StudentGradebook.ComponentLine toLine(
            AssessmentComponent component, Map<UUID, BigDecimal> earned, boolean graded) {
        ComponentSpec spec = toSpec(component);
        BigDecimal points = earned.getOrDefault(component.getId(), BigDecimal.ZERO);
        Boolean gatePassed =
                spec.minPassPercent() != null && graded
                        ? calculator.passesGate(points, spec)
                        : null;
        return new StudentGradebook.ComponentLine(
                spec.id(),
                spec.parentId(),
                component.getCode(),
                spec.kind(),
                spec.maxPoints(),
                spec.minPassPercent(),
                spec.blocksFinal(),
                points,
                graded,
                gatePassed);
    }

    private static StudentGradebook.EntryLine toEntryLine(
            GradeEntry entry, Map<UUID, String> titles, Map<UUID, String> codes) {
        return new StudentGradebook.EntryLine(
                entry,
                entry.getActivityId() == null ? null : titles.get(entry.getActivityId()),
                entry.getAssessmentComponentId() == null
                        ? null
                        : codes.get(entry.getAssessmentComponentId()));
    }

    /** The title of each graded activity: in the reader's language, else Uzbek, else any. */
    private Map<UUID, String> activityTitles(List<GradeEntry> entries, String locale) {
        Set<UUID> activityIds =
                entries.stream()
                        .map(GradeEntry::getActivityId)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());
        Map<UUID, String> titles = new HashMap<>();
        activities
                .listTranslationsForActivities(activityIds)
                .forEach(
                        (activityId, translations) ->
                                translations.stream()
                                        .min(
                                                Comparator.comparingInt(
                                                        t ->
                                                                t.getLocale().equals(locale)
                                                                        ? 0
                                                                        : t.getLocale()
                                                                                        .equals(
                                                                                                LocaleCode
                                                                                                        .DEFAULT
                                                                                                        .code())
                                                                                ? 1
                                                                                : 2))
                                        .ifPresent(t -> titles.put(activityId, t.getTitle())));
        return titles;
    }

    private static ComponentSpec toSpec(AssessmentComponent c) {
        return new ComponentSpec(
                c.getId(),
                c.getParent() == null ? null : c.getParent().getId(),
                c.getKind(),
                c.getMaxPoints(),
                c.getMinPassPercent(),
                c.isBlocksFinal());
    }

    private static List<EarnedEntry> toEarned(List<GradeEntry> current) {
        return current.stream()
                .filter(e -> e.getAssessmentComponentId() != null)
                .map(
                        e ->
                                new EarnedEntry(
                                        e.getAssessmentComponentId(),
                                        e.getScore(),
                                        e.getMaxScore()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<GradeEntry> listCurrentForCourse(UUID courseId, Pageable pageable) {
        return gradeEntries.findByCourseIdAndStatus(courseId, GradeEntryStatus.PUBLISHED, pageable);
    }

    /**
     * {@code assessment.app.AttemptService} publishes {@link QuizAttemptGradedEvent} on grading
     * (ADR-014's pattern, reused) — {@code assessment} cannot call {@code grading} directly (module
     * map runs the other way). {@code AFTER_COMMIT} so grading only ever reacts to an attempt
     * {@code assessment} has actually committed.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuizAttemptGraded(QuizAttemptGradedEvent event) {
        if (event.activityId() == null) {
            return;
        }
        self.recordAutoGrade(
                event.enrollmentId(),
                event.activityId(),
                GradeSource.QUIZ,
                GradeSourceRefType.QUIZ_ATTEMPT,
                event.attemptId(),
                event.score(),
                event.maxScore(),
                event.gradedAt());
    }

    /**
     * Auto-grading entry point — the {@link QuizAttemptGradedEvent} listener above and the
     * coding-submission sync both call this. A no-op if the activity isn't graded or carries no
     * assessment component (not every activity feeds the gradebook). {@code REQUIRES_NEW}: when
     * called from the event listener, that listener runs from inside the committing transaction's
     * {@code triggerAfterCompletion}, where a plain {@code @Transactional} would try to join an
     * already-unbound session and fail (same defect class {@code progress.app.ProgressService}'s
     * own listener hit in Phase 7 — fixed here from the start).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<GradeEntry> recordAutoGrade(
            UUID enrollmentId,
            UUID activityId,
            GradeSource source,
            GradeSourceRefType sourceRefType,
            UUID sourceRefId,
            BigDecimal score,
            BigDecimal maxScore,
            Instant when) {
        Activity activity = activities.require(activityId);
        if (!activity.isGraded() || activity.getAssessmentComponent() == null) {
            return Optional.empty();
        }
        GradeEntry entry =
                recordOrSupersede(
                        enrollmentId,
                        activity.getAssessmentComponent().getId(),
                        activityId,
                        source,
                        sourceRefType,
                        sourceRefId,
                        score,
                        maxScore,
                        null,
                        when);
        if (entry == null) {
            return Optional.empty();
        }
        recomputeFinalScore(enrollmentId);
        return Optional.of(entry);
    }

    /**
     * {@code execution-worker} is a separate process with no shared event bus (ADR-006) — the same
     * reason {@code progress.app.ProgressService} polls instead of listening for coding submissions
     * (V016). This is grading's own independent sweep over the same signal, marking {@code
     * grade_synced_at} (V017) rather than {@code progress_synced_at} so the two consumers never
     * interfere with each other's completeness.
     */
    @Scheduled(fixedDelayString = "PT10S")
    public void syncCodingSubmissions() {
        for (Submission submission : submissions.listUnsyncedForGrading(50)) {
            self.syncOneCodingSubmission(submission.getId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncOneCodingSubmission(UUID submissionId) {
        Submission submission = submissions.require(submissionId);
        if (submission.getGradeSyncedAt() != null) {
            return;
        }
        Instant now = clock.instant();
        if (submission.getStatus() == SubmissionStatus.COMPLETED
                && submission.getActivity() != null) {
            BigDecimal score = submission.getScore();
            BigDecimal maxScore = submission.getMaxScore();
            if (score != null && maxScore != null && maxScore.signum() > 0) {
                recordAutoGrade(
                        submission.getEnrollment().getId(),
                        submission.getActivity().getId(),
                        GradeSource.CODING,
                        GradeSourceRefType.SUBMISSION,
                        submission.getId(),
                        score,
                        maxScore,
                        now);
            }
        }
        submissions.markGradeSynced(submissionId, now);
    }

    /** Teacher-facing manual entry/override — always a {@link GradeSource#MANUAL} row. */
    @Transactional
    public GradeEntry recordManualGrade(
            UUID enrollmentId,
            UUID assessmentComponentId,
            BigDecimal score,
            BigDecimal maxScore,
            UUID evaluatorUserId,
            UUID actorId,
            String ip) {
        Enrollment enrollment = enrollments.require(enrollmentId);
        AssessmentComponent component = courses.requireAssessmentComponent(assessmentComponentId);
        if (!component.getCourse().getId().equals(enrollment.getCourse().getId())) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED,
                    "assessmentComponentId does not belong to this enrollment's course");
        }
        GradeEntry entry =
                recordOrSupersede(
                        enrollmentId,
                        assessmentComponentId,
                        null,
                        GradeSource.MANUAL,
                        GradeSourceRefType.MANUAL,
                        null,
                        score,
                        maxScore,
                        evaluatorUserId,
                        clock.instant());
        audit.record(
                AuditAction.GRADE_RECORDED,
                actorId,
                "enrollment",
                enrollmentId,
                ip,
                null,
                Map.of("assessmentComponentId", assessmentComponentId.toString()));
        recomputeFinalScore(enrollmentId);
        return entry;
    }

    @Transactional
    public void voidEntry(UUID entryId, String reason, UUID actorId, String ip) {
        GradeEntry entry = require(entryId);
        entry.voidEntry(reason);
        audit.record(
                AuditAction.GRADE_VOIDED,
                actorId,
                "enrollment",
                entry.getEnrollment().getId(),
                ip,
                null,
                Map.of("gradeEntryId", entryId.toString(), "reason", reason));
        recomputeFinalScore(entry.getEnrollment().getId());
    }

    /**
     * One current entry per (enrollment, component, source ref) — {@code uq_grade_entry_current}'s
     * own shape. If one already exists with a different score, it is superseded (new revision, old
     * row moves to REVISED); an identical replay is a no-op, matching {@code Submission}'s own
     * idempotent-write reasoning (Phase 8).
     *
     * <p>For graded quiz attempts, the best score counts: when a PUBLISHED grade already exists for
     * the same quiz activity and enrollment, and the new attempt's score is lower than or equal to
     * the existing grade's score, it is not superseded and no new row is created (logged at debug).
     * When the new score is higher, it supersedes with a new revision row.
     */
    private GradeEntry recordOrSupersede(
            UUID enrollmentId,
            UUID assessmentComponentId,
            UUID activityId,
            GradeSource source,
            GradeSourceRefType sourceRefType,
            UUID sourceRefId,
            BigDecimal score,
            BigDecimal maxScore,
            UUID evaluatorUserId,
            Instant when) {
        boolean isQuizAttempt =
                source == GradeSource.QUIZ
                        && sourceRefType == GradeSourceRefType.QUIZ_ATTEMPT
                        && activityId != null;

        Optional<GradeEntry> current =
                isQuizAttempt
                        ? gradeEntries.findByEnrollment_IdAndActivityIdAndSourceAndStatus(
                                enrollmentId,
                                activityId,
                                GradeSource.QUIZ,
                                GradeEntryStatus.PUBLISHED)
                        : gradeEntries
                                .findByEnrollment_IdAndAssessmentComponentIdAndSourceRefTypeAndSourceRefIdAndStatus(
                                        enrollmentId,
                                        assessmentComponentId,
                                        sourceRefType,
                                        sourceRefId,
                                        GradeEntryStatus.PUBLISHED);
        if (current.isPresent()) {
            GradeEntry previous = current.get();
            if (isQuizAttempt) {
                if (score.compareTo(previous.getScore()) <= 0) {
                    log.debug(
                            "Quiz attempt score {} is <= existing published grade score {}; keeping best score for enrollment {} and activity {}",
                            score,
                            previous.getScore(),
                            enrollmentId,
                            activityId);
                    return null;
                }
                previous.markRevised();
                GradeEntry next =
                        GradeEntry.supersede(
                                previous,
                                score,
                                maxScore,
                                source,
                                sourceRefType,
                                sourceRefId,
                                evaluatorUserId,
                                when);
                return gradeEntries.save(next);
            }

            if (previous.getScore().compareTo(score) == 0
                    && previous.getMaxScore().compareTo(maxScore) == 0) {
                return previous;
            }
            previous.markRevised();
            GradeEntry next =
                    GradeEntry.supersede(
                            previous,
                            score,
                            maxScore,
                            source,
                            sourceRefType,
                            sourceRefId,
                            evaluatorUserId,
                            when);
            return gradeEntries.save(next);
        }
        Enrollment enrollment = enrollments.require(enrollmentId);
        GradeEntry entry =
                new GradeEntry(
                        enrollment,
                        assessmentComponentId,
                        activityId,
                        source,
                        sourceRefType,
                        sourceRefId,
                        score,
                        maxScore,
                        evaluatorUserId,
                        when);
        entry.publish(when);
        return gradeEntries.save(entry);
    }

    private void recomputeFinalScore(UUID enrollmentId) {
        Enrollment enrollment = enrollments.require(enrollmentId);
        Course course = courses.require(enrollment.getCourse().getId());
        List<AssessmentComponent> components = courses.listAssessmentComponents(course.getId());
        List<GradeEntry> current = listCurrentForEnrollment(enrollmentId);

        List<ComponentSpec> specs = components.stream().map(GradeEntryService::toSpec).toList();
        List<EarnedEntry> earned = toEarned(current);
        List<GradeBand> bands =
                course.getGradeScale() == null
                        ? List.of()
                        : course.getGradeScale().getBands().stream()
                                .map(
                                        b ->
                                                new GradeBand(
                                                        b.getMinScore(),
                                                        b.getMaxScore(),
                                                        b.getSymbol(),
                                                        b.isPassing()))
                                .toList();

        if (specs.isEmpty() || bands.isEmpty()) {
            return;
        }
        FinalScoreResult result = calculator.compute(specs, earned, bands);
        enrollments.recordFinalGrade(
                enrollmentId,
                result.finalScore(),
                result.bandSymbol(),
                result.admittedToFinal(),
                result.academicDebt());
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("enrollmentId", enrollmentId.toString());
        payload.put("courseId", course.getId().toString());
        payload.put("finalScore", result.finalScore().toString());
        if (result.bandSymbol() != null) {
            payload.put("finalGradeSymbol", result.bandSymbol());
        }
        eventPublisher.publishEvent(
                new NotificationRequestedEvent(
                        enrollment.getStudent().getId(),
                        "GRADE_PUBLISHED",
                        payload,
                        null,
                        clock.instant()));
    }
}
