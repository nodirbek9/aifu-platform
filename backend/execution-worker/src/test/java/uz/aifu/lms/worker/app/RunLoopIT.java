package uz.aifu.lms.worker.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uz.aifu.lms.worker.config.SandboxProperties;
import uz.aifu.lms.worker.config.WorkerProperties;
import uz.aifu.lms.worker.data.CodeRunResultRepository;
import uz.aifu.lms.worker.data.SubmissionDataRepository;
import uz.aifu.lms.worker.queue.CodeRunQueue;
import uz.aifu.lms.worker.sandbox.SandboxEngine;
import uz.aifu.lms.worker.sandbox.SandboxRunRequest;
import uz.aifu.lms.worker.sandbox.SandboxRunResult;

/**
 * Ungraded runs through the real claim → run → write-back path, against real Postgres, with a
 * scripted sandbox in place of Docker. The loop connects as the RESTRICTED worker role (V014 +
 * V022), so this also proves those grants are enough for the work — and no more is needed.
 *
 * <p>Fixture rows are inserted by the owner role with {@code session_replication_role = replica},
 * the same shortcut {@code ExecutionJobQueueConcurrencyIT} takes: this test is about runs, not
 * about the academic FK chain a real task hangs from.
 */
@Tag("it")
@Testcontainers(disabledWithoutDocker = true)
class RunLoopIT {

    private static final String WORKER_ROLE = "aifu_execution_worker";
    private static final String WORKER_PASSWORD = "it_only_worker_password";

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("aifu_lms")
                    .withUsername("aifu_app")
                    .withPassword("it_only_password_not_used_anywhere_else");

    private static HikariDataSource ownerDataSource;
    private static HikariDataSource workerDataSource;
    private static JdbcTemplate owner;

    /** What the scripted sandbox answers, in order; the requests it received. */
    private final List<SandboxRunResult> scripted = new ArrayList<>();

    private final List<SandboxRunRequest> received = new ArrayList<>();

    @BeforeAll
    static void migrate() {
        POSTGRES.start();
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("filesystem:../api/src/main/resources/db/migration")
                .load()
                .migrate();
        // One connection: "SET session_replication_role" must apply to the inserts after it.
        ownerDataSource = dataSource(POSTGRES.getUsername(), POSTGRES.getPassword(), 1);
        owner = new JdbcTemplate(ownerDataSource);
        owner.execute("ALTER ROLE " + WORKER_ROLE + " PASSWORD '" + WORKER_PASSWORD + "'");
        workerDataSource = dataSource(WORKER_ROLE, WORKER_PASSWORD, 4);
    }

    @AfterAll
    static void tearDown() {
        if (workerDataSource != null) {
            workerDataSource.close();
        }
        if (ownerDataSource != null) {
            ownerDataSource.close();
        }
        POSTGRES.stop();
    }

    /** A run left waiting by another test must not be claimed by this one's loop. */
    @BeforeEach
    void quiesce() {
        owner.update(
                "update code_runs set status = 'FAILED' where status in ('QUEUED', 'RUNNING')");
    }

    @Test
    void customRun_storesTheWholeOutput_andIsNeverASubmission() {
        UUID runId = insertRun(null, "python", "CUSTOM", "print(input())", "salom");
        scripted.add(ok("salom\n"));

        loop().pollAndProcess();

        Map<String, Object> row = run(runId);
        assertThat(row.get("status")).isEqualTo("COMPLETED");
        assertThat(row.get("verdict")).isEqualTo("OK");
        assertThat(row.get("stdout")).isEqualTo("salom\n");
        assertThat(row.get("exit_code")).isEqualTo(0);
        assertThat(received).hasSize(1);
        assertThat(received.get(0).stdin()).isEqualTo("salom");
        assertThat(received.get(0).sourceFilename()).isEqualTo("main.py");
        assertThat(owner.queryForObject("select count(*) from submissions", Long.class)).isZero();
    }

