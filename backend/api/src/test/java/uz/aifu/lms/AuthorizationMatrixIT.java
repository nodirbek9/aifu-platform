package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ExceptionRequest;
import uz.aifu.lms.scheduling.domain.ExceptionKind;
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
    void adminEnrollmentsCrossUserFilter_withoutAnyToken_isUnauthorized() {
        UUID studentId = fixtures.activeUser(RoleCode.STUDENT).getId();
        for (String query :
                List.of(
                        "?userId=" + studentId,
                        "?courseId=" + UUID.randomUUID() + "&userId=" + studentId)) {
            ResponseEntity<String> response =
                    rest.exchange(
                            url("/api/v1/admin/enrollments" + query),
                            HttpMethod.GET,
                            null,
                            String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    void adminEnrollmentsCrossUserFilter_withStudentToken_isForbidden() {
        assertCrossUserFilterForbidden(RoleCode.STUDENT);
    }

    @Test
    void adminEnrollmentsCrossUserFilter_withTeacherToken_isForbidden() {
        assertCrossUserFilterForbidden(RoleCode.TEACHER);
    }

    @Test
    void adminEnrollmentsCrossUserFilter_withAdminToken_isAllowed() {
        ResponseEntity<String> response = getFilteredEnrollmentsAs(RoleCode.ADMIN);
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

    private void assertCrossUserFilterForbidden(String roleCode) {
        User target = fixtures.activeUser(RoleCode.STUDENT);
        UUID randomCourseId = UUID.randomUUID();
        for (String query :
                List.of(
                        "?userId=" + target.getId(),
                        "?courseId=" + randomCourseId + "&userId=" + target.getId())) {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokenFor(fixtures.activeUser(roleCode)));
            ResponseEntity<String> response =
                    rest.exchange(
                            url("/api/v1/admin/enrollments" + query),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).contains("\"code\":\"AUTH_FORBIDDEN\"");
        }
    }

    private ResponseEntity<String> getFilteredEnrollmentsAs(String roleCode) {
        User target = fixtures.activeUser(RoleCode.STUDENT);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenFor(fixtures.activeUser(roleCode)));
        return rest.exchange(
                url("/api/v1/admin/enrollments?userId=" + target.getId()),
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

    @Test
    void addTimetableException_withoutAnyToken_isUnauthorized() {
        ExceptionRequest request =
                new ExceptionRequest(
                        LocalDate.now(),
                        ExceptionKind.ADD,
                        null,
                        UUID.randomUUID(),
                        null,
                        null,
                        "Testing unauthorized");

        ResponseEntity<String> response =
                rest.exchange(
                        url("/api/v1/admin/timetable-entries/" + UUID.randomUUID() + "/exceptions"),
                        HttpMethod.POST,
                        new HttpEntity<>(request),
                        String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void addTimetableException_withTeacherToken_isForbidden() {
        String teacherToken = tokenFor(fixtures.activeUser(RoleCode.TEACHER));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(teacherToken);

        ExceptionRequest request =
                new ExceptionRequest(
                        LocalDate.now(),
                        ExceptionKind.ADD,
                        null,
                        UUID.randomUUID(),
                        null,
                        null,
                        "Testing authorization");

        ResponseEntity<String> response =
                rest.exchange(
                        url("/api/v1/admin/timetable-entries/" + UUID.randomUUID() + "/exceptions"),
                        HttpMethod.POST,
                        new HttpEntity<>(request, headers),
                        String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"code\":\"AUTH_FORBIDDEN\"");
    }

    @Test
    void addTimetableException_withStudentToken_isForbidden() {
        String studentToken = tokenFor(fixtures.activeUser(RoleCode.STUDENT));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(studentToken);

        ExceptionRequest request =
                new ExceptionRequest(
                        LocalDate.now(),
                        ExceptionKind.ADD,
                        null,
                        UUID.randomUUID(),
                        null,
                        null,
                        "Testing authorization");

        ResponseEntity<String> response =
                rest.exchange(
                        url("/api/v1/admin/timetable-entries/" + UUID.randomUUID() + "/exceptions"),
                        HttpMethod.POST,
                        new HttpEntity<>(request, headers),
                        String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"code\":\"AUTH_FORBIDDEN\"");
    }
}
