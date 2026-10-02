package uz.aifu.lms.execution.app;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.coding.app.CodingTaskContentService;
import uz.aifu.lms.coding.app.CodingTaskService;
import uz.aifu.lms.coding.app.ProgrammingLanguageService;
import uz.aifu.lms.coding.domain.CodingTask;
import uz.aifu.lms.coding.domain.CodingTaskLanguage;
import uz.aifu.lms.coding.domain.CodingTaskTestCase;
import uz.aifu.lms.common.config.ExecutionProperties;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.execution.api.dto.RunDtos.RunRequest;
import uz.aifu.lms.execution.api.dto.RunDtos.RunTestView;
import uz.aifu.lms.execution.api.dto.RunDtos.RunView;
import uz.aifu.lms.execution.api.dto.RunDtos.VerificationView;
import uz.aifu.lms.execution.domain.CodeRun;
import uz.aifu.lms.execution.domain.CodeRunMode;
import uz.aifu.lms.execution.domain.CodeRunStatus;
import uz.aifu.lms.execution.domain.CodeRunTestResult;
import uz.aifu.lms.execution.domain.CodeRunVerdict;
import uz.aifu.lms.execution.persistence.CodeRunRepository;
import uz.aifu.lms.execution.persistence.CodeRunTestResultRepository;
import uz.aifu.lms.iam.app.access.AccessGuard;
import uz.aifu.lms.iam.app.access.CourseAccess;

/**
 * Ungraded runs (product spec S-04.4, S-05.4, S-05.5, T-05). Like a submission, a run is only
 * enqueued here — the execution worker runs it in the sandbox (ADR-006). Unlike a submission it
 * leaves no trace in attempts, grades or progress, and is deleted after a retention period.
 */
@Service
public class CodeRunService {

    private static final Logger log = LoggerFactory.getLogger(CodeRunService.class);

    private final CodeRunRepository runs;
    private final CodeRunTestResultRepository testResults;
    private final CodingTaskService codingTasks;
    private final CodingTaskContentService taskContent;
    private final ProgrammingLanguageService languages;
    private final AccessGuard accessGuard;
    private final ExecutionProperties properties;
    private final Clock clock;
    private final Cache<UUID, Bucket> rateLimitBuckets =
            Caffeine.newBuilder()
                    .maximumSize(50_000)
                    .expireAfterAccess(Duration.ofHours(1))
                    .build();