    @Test
    void customRun_reportsACrash_aCompileError_andStripsNulBytes() {
        UUID crashed = insertRun(null, "python", "CUSTOM", "1/0", "");
        scripted.add(
                new SandboxRunResult(
                        true,
                        null,
                        1,
                        "a\u0000b",
                        "ZeroDivisionError",
                        9,
                        null,
                        false,
                        false,
                        null));
        loop().pollAndProcess();
        assertThat(run(crashed).get("verdict")).isEqualTo("RUNTIME_ERROR");
        assertThat(run(crashed).get("stdout")).isEqualTo("ab");
        assertThat(run(crashed).get("stderr")).isEqualTo("ZeroDivisionError");

        UUID broken = insertRun(null, "cpp", "CUSTOM", "int main( {", "");
        scripted.add(SandboxRunResult.compileFailure("main.cpp:1: error: expected ')'"));
        loop().pollAndProcess();
        assertThat(run(broken).get("status")).isEqualTo("COMPLETED");
        assertThat(run(broken).get("verdict")).isEqualTo("COMPILATION_ERROR");
        assertThat((String) run(broken).get("compile_output")).contains("expected ')'");
        assertThat(received.get(1).sourceFilename()).isEqualTo("main.cpp");
        assertThat(received.get(1).compileRequired()).isTrue();
    }

    @Test
    void examplesRun_touchesOnlyExampleTests_verifyRunsThemAll() {
        UUID taskId = insertTaskWithOneExampleAndOneHiddenTest();

        UUID examples = insertRun(taskId, "python", "EXAMPLES", "print(6)", "");
        scripted.add(ok("6\n"));
        loop().pollAndProcess();
        assertThat(received).hasSize(1);
        assertThat(received.get(0).stdin()).isEqualTo("3\n1 2 3");
        // the task's own limits apply, clamped to the platform ceiling
        assertThat(received.get(0).cpuTimeLimitMs()).isEqualTo(2000);
        Map<String, Object> examplesRow = run(examples);
        assertThat(examplesRow.get("verdict")).isEqualTo("ACCEPTED");
        assertThat(examplesRow.get("tests_passed")).isEqualTo(1);
        assertThat(examplesRow.get("tests_total")).isEqualTo(1);
        assertThat(testVerdicts(examples)).containsExactly("ACCEPTED");

        UUID verify = insertRun(taskId, "python", "VERIFY", "print(6)", "");
        scripted.add(ok("6\n"));
        scripted.add(ok("6\n"));
        loop().pollAndProcess();
        assertThat(received).hasSize(3);
        Map<String, Object> verifyRow = run(verify);
        assertThat(verifyRow.get("verdict")).isEqualTo("WRONG_ANSWER");
        assertThat(verifyRow.get("tests_passed")).isEqualTo(1);
        assertThat(verifyRow.get("tests_total")).isEqualTo(2);
        assertThat(testVerdicts(verify)).containsExactly("ACCEPTED", "WRONG_ANSWER");
    }

    @Test
    void sandboxTrouble_isRetriedOnce_thenTheRunFailsSoItsAuthorStopsWaiting() {
        UUID runId = insertRun(null, "java", "CUSTOM", "class Main {}", "");
        scripted.add(SandboxRunResult.internalFailure("docker daemon unreachable"));
        scripted.add(SandboxRunResult.internalFailure("docker daemon unreachable"));

        loop().pollAndProcess();
        assertThat(run(runId).get("status")).isEqualTo("QUEUED");
        assertThat(run(runId).get("attempt_count")).isEqualTo(1);

        owner.update("update code_runs set available_at = now() where id = ?", runId);
        loop().pollAndProcess();
        Map<String, Object> row = run(runId);
        assertThat(row.get("status")).isEqualTo("FAILED");
        assertThat(row.get("verdict")).isEqualTo("INTERNAL_ERROR");
        assertThat((String) row.get("internal_error")).contains("docker daemon unreachable");
    }

