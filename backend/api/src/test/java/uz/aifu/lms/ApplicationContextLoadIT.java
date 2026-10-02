package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.aifu.lms.support.AbstractIntegrationTest;

/**
 * Phase 1 acceptance criteria (docs/planning/phase-plan.md): the app boots, all 15 Flyway
 * migrations apply against a real Postgres 16, health is UP, and an unknown path returns a
 * well-formed {@code ApiError} rather than a container error page.
 */
class ApplicationContextLoadIT extends AbstractIntegrationTest {

    @Autowired private ApplicationContext context;

    @LocalServerPort private int port;

    @Test
    void contextLoads_migrationsApply_contextIsHealthy() {
        assertThat(context).isNotNull();
    }

    @Test
    void livenessProbe_isUp() {
        ResponseEntity<String> response =
                rest.getForEntity(
                        "http://localhost:" + port + "/actuator/health/liveness", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void readinessProbe_isUp() {
        ResponseEntity<String> response =
                rest.getForEntity(
                        "http://localhost:" + port + "/actuator/health/readiness", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void unknownPath_withoutAuthentication_returnsWellFormedApiError() {
        // SecurityConfig denies-by-default (anyRequest().authenticated()): an unauthenticated
        // caller
        // is rejected by the security filter chain before Spring MVC ever resolves a handler, so an
        // unknown path returns 401, not 404. Either way the body must be a well-formed ApiError,
        // never
        // a container error page or a leaked stack trace.
        ResponseEntity<String> response =
                rest.getForEntity(
                        "http://localhost:" + port + "/api/v1/this-does-not-exist", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString()).contains("application/json");
        assertThat(response.getBody()).contains("\"code\":\"AUTH_TOKEN_MISSING\"");
    }
}
