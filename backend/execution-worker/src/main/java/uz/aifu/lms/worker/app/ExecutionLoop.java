package uz.aifu.lms.worker.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.aifu.lms.worker.config.SandboxProperties;
import uz.aifu.lms.worker.config.WorkerProperties;
import uz.aifu.lms.worker.data.ResultWriteBackRepository;
import uz.aifu.lms.worker.data.SubmissionDataRepository;
import uz.aifu.lms.worker.domain.ClaimedJob;
import uz.aifu.lms.worker.domain.CodingTaskRecord;
import uz.aifu.lms.worker.domain.SubmissionContext;
import uz.aifu.lms.worker.domain.TestCaseRecord;
import uz.aifu.lms.worker.grading.Grader;
import uz.aifu.lms.worker.grading.Scorer;
import uz.aifu.lms.worker.grading.ScoringResult;
import uz.aifu.lms.worker.grading.TestOutcome;
import uz.aifu.lms.worker.queue.ExecutionJobQueue;
import uz.aifu.lms.worker.sandbox.SandboxEngine;
import uz.aifu.lms.worker.sandbox.SandboxRunRequest;
import uz.aifu.lms.worker.sandbox.SandboxRunResult;

/**
 * The claim → run → grade → write-back loop (ADR-006/ADR-007). Each polled job is processed
 * sequentially within one tick — {@code aifu.worker.concurrency} bounds how many worker
 * <em>instances</em> run side by side (a scaling knob for deployment, docker-compose replicas), not
 * an in-process thread pool; correctness never depends on it.
 */
@Component
public class ExecutionLoop {

    private static final Logger log = LoggerFactory.getLogger(ExecutionLoop.class);

    private final ExecutionJobQueue queue;
    private final SubmissionDataRepository data;
    private final ResultWriteBackRepository writeBack;
    private final SandboxEngine engine;
    private final WorkerProperties workerProperties;
    private final SandboxProperties sandboxProperties;

    public ExecutionLoop(
            ExecutionJobQueue queue,
            SubmissionDataRepository data,
            ResultWriteBackRepository writeBack,
            SandboxEngine engine,
            WorkerProperties workerProperties,
            SandboxProperties sandboxProperties) {
        this.queue = queue;
        this.data = data;
        this.writeBack = writeBack;
        this.engine = engine;
        this.workerProperties = workerProperties;
        this.sandboxProperties = sandboxProperties;
    }

    @Scheduled(fixedDelayString = "${aifu.worker.poll-interval:PT1S}")
    public void pollAndProcess() {
        List<ClaimedJob> jobs =
                queue.claim(
                        workerProperties.id(),
                        workerProperties.claimBatchSize(),
                        workerProperties.leaseDuration());
        for (ClaimedJob job : jobs) {
            processOne(job);
        }
    }

    /** Reclaims jobs whose lease expired (worker crash/OOM/network partition) — ADR-007. */
    @Scheduled(fixedDelayString = "PT30S")
    public void sweepExpiredLeases() {
        int reclaimed = queue.reclaimExpiredLeases();
        if (reclaimed > 0) {
            log.info("Reclaimed {} job(s) with an expired lease", reclaimed);
        }
    }

    private void processOne(ClaimedJob job) {
        try {
            queue.markRunning(job.jobId());
            Optional<SubmissionContext> context = data.load(job.submissionId());
            if (context.isEmpty()) {
                throw new IllegalStateException("Submission " + job.submissionId() + " not found");
            }
            runAndGrade(job, context.get());
            queue.complete(job.jobId());
        } catch (Exception e) {
            log.warn("Job {} (submission {}) failed", job.jobId(), job.submissionId(), e);
            queue.fail(
                    job.jobId(),
                    job.attemptCount(),
                    job.maxAttempts(),
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    backoff(job.attemptCount()));
        }
    }

    private void runAndGrade(ClaimedJob job, SubmissionContext context) {
        CodingTaskRecord task = context.task();
        int cpuLimit = sandboxProperties.clampCpuTimeMs(task.cpuTimeLimitMs());
        int wallLimit = sandboxProperties.clampWallTimeMs(task.wallTimeLimitMs());
        int memoryLimit = sandboxProperties.clampMemoryKb(task.memoryLimitKb());
        String sandboxProfile =
                "engine=%s cpuMs=%d wallMs=%d memKb=%d"
                        .formatted(sandboxProperties.engine(), cpuLimit, wallLimit, memoryLimit);

        List<TestOutcome> outcomes = new ArrayList<>();
        for (TestCaseRecord testCase : context.testCases()) {
            int perTestCpu =
                    testCase.cpuTimeLimitMs() == null
                            ? cpuLimit
                            : Math.min(testCase.cpuTimeLimitMs(), cpuLimit);
            SandboxRunRequest request =
                    new SandboxRunRequest(
                            context.submission().sourceCode(),
                            context.language().code(),
                            context.language().executorKey(),
                            context.language().compileRequired(),
                            context.language().sourceFilename(),
                            testCase.stdin(),
                            perTestCpu,
                            wallLimit,
                            memoryLimit,
                            sandboxProperties.maxOutputBytes());
            SandboxRunResult result = engine.run(request);

            if (result.internalError() != null) {
                // Likely transient sandbox infrastructure trouble, not the student's fault —
                // retry the whole job rather than permanently recording a 0-score result.
                throw new IllegalStateException(
                        "Sandbox internal error: " + result.internalError());
            }
            if (!result.compileSuccess()) {
                writeBack.writeCompileFailure(
                        context.submission().id(),
                        job.jobId(),
                        sandboxProperties.engine(),
                        sandboxProfile,
                        result,
                        task.maxPoints(),
                        context.submission().version());
                return;
            }
            outcomes.add(Grader.grade(testCase, memoryLimit, perTestCpu, result));
        }

        ScoringResult scoring =
                Scorer.score(
                        outcomes, context.testCases(), task.scoringStrategy(), task.maxPoints());
        writeBack.writeGraded(
                context.submission().id(),
                job.jobId(),
                sandboxProperties.engine(),
                sandboxProfile,
                outcomes,
                scoring,
                context.submission().version());
    }

    private static Duration backoff(int attemptCount) {
        long seconds = Math.min(300, (long) Math.pow(2, Math.max(0, attemptCount)) * 5);
        return Duration.ofSeconds(seconds);
    }
}
