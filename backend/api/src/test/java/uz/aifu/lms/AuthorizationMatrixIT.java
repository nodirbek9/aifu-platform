package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.api.dto.UserDtos.SetRolesRequest;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 2 acceptance criterion (docs/planning/phase-plan.md): a student token cannot reach any
 * {@code /admin} route. Exercises {@code /admin/users} end to end, real HTTP, real permission
 * grants from V003 — not a mocked {@code @PreAuthorize} check.
 */
class AuthorizationMatrixIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures fixtures;
    @LocalServerPort private int port;

    @Test
    void adminUsers_withoutAnyToken_isUnauthorized() {
        ResponseEntity<String> response =
                rest.exchange(url("/api/v1/admin/users"), HttpMethod.GET, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void adminUsers_withStudentToken_isForbidden() {
        ResponseEntity<String> response = getAdminUsersAs(RoleCode.STUDENT);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"code\":\"AUTH_FORBIDDEN\"");
    }

    @Test
    void adminUsers_withTeacherToken_isForbidden() {
        ResponseEntity<String> response = getAdminUsersAs(RoleCode.TEACHER);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminUsers_withAdminToken_isAllowed() {
        ResponseEntity<String> response = getAdminUsersAs(RoleCode.ADMIN);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void setRoles_withTeacherToken_isForbidden_evenThoughTeacherCanReachOtherAdminEndpoints() {
        // ROLE_MANAGE is strictly narrower than USER_MANAGE/USER_READ and granted to ADMIN only.
        User target = fixtures.activeUser(RoleCode.STUDENT);
        String teacherToken = tokenFor(fixtures.activeUser(RoleCode.TEACHER));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(teacherToken);
        ResponseEntity<String> response =
                rest.exchange(
                        url("/api/v1/admin/users/" + target.getId() + "/roles"),
                        HttpMethod.PUT,
                        new HttpEntity<>(new SetRolesRequest(java.util.Set.of("TEACHER")), headers),
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void me_isReachableByAnyAuthenticatedRole() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenFor(fixtures.activeUser(RoleCode.STUDENT)));
        ResponseEntity<String> response =
                rest.exchange(
                        url("/api/v1/auth/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ResponseEntity<String> getAdminUsersAs(String roleCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenFor(fixtures.activeUser(roleCode)));
        return rest.exchange(
                url("/api/v1/admin/users"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
    }

    private String tokenFor(User user) {
        ResponseEntity<TokenResponse> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class);
        return response.getBody().accessToken();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
