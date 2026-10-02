package uz.aifu.lms.coding.app;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.coding.domain.CodingTask;
import uz.aifu.lms.coding.domain.CodingTaskLanguage;
import uz.aifu.lms.coding.domain.ProgrammingLanguage;
import uz.aifu.lms.coding.domain.Submission;
import uz.aifu.lms.coding.domain.SubmissionStatus;
import uz.aifu.lms.coding.persistence.CodingTaskLanguageRepository;
import uz.aifu.lms.coding.persistence.SubmissionRepository;
import uz.aifu.lms.common.config.ExecutionProperties;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.curriculum.app.ActivityService;
import uz.aifu.lms.curriculum.domain.Activity;
import uz.aifu.lms.curriculum.domain.ActivityType;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;

/**
 * Submission creation guards (docs/planning/phase-plan.md Phase 8,
 * skills/coding-execution-security/SKILL.md). Deliberately does <b>not</b> enqueue an {@code
 * execution_job} — that would need {@code coding -> execution}, the wrong direction (module map:
 * {@code execution -> coding}). {@code execution.app}'s own orchestrator calls {@link #create} and
 * enqueues in the same transaction (ADR-007).
 */
@Service
public class SubmissionService {

    private final SubmissionRepository submissions;
    private final CodingTaskService codingTasks;
    private final CodingTaskLanguageRepository taskLanguages;
    private final EnrollmentService enrollments;
    private final ActivityService activities;
    private final ExecutionProperties executionProperties;
    private final Clock clock;
    private final Cache<UUID, Bucket> rateLimitBuckets =
            Caffeine.newBuilder()
                    .maximumSize(50_000)
                    .expireAfterAccess(Duration.ofHours(1))
                    .build();

