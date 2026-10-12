package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.CsvImportRowView;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.api.dto.RoleDtos.PermissionView;
import uz.aifu.lms.iam.api.dto.RoleDtos.RoleView;
import uz.aifu.lms.iam.api.dto.UserDtos.SetRolesRequest;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 10 (teacher & admin surfaces): the read-only role/permission catalogue, the audit browser,
 * and CSV roster import — all real HTTP against real Postgres.
 */
class AdminSurfacesIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @LocalServerPort private int port;

    @Test
    void listRoles_isAdminOnly_andIncludesGrantedPermissions() {
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        ResponseEntity<String> forbidden =
                rest.exchange(
                        url("/api/v1/admin/roles"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(student))),
                        String.class);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        ResponseEntity<RoleView[]> ok =
                rest.exchange(
                        url("/api/v1/admin/roles"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(admin))),
                        RoleView[].class);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<RoleView> roles = List.of(ok.getBody());
        assertThat(roles).extracting(RoleView::code).contains("ADMIN", "TEACHER", "STUDENT");
        RoleView adminRole =
                roles.stream().filter(r -> r.code().equals("ADMIN")).findFirst().orElseThrow();
        assertThat(adminRole.permissions()).contains("ROLE_MANAGE", "AUDIT_READ");
    }

    @Test
    void listPermissions_returnsTheSeededCatalogue() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        ResponseEntity<PermissionView[]> response =
                rest.exchange(
                        url("/api/v1/admin/permissions"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(admin))),
                        PermissionView[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(List.of(response.getBody()))
                .extracting(PermissionView::code)
                .contains("AUDIT_READ", "ROLE_MANAGE", "ENROLLMENT_MANAGE");
    }

    @Test
    void auditLog_isAdminOnly_andSurfacesARealRecordedAction() {
        User teacher = userFixtures.activeUser(RoleCode.TEACHER);
        ResponseEntity<String> forbidden =
                rest.exchange(
                        url("/api/v1/admin/audit-log"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(teacher))),
                        String.class);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin); // this login itself is an audited LOGIN_SUCCESS
        ResponseEntity<PageResponse> response =
                rest.exchange(
                        url("/api/v1/admin/audit-log?actorUserId=" + admin.getId()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        PageResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).isNotEmpty();
    }

    @Test
    void auditLog_namesTheActorOfAnAuthenticatedAction() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        User target = userFixtures.activeUser(RoleCode.STUDENT);
        HttpHeaders headers = bearer(login(admin));
        ResponseEntity<String> granted =
                rest.exchange(
                        url("/api/v1/admin/users/" + target.getId() + "/roles"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new SetRolesRequest(java.util.Set.of("STUDENT", "TEACHER")),
                                headers),
                        String.class);
        assertThat(granted.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> trail =
                rest.exchange(
                        url("/api/v1/admin/audit-log?actorUserId=" + admin.getId()),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);
        assertThat(trail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(trail.getBody()).contains("\"actorLabel\":\"" + admin.getUsername() + "\"");
    }

    @Test
    void csvImport_enrollsResolvableStudents_andReportsUnknownNumbersAsFailedRows() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        String studentNumber = "SN-" + java.util.UUID.randomUUID();
        userFixtures.activeStudentWithNumber(studentNumber);

        String body = "student_number\n" + studentNumber + "\nNO-SUCH-NUMBER-" + course.getId();

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add(
                "file",
                new ByteArrayResource(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
                    @Override
                    public String getFilename() {
                        return "roster.csv";
                    }
                });

        HttpHeaders headers = bearer(adminToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<CsvImportRowView[]> response =
                rest.exchange(
                        url("/api/v1/admin/enrollments/import?courseId=" + course.getId()),
                        HttpMethod.POST,
                        new HttpEntity<>(form, headers),
                        CsvImportRowView[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CsvImportRowView> rows = List.of(response.getBody());
        assertThat(rows).hasSize(2);
        assertThat(rows.stream().filter(CsvImportRowView::success)).hasSize(1);
        assertThat(rows.stream().filter(r -> !r.success())).hasSize(1);
        assertThat(rows.stream().filter(r -> !r.success()).findFirst().orElseThrow().errorCode())
                .isEqualTo("RESOURCE_NOT_FOUND");
    }

    private String login(User user) {
        ResponseEntity<TokenResponse> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class);
        return response.getBody().accessToken();
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
