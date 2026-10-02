package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
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
import uz.aifu.lms.academics.api.dto.CourseDtos.AssignTeacherRequest;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.TeacherRole;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.CreateEnrollmentRequest;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentDetail;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentSummary;
import uz.aifu.lms.enrollment.domain.EnrollmentStatus;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 4 domain invariants (CLAUDE.md §8, docs/planning/phase-plan.md): duplicate-active
 * rejection, withdraw-then-re-enroll history preservation, the full status transition matrix,
 * closed-course rejection, and teacher/self access scoping — all against real Postgres.
 */
class EnrollmentIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private CourseService courses;
    @LocalServerPort private int port;

    @Test
    void enroll_duplicateActive_isRejected() {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);

        assertThat(enroll(adminToken, course.getId(), student.getId()).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ApiError> conflict =
                rest.exchange(
                        url("/api/v1/admin/enrollments"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateEnrollmentRequest(course.getId(), student.getId(), null),
                                bearer(adminToken)),
                        ApiError.class);

        assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(conflict.getBody().code()).isEqualTo("ENROLLMENT_DUPLICATE");
    }

    @Test
    void enroll_closedCourse_isRejected() {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        courses.close(course.getId(), UUID.randomUUID(), null);
        User student = userFixtures.activeUser(RoleCode.STUDENT);

        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/admin/enrollments"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateEnrollmentRequest(course.getId(), student.getId(), null),
                                bearer(adminToken)),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("ENROLLMENT_CLOSED");
    }

    @Test
    void withdraw_thenReEnroll_bothRowsRetained() {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);

        EnrollmentDetail first = enroll(adminToken, course.getId(), student.getId()).getBody();
        assertThat(first.attemptNo()).isEqualTo(1);

        ResponseEntity<EnrollmentDetail> withdrawn =
                changeStatus(
                        adminToken,
                        first.id(),
                        EnrollmentStatus.WITHDRAWN,
                        "left the university",
                        EnrollmentDetail.class);
        assertThat(withdrawn.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(withdrawn.getBody().status()).isEqualTo(EnrollmentStatus.WITHDRAWN);

        EnrollmentDetail second = enroll(adminToken, course.getId(), student.getId()).getBody();
        assertThat(second.attemptNo()).isEqualTo(2);
        assertThat(second.id()).isNotEqualTo(first.id());

        List<EnrollmentSummary> roster = listByCourse(adminToken, course.getId());
        assertThat(roster)
                .hasSize(2)
                .extracting(EnrollmentSummary::attemptNo)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void statusTransitionMatrix() {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        EnrollmentDetail enrollment = enroll(adminToken, course.getId(), student.getId()).getBody();

        assertStatus(
                changeStatus(
                        adminToken,
                        enrollment.id(),
                        EnrollmentStatus.SUSPENDED,
                        null,
                        EnrollmentDetail.class),
                EnrollmentStatus.SUSPENDED);
        assertStatus(
                changeStatus(
                        adminToken,
                        enrollment.id(),
                        EnrollmentStatus.ACTIVE,
                        null,
                        EnrollmentDetail.class),
                EnrollmentStatus.ACTIVE);
        assertStatus(
                changeStatus(
                        adminToken,
                        enrollment.id(),
                        EnrollmentStatus.WITHDRAWN,
                        "changed major",
                        EnrollmentDetail.class),
                EnrollmentStatus.WITHDRAWN);
        assertStatus(
                changeStatus(
                        adminToken,
                        enrollment.id(),
                        EnrollmentStatus.ACTIVE,
                        "correcting a mistaken withdrawal",
                        EnrollmentDetail.class),
                EnrollmentStatus.ACTIVE);
        assertStatus(
                changeStatus(
                        adminToken,
                        enrollment.id(),
                        EnrollmentStatus.COMPLETED,
                        null,
                        EnrollmentDetail.class),
                EnrollmentStatus.COMPLETED);

        // COMPLETED is genuinely terminal: not even an admin "reinstate" may move it.
        ResponseEntity<ApiError> illegal =
                changeStatus(
                        adminToken,
                        enrollment.id(),
                        EnrollmentStatus.WITHDRAWN,
                        null,
                        ApiError.class);
        assertThat(illegal.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(illegal.getBody().code()).isEqualTo("ENROLLMENT_INVALID_TRANSITION");
    }

    @Test
    void events_recordEveryTransition() {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        EnrollmentDetail enrollment = enroll(adminToken, course.getId(), student.getId()).getBody();
        changeStatus(
                adminToken,
                enrollment.id(),
                EnrollmentStatus.SUSPENDED,
                "medical leave",
                EnrollmentDetail.class);

        ResponseEntity<EnrollmentDetail> detail =
                rest.exchange(
                        url("/api/v1/admin/enrollments/" + enrollment.id()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        EnrollmentDetail.class);

        assertThat(detail.getBody().events()).hasSize(2);
        assertThat(detail.getBody().events().get(0).eventType().name()).isEqualTo("CREATED");
        assertThat(detail.getBody().events().get(1).eventType().name()).isEqualTo("SUSPENDED");
        assertThat(detail.getBody().events().get(1).reason()).isEqualTo("medical leave");
    }

    @Test
    void teacherRoster_onlyTheAssignedTeacherCanRead() {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enroll(adminToken, course.getId(), student.getId());

        User assignedTeacher = userFixtures.activeUser(RoleCode.TEACHER);
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        courses.assignTeacher(
                course.getId(),
                new AssignTeacherRequest(assignedTeacher.getId(), TeacherRole.LEAD),
                admin.getId(),
                null);

        ResponseEntity<PageResponse<EnrollmentSummary>> ok =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + course.getId() + "/students"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(assignedTeacher))),
                        new org.springframework.core.ParameterizedTypeReference<>() {});
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ok.getBody().content()).hasSize(1);

        User outsiderTeacher = userFixtures.activeUser(RoleCode.TEACHER);
        ResponseEntity<ApiError> forbidden =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + course.getId() + "/students"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(outsiderTeacher))),
                        ApiError.class);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(forbidden.getBody().code()).isEqualTo("NOT_COURSE_TEACHER");
    }

    @Test
    void myEnrollments_returnsOnlyTheCallersOwnRecords() {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        User studentA = userFixtures.activeUser(RoleCode.STUDENT);
        User studentB = userFixtures.activeUser(RoleCode.STUDENT);
        enroll(adminToken, course.getId(), studentA.getId());

        ResponseEntity<PageResponse<EnrollmentSummary>> asA =
                rest.exchange(
                        url("/api/v1/enrollments/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(studentA))),
                        new org.springframework.core.ParameterizedTypeReference<>() {});
        assertThat(asA.getBody().content()).hasSize(1);
        assertThat(asA.getBody().content().get(0).studentUserId()).isEqualTo(studentA.getId());

        ResponseEntity<PageResponse<EnrollmentSummary>> asB =
                rest.exchange(
                        url("/api/v1/enrollments/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(studentB))),
                        new org.springframework.core.ParameterizedTypeReference<>() {});
        assertThat(asB.getBody().content()).isEmpty();
    }

    private void assertStatus(
            ResponseEntity<EnrollmentDetail> response, EnrollmentStatus expected) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo(expected);
    }

    private ResponseEntity<EnrollmentDetail> enroll(
            String adminToken, UUID courseId, UUID studentUserId) {
        return rest.exchange(
                url("/api/v1/admin/enrollments"),
                HttpMethod.POST,
                new HttpEntity<>(
                        new CreateEnrollmentRequest(courseId, studentUserId, null),
                        bearer(adminToken)),
                EnrollmentDetail.class);
    }

    private <T> ResponseEntity<T> changeStatus(
            String adminToken,
            UUID id,
            EnrollmentStatus status,
            String reason,
            Class<T> responseType) {
        URI uri =
                org.springframework.web.util.UriComponentsBuilder.fromUriString(
                                url("/api/v1/admin/enrollments/" + id + "/status"))
                        .queryParam("status", status)
                        .queryParamIfPresent("reason", java.util.Optional.ofNullable(reason))
                        .build()
                        .toUri();
        return rest.exchange(
                uri, HttpMethod.PUT, new HttpEntity<>(bearer(adminToken)), responseType);
    }

    private List<EnrollmentSummary> listByCourse(String adminToken, UUID courseId) {
        ResponseEntity<PageResponse<EnrollmentSummary>> response =
                rest.exchange(
                        url("/api/v1/admin/enrollments?courseId=" + courseId),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        new org.springframework.core.ParameterizedTypeReference<>() {});
        return response.getBody().content();
    }

    private String adminToken() {
        return login(userFixtures.activeUser(RoleCode.ADMIN));
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