    public SubmissionService(
            SubmissionRepository submissions,
            CodingTaskService codingTasks,
            CodingTaskLanguageRepository taskLanguages,
            EnrollmentService enrollments,
            ActivityService activities,
            ExecutionProperties executionProperties,
            Clock clock) {
        this.submissions = submissions;
        this.codingTasks = codingTasks;
        this.taskLanguages = taskLanguages;
        this.enrollments = enrollments;
        this.activities = activities;
        this.executionProperties = executionProperties;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Submission require(UUID id) {
        return submissions
                .findWithDetailsById(id)
                .orElseThrow(() -> Exceptions.notFound("Submission", id));
    }

    /**
     * Ownership-checked accessor for the caller's own submission (CLAUDE.md §7.3), same shape as
     * {@code AttemptService#require(UUID, UUID)}.
     */
    @Transactional(readOnly = true)
    public Submission require(UUID id, UUID studentUserId) {
        Submission submission = require(id);
        if (!submission.getEnrollment().getStudent().getId().equals(studentUserId)) {
            throw Exceptions.notFound("Submission", id);
        }
        return submission;
    }

    @Transactional(readOnly = true)
    public Page<Submission> listOwn(UUID codingTaskId, UUID studentUserId, Pageable pageable) {
        CodingTask task = codingTasks.require(codingTaskId);
        Enrollment enrollment =
                enrollments.requireActiveEnrollment(task.getCourse().getId(), studentUserId);
        return submissions.findByEnrollment_IdAndCodingTask_IdOrderByAttemptNumberDesc(
                enrollment.getId(), codingTaskId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Submission> listForTask(UUID codingTaskId, Pageable pageable) {
        return submissions.findByCodingTask_Id(codingTaskId, pageable);
    }

    /**
     * Backs {@code progress.app.ProgressService}'s scheduled sweeper — terminal submissions tied to
     * a gradable activity that have not yet been folded into {@code activity_progress} (ADR-007's
     * "progress recompute" scheduled work; V016).
     */
    @Transactional(readOnly = true)
    public List<Submission> listUnsyncedForProgress(int batchSize) {
        return submissions.findByStatusInAndActivityIsNotNullAndProgressSyncedAtIsNull(
                List.of(
                        SubmissionStatus.COMPLETED,
                        SubmissionStatus.FAILED,
                        SubmissionStatus.CANCELLED),
                PageRequest.of(0, batchSize));
    }

    @Transactional
    public void markProgressSynced(UUID submissionId, Instant now) {
        Submission submission = require(submissionId);
        submission.markProgressSynced(now);
    }

    /**
     * Backs {@code grading.app.GradeEntryService}'s scheduled sweeper — same shape as {@link
     * #listUnsyncedForProgress}, independent marker column (V017).
     */
    @Transactional(readOnly = true)
    public List<Submission> listUnsyncedForGrading(int batchSize) {
        return submissions.findByStatusInAndActivityIsNotNullAndGradeSyncedAtIsNull(
                List.of(
                        SubmissionStatus.COMPLETED,
                        SubmissionStatus.FAILED,
                        SubmissionStatus.CANCELLED),
                PageRequest.of(0, batchSize));
    }

    @Transactional
    public void markGradeSynced(UUID submissionId, Instant now) {
        Submission submission = require(submissionId);
        submission.markGradeSynced(now);
    }

    /**
     * Creates and persists one {@link Submission} in {@code QUEUED} status. Idempotent: a retry
     * with the same {@code idempotencyKey} for the same enrollment returns the original row instead
     * of creating a second one (CLAUDE.md §9).
     */
    @Transactional
    public Submission create(
            UUID codingTaskId,
            UUID studentUserId,
            String languageCode,
            String sourceCode,
            String idempotencyKey,
            UUID activityId) {
        CodingTask task = codingTasks.requirePublished(codingTaskId);
        Enrollment enrollment =
                enrollments.requireActiveEnrollment(task.getCourse().getId(), studentUserId);

        if (idempotencyKey != null) {
            Optional<Submission> existing =
                    submissions.findByEnrollment_IdAndIdempotencyKey(
                            enrollment.getId(), idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        requireNotRateLimited(studentUserId);

        CodingTaskLanguage taskLanguage =
                taskLanguages
                        .findByCodingTask_IdAndLanguage_Code(codingTaskId, languageCode)
                        .orElseThrow(
                                () ->
                                        Exceptions.rule(
                                                ErrorCode.LANGUAGE_NOT_ENABLED,
                                                "This task does not accept " + languageCode));
        ProgrammingLanguage language = taskLanguage.getLanguage();
        if (!language.isEnabled()) {
            throw Exceptions.rule(ErrorCode.LANGUAGE_NOT_ENABLED, languageCode + " is not enabled");
        }

        int sourceBytes =
                sourceCode == null ? 0 : sourceCode.getBytes(StandardCharsets.UTF_8).length;
        if (sourceBytes == 0 || sourceBytes > executionProperties.maxSourceBytes()) {
            throw Exceptions.rule(
                    ErrorCode.SOURCE_TOO_LARGE,
                    "Source must be between 1 and "
                            + executionProperties.maxSourceBytes()
                            + " bytes");
        }

        int maxAttemptNumber = submissions.findMaxAttemptNumber(codingTaskId, enrollment.getId());
        if (task.getMaxAttempts() != null && maxAttemptNumber >= task.getMaxAttempts()) {
            throw Exceptions.rule(
                    ErrorCode.ATTEMPT_LIMIT_REACHED, "Maximum attempts reached for this task");
        }

        long inFlight =
                submissions.countByEnrollment_IdAndStatusIn(
                        enrollment.getId(),
                        EnumSet.of(SubmissionStatus.QUEUED, SubmissionStatus.RUNNING));
        if (inFlight >= executionProperties.maxConcurrentSubmissionsPerStudent()) {
            throw Exceptions.rule(
                    ErrorCode.TOO_MANY_CONCURRENT_SUBMISSIONS,
                    "Too many submissions already queued or running; wait for one to finish");
        }

        Activity activity = resolveActivity(activityId, codingTaskId);
        Instant now = clock.instant();
        Submission submission =
                new Submission(
                        task,
                        enrollment,
                        activity,
                        language,
                        maxAttemptNumber + 1,
                        sourceCode,
                        idempotencyKey,
                        now);
        try {
            return submissions.saveAndFlush(submission);
        } catch (DataIntegrityViolationException e) {
            // A genuine concurrent double-submit race (same idempotency key, or both computed the
            // same attempt number) must surface here, not later at an uncontrolled commit-time
            // flush — same reasoning as EnrollmentService.enroll.
            if (idempotencyKey != null) {
                return submissions
                        .findByEnrollment_IdAndIdempotencyKey(enrollment.getId(), idempotencyKey)
                        .orElseThrow(() -> e);
            }
            throw Exceptions.conflict(
                    ErrorCode.CONCURRENT_MODIFICATION,
                    "A concurrent submission already claimed this attempt");
        }
    }

    private void requireNotRateLimited(UUID studentUserId) {
        Bucket bucket = rateLimitBuckets.get(studentUserId, ignored -> newRateLimitBucket());
        if (bucket == null || !bucket.tryConsume(1)) {
            throw Exceptions.rule(
                    ErrorCode.RATE_LIMIT_EXCEEDED,
                    "Too many submissions; slow down and try again shortly");
        }
    }

    private Bucket newRateLimitBucket() {
        int perMinute = executionProperties.submissionRateLimitPerMinute();
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(perMinute)
                                .refillGreedy(perMinute, Duration.ofMinutes(1))
                                .build())
                .build();
    }

    private Activity resolveActivity(UUID activityId, UUID codingTaskId) {
        if (activityId == null) {
            return activities
                    .findSolePublishedFor(ActivityType.CODING_TASK, codingTaskId)
                    .orElse(null);
        }
        Activity activity = activities.require(activityId);
        if (activity.getActivityType() != ActivityType.CODING_TASK
                || !codingTaskId.equals(activity.getReferenceId())) {
            throw Exceptions.validation("activityId does not reference this coding task");
        }
        return activity;
    }
}
