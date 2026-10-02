package uz.aifu.lms.worker.queue;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uz.aifu.lms.worker.domain.ClaimedJob;

/**
 * Proves {@code FOR UPDATE SKIP LOCKED} (V011's own documented claim statement) actually holds
 * under real concurrent load — the same property {@code EnrollmentConcurrencyIT} proves for {@code
 * uq_enrollment_active} in {@code api}, applied here to "no two workers ever claim the same job."
 *
 * <p>Fixture rows are inserted with {@code session_replication_role = replica} to bypass FK trigger
 * checks for {@code coding_task_id}/{@code enrollment_id} — this test exercises {@code
 * execution_jobs} claim SQL only, not the full academic FK chain a real submission would need (see
 * {@code RestrictedRoleGrantsIT}'s class doc for the same reasoning applied to grants instead of
 * concurrency).
 */
@Tag("it")
@Testcontainers(disabledWithoutDocker = true)
class ExecutionJobQueueConcurrencyIT {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("aifu_lms")
                    .withUsername("aifu_app")
                    .withPassword("it_only_password_not_used_anywhere_else");

    private static HikariDataSource dataSource;

    @BeforeAll
    static void migrate() {
        POSTGRES.start();
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("filesystem:../api/src/main/resources/db/migration")
                .load()
                .migrate();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(POSTGRES.getJdbcUrl());
        config.setUsername(POSTGRES.getUsername());
        config.setPassword(POSTGRES.getPassword());
        config.setMaximumPoolSize(16);
        dataSource = new HikariDataSource(config);
    }

    @AfterAll
    static void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
        POSTGRES.stop();
    }

    @Test
    void eightConcurrentWorkers_onlyOneEverClaimsTheSameJob() throws Exception {
        JdbcTemplate setup = new JdbcTemplate(dataSource);
        UUID submissionId = insertFixtureSubmissionAndJob(setup);

        int workerCount = 8;
        ExecutorService pool = Executors.newFixedThreadPool(workerCount);
        try {
            List<Future<List<ClaimedJob>>> futures =
                    IntStream.range(0, workerCount)
                            .mapToObj(
                                    i ->
                                            pool.submit(
                                                    () -> {
                                                        ExecutionJobQueue queue =
                                                                new ExecutionJobQueue(
                                                                        new JdbcTemplate(
                                                                                dataSource),
                                                                        Clock.systemUTC());
                                                        return queue.claim(
                                                                "worker-" + i,
                                                                1,
                                                                Duration.ofMinutes(2));
                                                    }))
                            .toList();

            int totalClaims = 0;
            for (Future<List<ClaimedJob>> future : futures) {
                totalClaims += future.get(10, TimeUnit.SECONDS).size();
            }

            assertThat(totalClaims).isEqualTo(1);
            String status =
                    setup.queryForObject(
                            "select status from execution_jobs where submission_id = ?",
                            String.class,
                            submissionId);
            assertThat(status).isEqualTo("CLAIMED");
        } finally {
            pool.shutdownNow();
        }
    }

    private UUID insertFixtureSubmissionAndJob(JdbcTemplate jdbc) {
        UUID submissionId = UUID.randomUUID();
        jdbc.execute("SET session_replication_role = replica");
        try {
            jdbc.update(
                    """
                    INSERT INTO submissions
                        (id, coding_task_id, enrollment_id, language_code, attempt_number,
                         source_code, source_size_bytes, status, submitted_at)
                    VALUES (?, ?, ?, 'python', 1, 'print(1)', 8, 'QUEUED', now())
                    """,
                    submissionId,
                    UUID.randomUUID(),
                    UUID.randomUUID());
        } finally {
            jdbc.execute("SET session_replication_role = origin");
        }
        jdbc.update("INSERT INTO execution_jobs (submission_id) VALUES (?)", submissionId);
        return submissionId;
    }
}
