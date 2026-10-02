package uz.aifu.lms.support;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for every DB-backed integration test (skills/testing/SKILL.md).
 *
 * <p>One Postgres 16 container is started once per JVM and reused across all subclasses — this is
 * the cost of Testcontainers Flyway migration (15 files) paid once, not per test class.
 *
 * <p>Deliberately NOT {@code @Transactional}: with {@code webEnvironment = RANDOM_PORT}, an HTTP
 * call through {@code TestRestTemplate} is served by the embedded server on its own thread with its
 * own database connection/transaction. A test-method transaction here would never be visible to
 * that request — a fixture inserted "in" the test would look like it does not exist to the server,
 * which fails confusingly (e.g. a freshly created user gets "invalid credentials" on login, because
 * the row was never committed). Tests instead give fixtures unique identifiers (see {@code
 * support.fixtures}) so leftover rows in the shared container never collide.
 *
 * <p>Deliberately Postgres, never H2: this schema uses JSONB, generated {@code tsvector} columns,
 * partial unique indexes, {@code CHECK} constraints and (in the worker path) {@code FOR UPDATE SKIP
 * LOCKED} — none of which H2 emulates faithfully.
 */
@Tag("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractIntegrationTest {

    /**
     * Deliberately NOT the {@code @Autowired TestRestTemplate}. Both JDK HTTP clients were tried
     * and rejected here: the legacy {@code HttpURLConnection} (Boot's default) throws {@code
     * HttpRetryException: cannot retry due to server authentication, in streaming mode} on a POST
     * that gets a 401/403 back, and {@code java.net.http.HttpClient} reproducibly hangs forever on
     * a PUT reusing a keep-alive connection against the embedded Tomcat NIO connector — confirmed
     * by server logs showing the request fully processed while the client never returns from
     * reading the response. Apache HttpClient5 has neither issue and is what Spring Boot's own
     * {@code RestTemplateBuilder} auto-detection prefers first when present, for the same reason.
     */
    protected final TestRestTemplate rest =
            new TestRestTemplate(
                    new RestTemplateBuilder()
                            .requestFactory(() -> new HttpComponentsClientHttpRequestFactory()));

    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("aifu_lms")
                    .withUsername("aifu_app")
                    .withPassword("it_only_password_not_used_anywhere_else");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
