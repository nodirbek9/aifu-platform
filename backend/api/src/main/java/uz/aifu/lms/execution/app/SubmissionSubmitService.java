package uz.aifu.lms.execution.app;

import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.coding.app.SubmissionService;
import uz.aifu.lms.coding.domain.Submission;
import uz.aifu.lms.execution.domain.ExecutionJob;
import uz.aifu.lms.execution.persistence.ExecutionJobRepository;

/**
 * The one place a submission is created AND enqueued, atomically (ADR-007: "no submission can exist
 * without its job, and no job without its submission"). Lives in {@code execution}, not {@code
 * coding}: the module map only allows {@code execution -> coding}, never the reverse, so this
 * orchestrator — which needs both {@code coding.app.SubmissionService.create} (allowed) and {@code
 * execution}'s own {@link ExecutionJobRepository} (its own persistence) — is the one legal place
 * for the combined write to happen.
 */
@Service
public class SubmissionSubmitService {

    private final SubmissionService submissions;
    private final ExecutionJobRepository jobs;
    private final Clock clock;

    public SubmissionSubmitService(
            SubmissionService submissions, ExecutionJobRepository jobs, Clock clock) {
        this.submissions = submissions;
        this.jobs = jobs;
        this.clock = clock;
    }

    @Transactional
    public Submission submit(
            UUID codingTaskId,
            UUID studentUserId,
            String languageCode,
            String sourceCode,
            String idempotencyKey,
            UUID activityId) {
        Submission submission =
                submissions.create(
                        codingTaskId,
                        studentUserId,
                        languageCode,
                        sourceCode,
                        idempotencyKey,
                        activityId);
        // Idempotent replay (same idempotencyKey as an earlier call) returns the ORIGINAL
        // submission, which already has a job — inserting a second one would violate
        // uq_execution_job_submission. A brand-new submission never has a job yet.
        if (!jobs.existsBySubmission_Id(submission.getId())) {
            jobs.save(new ExecutionJob(submission, 0, clock.instant()));
        }
        return submission;
    }
}
