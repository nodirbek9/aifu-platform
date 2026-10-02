package uz.aifu.lms.worker.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.aifu.lms.worker.config.SandboxProperties;
import uz.aifu.lms.worker.config.WorkerProperties;
import uz.aifu.lms.worker.data.CodeRunResultRepository;
import uz.aifu.lms.worker.data.SubmissionDataRepository;
import uz.aifu.lms.worker.domain.ClaimedRun;
import uz.aifu.lms.worker.domain.CodingTaskRecord;
import uz.aifu.lms.worker.domain.LanguageRecord;
import uz.aifu.lms.worker.domain.TestCaseRecord;
import uz.aifu.lms.worker.grading.Grader;
import uz.aifu.lms.worker.grading.TestOutcome;
import uz.aifu.lms.worker.queue.CodeRunQueue;
import uz.aifu.lms.worker.sandbox.SandboxEngine;
import uz.aifu.lms.worker.sandbox.SandboxRunRequest;
import uz.aifu.lms.worker.sandbox.SandboxRunResult;

/**
 * Ungraded runs (V022): "Run", "Run examples" and a teacher's "Verify solutions". Same sandbox and
 * limits as a graded submission, but the outcome goes to {@code code_runs} only — no score, no
 * attempt, nothing a grade is computed from.
 *
 * <p>A run that fails for an infrastructure reason is retried once and then marked FAILED: nobody
 * waits minutes for scratch work the way a graded submission is retried.
 */
@Component
public class RunLoop {

    static final int MAX_ATTEMPTS = 2;

    private static final Logger log = LoggerFactory.getLogger(RunLoop.class);

    private final CodeRunQueue queue;
    private final SubmissionDataRepository data;
    private final CodeRunResultRepository results;
    private final SandboxEngine engine;
    private final WorkerProperties workerProperties;
    private final SandboxProperties sandboxProperties;

    public RunLoop(
            CodeRunQueue queue,
            SubmissionDataRepository data,
            CodeRunResultRepository results,
            SandboxEngine engine,
            WorkerProperties workerProperties,
            SandboxProperties sandboxProperties) {
        this.queue = queue;
        this.data = data;
        this.results = results;
        this.engine = engine;
        this.workerProperties = workerProperties;
        this.sandboxProperties = sandboxProperties;
    }

    @Scheduled(fixedDelayString = "${aifu.worker.poll-interval:PT1S}")
    public void pollAndProcess() {
        List<ClaimedRun> runs =
                queue.claim(
                        workerProperties.id(),
                        workerProperties.claimBatchSize(),
                        workerProperties.leaseDuration());
        for (ClaimedRun run : runs) {
            processOne(run);
        }
    }

    @Scheduled(fixedDelayString = "PT30S")
    public void sweepExpiredLeases() {
        int reclaimed = queue.reclaimExpiredLeases(MAX_ATTEMPTS);
        if (reclaimed > 0) {
            log.info("Reclaimed {} run(s) with an expired lease", reclaimed);
        }
    }

    void processOne(ClaimedRun run) {
        try {
            execute(run);
        } catch (Exception e) {
            log.warn("Run {} failed", run.id(), e);
            queue.fail(
                    run.id(),
                    run.attemptCount(),
                    MAX_ATTEMPTS,
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    Duration.ofSeconds(3));
        }
    }

    private void execute(ClaimedRun run) {
        LanguageRecord language = data.loadLanguage(run.languageCode());
        CodingTaskRecord task =
                run.codingTaskId() == null ? null : data.loadTask(run.codingTaskId());
        int cpuLimit =
                sandboxProperties.clampCpuTimeMs(task == null ? null : task.cpuTimeLimitMs());
        int wallLimit =
                sandboxProperties.clampWallTimeMs(task == null ? null : task.wallTimeLimitMs());
        int memoryLimit =
                sandboxProperties.clampMemoryKb(task == null ? null : task.memoryLimitKb());

        List<TestCaseRecord> testCases =
                switch (run.mode()) {
                    case "EXAMPLES" -> data.loadExampleTestCases(run.codingTaskId());
                    case "VERIFY" -> data.loadTestCases(run.codingTaskId());
                    default -> List.of();
                };
        if (testCases.isEmpty()) {
            // CUSTOM, or a task that has no example tests: one run, so the author still sees
            // compile errors and the program's output.
            SandboxRunResult result =
                    engine.run(
                            request(run, language, run.stdin(), cpuLimit, wallLimit, memoryLimit));
            requireNoInternalError(result);
            if (!result.compileSuccess()) {
                results.writeCompileFailure(run.id(), result.compileOutput());
                return;
            }
            String failure = Grader.failureVerdict(result, memoryLimit, cpuLimit);
            results.writeCustom(run.id(), failure == null ? "OK" : failure, result);
            return;
        }

        List<TestOutcome> outcomes = new ArrayList<>();
        for (TestCaseRecord testCase : testCases) {
            queue.renewLease(run.id(), workerProperties.leaseDuration());
            int perTestCpu =
                    testCase.cpuTimeLimitMs() == null
                            ? cpuLimit
                            : Math.min(testCase.cpuTimeLimitMs(), cpuLimit);
            SandboxRunResult result =
                    engine.run(
                            request(
                                    run,
                                    language,
                                    testCase.stdin(),
                                    perTestCpu,
                                    wallLimit,
                                    memoryLimit));
            requireNoInternalError(result);
            if (!result.compileSuccess()) {
                results.writeCompileFailure(run.id(), result.compileOutput());
                return;
            }
            outcomes.add(Grader.grade(testCase, memoryLimit, perTestCpu, result));
        }
        results.writeTests(run.id(), testCases, outcomes);
    }

    private SandboxRunRequest request(
            ClaimedRun run,
            LanguageRecord language,
            String stdin,
            int cpuLimit,
            int wallLimit,
            int memoryLimit) {
        return new SandboxRunRequest(
                run.sourceCode(),
                language.code(),
                language.executorKey(),
                language.compileRequired(),
                language.sourceFilename(),
                stdin,
                cpuLimit,
                wallLimit,
                memoryLimit,
                sandboxProperties.maxOutputBytes());
    }

    /** Sandbox infrastructure trouble is not the author's fault: retry instead of reporting it. */
    private static void requireNoInternalError(SandboxRunResult result) {
        if (result.internalError() != null) {
            throw new IllegalStateException("Sandbox internal error: " + result.internalError());
        }
    }
}