    @Test
    void aRunWhoseWorkerDied_isReclaimed() {
        UUID runId = insertRun(null, "python", "CUSTOM", "print(1)", "");
        CodeRunQueue queue =
                new CodeRunQueue(new JdbcTemplate(workerDataSource), Clock.systemUTC());
        assertThat(queue.claim("dead-worker", 5, Duration.ofMinutes(2))).isNotEmpty();
        owner.update(
                "update code_runs set lease_expires_at = now() - interval '1 minute' where id = ?",
                runId);

        assertThat(queue.reclaimExpiredLeases(RunLoop.MAX_ATTEMPTS)).isEqualTo(1);
        assertThat(run(runId).get("status")).isEqualTo("QUEUED");
    }

    // ------------------------------------------------------------------ helpers

    private RunLoop loop() {
        JdbcTemplate worker = new JdbcTemplate(workerDataSource);
        Clock clock = Clock.systemUTC();
        SandboxEngine engine =
                new SandboxEngine() {
                    @Override
                    public boolean supports(String engineKey) {
                        return true;
                    }

                    @Override
                    public SandboxRunResult run(SandboxRunRequest request) {
                        received.add(request);
                        return scripted.remove(0);
                    }
                };
        return new RunLoop(
                new CodeRunQueue(worker, clock),
                new SubmissionDataRepository(worker),
                new CodeRunResultRepository(worker, clock),
                engine,
                new WorkerProperties(
                        "it-worker", 1, Duration.ofSeconds(1), 5, Duration.ofMinutes(2), 3),
                new SandboxProperties(
                        "scripted", null, null, 5000, 10000, 262144, 64, 65536, false));
    }

    private static SandboxRunResult ok(String stdout) {
        return new SandboxRunResult(true, null, 0, stdout, "", 15, null, false, false, null);
    }

    private static HikariDataSource dataSource(String username, String password, int size) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(POSTGRES.getJdbcUrl());
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(size);
        return new HikariDataSource(config);
    }

    private UUID insertRun(UUID taskId, String language, String mode, String source, String stdin) {
        UUID runId = UUID.randomUUID();
        owner.execute("SET session_replication_role = replica");
        try {
            owner.update(
                    """
                    INSERT INTO code_runs (id, user_id, coding_task_id, language_code, mode,
                        source_code, stdin)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    runId,
                    UUID.randomUUID(),
                    taskId,
                    language,
                    mode,
                    source,
                    stdin);
        } finally {
            owner.execute("SET session_replication_role = origin");
        }
        return runId;
    }

    private UUID insertTaskWithOneExampleAndOneHiddenTest() {
        UUID taskId = UUID.randomUUID();
        owner.execute("SET session_replication_role = replica");
        try {
            owner.update(
                    "INSERT INTO coding_tasks (id, course_id, code, cpu_time_limit_ms) VALUES (?, ?, ?, 2000)",
                    taskId,
                    UUID.randomUUID(),
                    "T-" + taskId);
            owner.update(
                    """
                    INSERT INTO coding_task_test_cases
                        (coding_task_id, position, visibility, is_example, stdin, expected_stdout)
                    VALUES (?, 0, 'VISIBLE', true, '3\n1 2 3', '6'), (?, 1, 'HIDDEN', false, '4\n-5 5 -3 3', '0')
                    """,
                    taskId,
                    taskId);
        } finally {
            owner.execute("SET session_replication_role = origin");
        }
        return taskId;
    }

    private static Map<String, Object> run(UUID runId) {
        return owner.queryForMap("select * from code_runs where id = ?", runId);
    }

    private static List<String> testVerdicts(UUID runId) {
        return owner.queryForList(
                "select verdict from code_run_test_results where run_id = ? order by position",
                String.class,
                runId);
    }
}
