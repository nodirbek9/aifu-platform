package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
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
import uz.aifu.lms.academics.api.dto.CourseDtos.CourseDetail;
import uz.aifu.lms.academics.api.dto.CourseDtos.CreateAssessmentComponentRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.CreateCourseRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.SetSessionAllocationRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.UpdateCourseDeliveryRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.UpsertCourseTranslationRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.CreateSubjectRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.SubjectDetail;
import uz.aifu.lms.academics.domain.AssessmentComponentKind;
import uz.aifu.lms.academics.domain.CourseStatus;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.academics.domain.SubjectKind;
import uz.aifu.lms.academics.domain.TeacherRole;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.AcademicFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 3 domain invariants (CLAUDE.md §8): assessment component weights must sum to the scheme
 * total, session-allocation hours must reconcile with {@code totalHours}, and a teacher may edit
 * only a course they are assigned to — all exercised end to end against real Postgres.
 */
class CourseDeliveryIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private AcademicFixtures academicFixtures;
    @LocalServerPort private int port;

    @Test
    void publish_assessmentWeightsDoNotSumTo100_isRejected() {
        String adminToken = adminToken();
        CourseDetail course = createCourse(adminToken, 100);
        addTranslation(adminToken, course.id());
        addComponent(adminToken, course.id(), "JN", 50);
        setAllocation(adminToken, course.id(), SessionType.LECTURE, 100);

        ResponseEntity<ApiError> response =
                changeStatus(adminToken, course.id(), CourseStatus.PUBLISHED, ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("ASSESSMENT_WEIGHTS_INVALID");
    }

    @Test
    void publish_sessionHoursDoNotReconcileWithTotalHours_isRejected() {
        String adminToken = adminToken();
        CourseDetail course = createCourse(adminToken, 100);
        addTranslation(adminToken, course.id());
        addComponent(adminToken, course.id(), "JN", 60);
        addComponent(adminToken, course.id(), "ON", 40);
        setAllocation(adminToken, course.id(), SessionType.LECTURE, 60);

        ResponseEntity<ApiError> response =
                changeStatus(adminToken, course.id(), CourseStatus.PUBLISHED, ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("PUBLICATION_VALIDATION_FAILED");
    }

    @Test
    void publish_whenTranslationWeightsAndHoursAllReconcile_succeeds() {
        String adminToken = adminToken();
        CourseDetail course = createCourse(adminToken, 100);
        addTranslation(adminToken, course.id());
        addComponent(adminToken, course.id(), "JN", 60);
        addComponent(adminToken, course.id(), "ON", 40);
        setAllocation(adminToken, course.id(), SessionType.LECTURE, 60);
        setAllocation(adminToken, course.id(), SessionType.SEMINAR, 40);

        ResponseEntity<CourseDetail> response =
                changeStatus(adminToken, course.id(), CourseStatus.PUBLISHED, CourseDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo(CourseStatus.PUBLISHED);
    }

    @Test
    void courseEditor_unassignedTeacher_isForbidden() {
        String adminToken = adminToken();
        CourseDetail course = createCourse(adminToken, 100);
        String outsiderToken = teacherToken();

        HttpHeaders headers = bearer(outsiderToken);
        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/admin/courses/" + course.id()),
                        HttpMethod.PATCH,
                        new HttpEntity<>(
                                new UpdateCourseDeliveryRequest(
                                        "SECTION-B", null, null, null, null),
                                headers),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().code()).isEqualTo("NOT_COURSE_TEACHER");
    }

    @Test
    void courseEditor_assignedTeacher_canEditTheirOwnCourse() {
        String adminToken = adminToken();
        CourseDetail course = createCourse(adminToken, 100);
        User teacher = userFixtures.activeUser(RoleCode.TEACHER);
        assignTeacher(adminToken, course.id(), teacher.getId());
        String teacherToken = login(teacher);

        HttpHeaders headers = bearer(teacherToken);
        ResponseEntity<CourseDetail> response =
                rest.exchange(
                        url("/api/v1/admin/courses/" + course.id()),
                        HttpMethod.PATCH,
                        new HttpEntity<>(
                                new UpdateCourseDeliveryRequest(
                                        "SECTION-A", null, null, null, null),
                                headers),
                        CourseDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().cohortCode()).isEqualTo("SECTION-A");
    }

    private CourseDetail createCourse(String adminToken, int totalHours) {
        HttpHeaders headers = bearer(adminToken);
        var department = academicFixtures.department();
        var term = academicFixtures.term();
        String subjectCode =
                "SUB"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 10)
                                .toUpperCase();
        ResponseEntity<SubjectDetail> subjectResponse =
                rest.exchange(
                        url("/api/v1/admin/subjects"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateSubjectRequest(
                                        subjectCode,
                                        department.getId(),
                                        SubjectKind.MANDATORY,
                                        "uz"),
                                headers),
                        SubjectDetail.class);
        UUID subjectId = subjectResponse.getBody().id();

        ResponseEntity<CourseDetail> courseResponse =
                rest.exchange(
                        url("/api/v1/admin/courses"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateCourseRequest(
                                        subjectId,
                                        term.getId(),
                                        null,
                                        BigDecimal.valueOf(6),
                                        totalHours,
                                        "uz",
                                        null),
                                headers),
                        CourseDetail.class);
        assertThat(courseResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return courseResponse.getBody();
    }

    private void addTranslation(String adminToken, UUID courseId) {
        HttpHeaders headers = bearer(adminToken);
        ResponseEntity<CourseDetail> response =
                rest.exchange(
                        url("/api/v1/admin/courses/" + courseId + "/translations/uz"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new UpsertCourseTranslationRequest("Diskret matematika", null),
                                headers),
                        CourseDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void addComponent(String adminToken, UUID courseId, String code, int maxPoints) {
        HttpHeaders headers = bearer(adminToken);
        ResponseEntity<CourseDetail> response =
                rest.exchange(
                        url("/api/v1/admin/courses/" + courseId + "/assessment-components"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateAssessmentComponentRequest(
                                        null,
                                        code,
                                        AssessmentComponentKind.CONTINUOUS,
                                        BigDecimal.valueOf(maxPoints),
                                        null,
                                        false,
                                        null,
                                        0),
                                headers),
                        CourseDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void setAllocation(
            String adminToken, UUID courseId, SessionType sessionType, int hours) {
        HttpHeaders headers = bearer(adminToken);
        ResponseEntity<CourseDetail> response =
                rest.exchange(
                        url("/api/v1/admin/courses/" + courseId + "/session-allocations"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new SetSessionAllocationRequest(sessionType, hours), headers),
                        CourseDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void assignTeacher(String adminToken, UUID courseId, UUID teacherUserId) {
        HttpHeaders headers = bearer(adminToken);
        ResponseEntity<CourseDetail> response =
                rest.exchange(
                        url("/api/v1/admin/courses/" + courseId + "/teachers"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new AssignTeacherRequest(teacherUserId, TeacherRole.LEAD), headers),
                        CourseDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private <T> ResponseEntity<T> changeStatus(
            String adminToken, UUID courseId, CourseStatus status, Class<T> responseType) {
        HttpHeaders headers = bearer(adminToken);
        return rest.exchange(
                url("/api/v1/admin/courses/" + courseId + "/status?status=" + status),
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                responseType);
    }

    private String adminToken() {
        return login(userFixtures.activeUser(RoleCode.ADMIN));
    }

    private String teacherToken() {
        return login(userFixtures.activeUser(RoleCode.TEACHER));
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
