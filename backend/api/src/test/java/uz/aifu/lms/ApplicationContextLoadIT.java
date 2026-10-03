package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 1 acceptance criteria (docs/planning/phase-plan.md): the app boots, all 15 Flyway
 * migrations apply against a real Postgres 16, health is UP, and an unknown path returns a
 * well-formed {@code ApiError} rather than a container error page.
 */
class ApplicationContextLoadIT extends AbstractIntegrationTest {

    @Autowired private ApplicationContext context;
    @Autowired private UserFixtures userFixtures;

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

    @Test
    void unknownPath_signedIn_isA404ApiError_notAServerError() {
        String token = login(userFixtures.activeUser(RoleCode.STUDENT));
        ResponseEntity<String> response =
                rest.exchange(
                        url("/api/v1/this-does-not-exist"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(token)),
                        String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("\"code\":\"RESOURCE_NOT_FOUND\"");
    }

    private String login(User user) {
        return rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class)
                .getBody()
                .accessToken();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
