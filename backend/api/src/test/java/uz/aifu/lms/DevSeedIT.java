package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * The development seed ({@code db/seed}, V900+) on top of the current schema, in a database of its
 * own. Every other integration test runs without the seed, so nothing else notices when a schema
 * migration breaks it — and a broken seed means the demo database does not start.
 *
 * <p>Also holds the seed to the product's promise that every practice task can be solved in Python,
 * Java and C++ (spec S-05.2).
 */
@Tag("it")
@Testcontainers(disabledWithoutDocker = true)
class DevSeedIT {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("aifu_lms")
                    .withUsername("aifu_app")
                    .withPassword("it_only_password_not_used_anywhere_else");

    private static JdbcTemplate jdbc;

    @BeforeAll
    static void migrateWithSeed() {
        POSTGRES.start();
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration", "classpath:db/seed")
                .load()
                .migrate();
        jdbc =
                new JdbcTemplate(
                        new DriverManagerDataSource(
                                POSTGRES.getJdbcUrl(),
                                POSTGRES.getUsername(),
                                POSTGRES.getPassword()));
    }

    @AfterAll
    static void stop() {
        POSTGRES.stop();
    }

    @Test
    void seedApplies_andHasContentToShow() {
        assertThat(count("select count(*) from subjects")).isGreaterThanOrEqualTo(5);
        assertThat(count("select count(*) from lessons")).isGreaterThan(100);
        assertThat(count("select count(*) from content_blocks")).isGreaterThan(400);
        assertThat(count("select count(*) from coding_tasks where status = 'PUBLISHED'"))
                .isGreaterThanOrEqualTo(53);
    }

    @Test
    void everyPublishedTask_isSolvableInPythonJavaAndCpp() {
        List<String> incomplete =
                jdbc.queryForList(
                        """
                        select t.code from coding_tasks t
                        where t.status = 'PUBLISHED' and (
                            select count(*) from coding_task_languages l
                            where l.coding_task_id = t.id
                              and l.language_code in ('python', 'java', 'cpp')
                              and coalesce(l.starter_code, '') <> ''
                              and coalesce(l.solution_code, '') <> '') < 3
                        order by t.code
                        """,
                        String.class);
        assertThat(incomplete).as("tasks without a starter and solution in all three").isEmpty();
    }

    @Test
    void onlyTheThreeTeachingLanguagesAreOffered() {
        assertThat(
                        jdbc.queryForList(
                                "select code from programming_languages where is_enabled order by code",
                                String.class))
                .containsExactly("cpp", "java", "python");
    }

    @Test
    void seededStudentsBelongToRealGroups() {
        // V019 turned group codes into rows; a seeded profile with a code must point at one.
        assertThat(
                        count(
                                "select count(*) from student_profiles p where p.group_code is not null"
                                        + " and not exists (select 1 from student_groups g"
                                        + " where g.code = p.group_code)"))
                .isZero();
    }

    private static long count(String sql) {
        return jdbc.queryForObject(sql, Long.class);
    }
}
