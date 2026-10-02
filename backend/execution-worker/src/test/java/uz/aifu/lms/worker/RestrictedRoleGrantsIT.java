package uz.aifu.lms.worker;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Proves V014's restricted {@code aifu_execution_worker} role grants are EXACTLY what
 * skills/coding-execution-security/SKILL.md and V014's own header comment promise — not by
 * empirically poking at fixture data (which would need the full academic FK chain:
 * departments→subjects→courses→enrollments→submissions, unrelated to what this test actually
 * checks), but by querying Postgres's own privilege catalog directly via {@code
 * has_table_privilege}/{@code has_column_privilege}. A permission check happens before any row is
 * read, so an empty table is exactly as valid a subject as a populated one.
 *
 * <p>Runs api's own Flyway migrations against a Testcontainers Postgres via a {@code filesystem:}
 * location (module structure: {@code backend/execution-worker/..} → {@code
 * backend/api/src/main/resources/db/migration}) — this module deliberately does not depend on the
 * {@code api} artifact (ADR-001: separate deployable), so this is the one legitimate way to stand
 * up the real schema for a worker-side integration test.
 */
@Tag("it")
@Testcontainers(disabledWithoutDocker = true)
class RestrictedRoleGrantsIT {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("aifu_lms")
                    .withUsername("aifu_app")
                    .withPassword("it_only_password_not_used_anywhere_else");

    private static JdbcTemplate jdbc;
    private static Connection connection;

    @BeforeAll
    static void migrate() throws Exception {
        POSTGRES.start();
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("filesystem:../api/src/main/resources/db/migration")
                .load()
                .migrate();
        connection =
                DriverManager.getConnection(
                        POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
        POSTGRES.stop();
    }

    private static final String ROLE = "aifu_execution_worker";

    @Test
    void roleExists() {
        Boolean exists =
                jdbc.queryForObject(
                        "select exists(select 1 from pg_roles where rolname = ?)",
                        Boolean.class,
                        ROLE);
        assertThat(exists).isTrue();
    }

    @Test
    void canReadTheTablesNeededToRunOneSubmission() {
        for (String table :
                new String[] {
                    "submissions",
                    "coding_tasks",
                    "coding_task_test_cases",
                    "coding_task_languages",
                    "programming_languages"
                }) {
            assertThat(hasTablePrivilege(table, "SELECT")).as("SELECT on %s", table).isTrue();
        }
    }

    @Test
    void canFullyManageItsOwnExecutionTables() {
        for (String table :
                new String[] {"execution_jobs", "execution_results", "execution_test_results"}) {
            for (String priv : new String[] {"SELECT", "INSERT", "UPDATE", "DELETE"}) {
                assertThat(hasTablePrivilege(table, priv)).as("%s on %s", priv, table).isTrue();
            }
        }
    }

    @Test
    void canUpdateOnlyTheOutcomeColumnsOnSubmissions_neverSourceOrOwnership() {
        for (String column :
                new String[] {
                    "status",
                    "verdict",
                    "score",
                    "max_score",
                    "tests_passed",
                    "tests_total",
                    "completed_at",
                    "version"
                }) {
            assertThat(hasColumnPrivilege("submissions", column, "UPDATE"))
                    .as("UPDATE on submissions.%s", column)
                    .isTrue();
        }
        for (String column :
                new String[] {"source_code", "enrollment_id", "attempt_number", "coding_task_id"}) {
            assertThat(hasColumnPrivilege("submissions", column, "UPDATE"))
                    .as("UPDATE on submissions.%s must be DENIED", column)
                    .isFalse();
        }
        // Never granted INSERT/DELETE on submissions at all — the worker only ever updates a row
        // the API already created.
        assertThat(hasTablePrivilege("submissions", "INSERT")).isFalse();
        assertThat(hasTablePrivilege("submissions", "DELETE")).isFalse();
    }

    @Test
    void cannotSeeUsersTokensAuditOrGrades() {
        for (String table :
                new String[] {
                    "users",
                    "refresh_tokens",
                    "user_roles",
                    "audit_log",
                    "grade_entries",
                    "enrollments",
                    "quizzes",
                    "quiz_attempts",
                    "files",
                    "notifications"
                }) {
            assertThat(hasTablePrivilege(table, "SELECT"))
                    .as("SELECT on %s must be DENIED", table)
                    .isFalse();
        }
    }

    @Test
    void cannotWriteToTheTablesItMayOnlyRead() {
        for (String table :
                new String[] {
                    "submissions",
                    "coding_tasks",
                    "coding_task_test_cases",
                    "coding_task_languages",
                    "programming_languages"
                }) {
            assertThat(hasTablePrivilege(table, "INSERT"))
                    .as("INSERT on %s must be DENIED", table)
                    .isFalse();
            assertThat(hasTablePrivilege(table, "DELETE"))
                    .as("DELETE on %s must be DENIED", table)
                    .isFalse();
        }
    }

    @Test
    void ungradedRuns_canBeClaimedAndAnswered_butNeverCreatedDeletedOrRewritten() {
        assertThat(hasTablePrivilege("code_runs", "SELECT")).isTrue();
        for (String column :
                new String[] {
                    "status",
                    "attempt_count",
                    "lease_expires_at",
                    "verdict",
                    "stdout",
                    "stderr",
                    "compile_output",
                    "completed_at"
                }) {
            assertThat(hasColumnPrivilege("code_runs", column, "UPDATE"))
                    .as("UPDATE on code_runs.%s", column)
                    .isTrue();
        }
        for (String column :
                new String[] {
                    "user_id", "coding_task_id", "language_code", "mode", "source_code", "stdin"
                }) {
            assertThat(hasColumnPrivilege("code_runs", column, "UPDATE"))
                    .as("UPDATE on code_runs.%s must be DENIED", column)
                    .isFalse();
        }
        assertThat(hasTablePrivilege("code_runs", "INSERT")).isFalse();
        assertThat(hasTablePrivilege("code_runs", "DELETE")).isFalse();
        for (String priv : new String[] {"SELECT", "INSERT", "DELETE"}) {
            assertThat(hasTablePrivilege("code_run_test_results", priv)).isTrue();
        }
        // Which hints a student opened is none of the worker's business.
        assertThat(hasTablePrivilege("coding_task_hint_reveals", "SELECT")).isFalse();
    }

    private boolean hasTablePrivilege(String table, String privilege) {
        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        "select has_table_privilege(?, ?, ?)",
                        Boolean.class,
                        ROLE,
                        table,
                        privilege));
    }

    private boolean hasColumnPrivilege(String table, String column, String privilege) {
        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        "select has_column_privilege(?, ?, ?, ?)",
                        Boolean.class,
                        ROLE,
                        table,
                        column,
                        privilege));
    }
}