    public CodeRunService(
            CodeRunRepository runs,
            CodeRunTestResultRepository testResults,
            CodingTaskService codingTasks,
            CodingTaskContentService taskContent,
            ProgrammingLanguageService languages,
            AccessGuard accessGuard,
            ExecutionProperties properties,
            Clock clock) {
        this.runs = runs;
        this.testResults = testResults;
        this.codingTasks = codingTasks;
        this.taskContent = taskContent;
        this.languages = languages;
        this.accessGuard = accessGuard;
        this.properties = properties;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ run / run examples

    @Transactional
    public RunView run(UUID userId, RunRequest request) {
        CodeRunMode mode = request.mode() == null ? CodeRunMode.CUSTOM : request.mode();
        if (mode == CodeRunMode.VERIFY) {
            throw Exceptions.validation("mode must be CUSTOM or EXAMPLES");
        }
        if (request.codingTaskId() == null) {
            if (mode == CodeRunMode.EXAMPLES) {
                throw Exceptions.validation("Running the examples needs a codingTaskId");
            }
            languages.requireEnabled(request.languageCode());
        } else {
            CourseAccess access = accessGuard.requireCodingTaskViewer(request.codingTaskId());
            CodingTask task = codingTasks.require(request.codingTaskId());
            if (task.getStatus() != PublicationStatus.PUBLISHED && !access.canSeeDrafts()) {
                throw Exceptions.notFound("CodingTask", request.codingTaskId());
            }
            requireTaskLanguage(request.codingTaskId(), request.languageCode());
        }
        requireSize(request.sourceCode(), request.stdin());
        requireNotThrottled(userId);

        CodeRun saved =
                runs.save(
                        new CodeRun(
                                userId,
                                request.codingTaskId(),
                                request.languageCode(),
                                mode,
                                request.sourceCode(),
                                mode == CodeRunMode.CUSTOM ? request.stdin() : null,
                                clock.instant()));
        return toView(saved, List.of(), Map.of());
    }

    /** The caller's own run. Somebody else's run does not exist as far as the caller can tell. */
    @Transactional(readOnly = true)
    public RunView get(UUID runId, UUID userId) {
        CodeRun run =
                runs.findById(runId)
                        .filter(r -> r.getUserId().equals(userId))
                        .orElseThrow(() -> Exceptions.notFound("CodeRun", runId));
        return toViews(List.of(run)).get(0);
    }

    // ------------------------------------------------------------------ verify solutions

    /**
     * Queues every reference solution of a task against all of its tests. The caller must already
     * be an editor of the task ({@code AccessGuard.requireCodingTaskEditor}).
     */
    @Transactional
    public VerificationView verify(UUID codingTaskId, UUID actorId) {
        codingTasks.require(codingTaskId);
        List<CodingTaskLanguage> withSolution =
                taskContent.listLanguages(codingTaskId).stream()
                        .filter(l -> l.getLanguage().isEnabled())
                        .filter(l -> l.getSolutionCode() != null && !l.getSolutionCode().isBlank())
                        .toList();
        if (withSolution.isEmpty()) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION,
                    "This task has no reference solution to verify");
        }
        if (taskContent.listTestCases(codingTaskId).isEmpty()) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION, "This task has no test cases yet");
        }
        Instant now = clock.instant();
        for (CodingTaskLanguage language : withSolution) {
            runs.save(
                    new CodeRun(
                            actorId,
                            codingTaskId,
                            language.getLanguage().getCode(),
                            CodeRunMode.VERIFY,
                            language.getSolutionCode(),
                            null,
                            now));
        }
        return verification(codingTaskId);
    }

    /** The most recent verification of each language's solution; poll it until all are terminal. */
    @Transactional(readOnly = true)
    public VerificationView verification(UUID codingTaskId) {
        codingTasks.require(codingTaskId);
        Map<String, CodeRun> latest = new LinkedHashMap<>();
        for (CodeRun run :
                runs.findByCodingTaskIdAndModeOrderByCreatedAtDesc(
                        codingTaskId, CodeRunMode.VERIFY)) {
            latest.putIfAbsent(run.getLanguageCode(), run);
        }
        List<String> missing = new ArrayList<>();
        List<CodeRun> current = new ArrayList<>();
        for (CodingTaskLanguage language : taskContent.listLanguages(codingTaskId)) {
            String code = language.getLanguage().getCode();
            boolean hasSolution =
                    language.getSolutionCode() != null && !language.getSolutionCode().isBlank();
            if (!hasSolution) {
                missing.add(code);
            } else if (latest.containsKey(code)) {
                current.add(latest.get(code));
            }
        }
        boolean allPassed =
                missing.isEmpty()
                        && !current.isEmpty()
                        && current.stream()
                                .allMatch(r -> r.getVerdict() == CodeRunVerdict.ACCEPTED);
        return new VerificationView(codingTaskId, missing, allPassed, toViews(current));
    }

    // ------------------------------------------------------------------ housekeeping

    /** Deletes expired runs and fails the ones no worker picked up. */
    @Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT2M")
    @Transactional
    public void sweep() {
        Instant now = clock.instant();
        int abandoned =
                runs.failUnclaimedBefore(
                        now.minus(properties.runGiveUpAfter()),
                        now,
                        "The execution service did not pick this run up; try again later");
        int scratch =
                runs.deleteScratchRunsBefore(
                        CodeRunMode.VERIFY, now.minus(properties.runRetention()));
        int verifications =
                runs.deleteByModeBefore(
                        CodeRunMode.VERIFY, now.minus(properties.verifyRunRetention()));
        if (abandoned + scratch + verifications > 0) {
            log.info(
                    "Code runs: failed {} unclaimed, deleted {} expired",
                    abandoned,
                    scratch + verifications);
        }
    }

    // ------------------------------------------------------------------ guards

    private void requireTaskLanguage(UUID codingTaskId, String languageCode) {
        boolean accepted =
                taskContent.listLanguages(codingTaskId).stream()
                        .anyMatch(
                                l ->
                                        l.getLanguage().getCode().equals(languageCode)
                                                && l.getLanguage().isEnabled());
        if (!accepted) {
            throw Exceptions.rule(
                    ErrorCode.LANGUAGE_NOT_ENABLED, "This task does not accept " + languageCode);
        }
    }

    private void requireSize(String sourceCode, String stdin) {
        int sourceBytes = sourceCode.getBytes(StandardCharsets.UTF_8).length;
        if (sourceBytes == 0 || sourceBytes > properties.maxSourceBytes()) {
            throw Exceptions.rule(
                    ErrorCode.SOURCE_TOO_LARGE,
                    "Source must be between 1 and " + properties.maxSourceBytes() + " bytes");
        }
        if (stdin != null
                && stdin.getBytes(StandardCharsets.UTF_8).length > properties.maxStdinBytes()) {
            throw Exceptions.rule(
                    ErrorCode.SOURCE_TOO_LARGE,
                    "Input must be at most " + properties.maxStdinBytes() + " bytes");
        }
    }

    private void requireNotThrottled(UUID userId) {
        Bucket bucket = rateLimitBuckets.get(userId, ignored -> newBucket());
        if (bucket == null || !bucket.tryConsume(1)) {
            throw Exceptions.rule(
                    ErrorCode.RATE_LIMIT_EXCEEDED, "Too many runs; wait a moment and try again");
        }
        long inFlight =
                runs.countByUserIdAndStatusIn(
                        userId, EnumSet.of(CodeRunStatus.QUEUED, CodeRunStatus.RUNNING));
        if (inFlight >= properties.maxConcurrentRunsPerUser()) {
            throw Exceptions.rule(
                    ErrorCode.TOO_MANY_CONCURRENT_SUBMISSIONS,
                    "A run is still in progress; wait for it to finish");
        }
    }

    private Bucket newBucket() {
        int perMinute = properties.runRateLimitPerMinute();
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(perMinute)
                                .refillGreedy(perMinute, Duration.ofMinutes(1))
                                .build())
                .build();
    }

    // ------------------------------------------------------------------ views

    private List<RunView> toViews(List<CodeRun> found) {
        if (found.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<CodeRunTestResult>> results =
                testResults
                        .findByRunIdInOrderByPosition(found.stream().map(CodeRun::getId).toList())
                        .stream()
                        .collect(Collectors.groupingBy(CodeRunTestResult::getRunId));
        Map<UUID, CodingTaskTestCase> testCases = new LinkedHashMap<>();
        found.stream()
                .map(CodeRun::getCodingTaskId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .forEach(
                        taskId ->
                                taskContent
                                        .listTestCases(taskId)
                                        .forEach(t -> testCases.put(t.getId(), t)));
        return found.stream()
                .map(r -> toView(r, results.getOrDefault(r.getId(), List.of()), testCases))
                .toList();
    }

    private static RunView toView(
            CodeRun run, List<CodeRunTestResult> results, Map<UUID, CodingTaskTestCase> testCases) {
        return new RunView(
                run.getId(),
                run.getCodingTaskId(),
                run.getLanguageCode(),
                run.getMode(),
                run.getStatus(),
                run.getVerdict(),
                run.getCompileOutput(),
                run.getStdout(),
                run.getStderr(),
                run.getExitCode(),
                run.getTimeMs(),
                run.getMemoryKb(),
                run.getTestsPassed(),
                run.getTestsTotal(),
                run.getStatus() == CodeRunStatus.FAILED ? run.getInternalError() : null,
                run.getCreatedAt(),
                run.getCompletedAt(),
                results.stream()
                        .map(r -> toTestView(run, r, testCases.get(r.getTestCaseId())))
                        .toList());
    }

    /**
     * An EXAMPLES run only ever contains example tests, whose input and expected output a student
     * may read anyway. Should a non-example test ever appear in one, its data stays out of the
     * response: only pass/fail and the verdict are shown.
     */
    private static RunTestView toTestView(
            CodeRun run, CodeRunTestResult result, CodingTaskTestCase testCase) {
        boolean open =
                testCase != null && (run.getMode() == CodeRunMode.VERIFY || testCase.isExample());
        return new RunTestView(
                result.getTestCaseId(),
                result.getPosition(),
                testCase == null ? null : testCase.getName(),
                result.isPassed(),
                result.getVerdict(),
                result.getTimeMs(),
                result.getMemoryKb(),
                open ? testCase.getStdin() : null,
                open ? testCase.getExpectedStdout() : null,
                open ? result.getStdoutExcerpt() : null,
                open ? result.getStderrExcerpt() : null);
    }
}
