package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.AttemptAllowanceView;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.AttemptDetail;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.StartAttemptRequest;
import uz.aifu.lms.assessment.api.dto.AttemptGrantDtos.AttemptGrantRequest;
import uz.aifu.lms.assessment.api.dto.AttemptGrantDtos.AttemptGrantView;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/** Integration tests for admin quiz attempt grants (V032, Phase 5). */
@Tag("it")
class AttemptGrantIT extends AbstractIntegrationTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @LocalServerPort private int port;

    @Test
    void grantAttempt_whileAttemptsRemain_returnsValidationFailed() {
        TestScenario scenario = setupScenario();

        ResponseEntity<ApiError> response =
                rest.exchange(
                        grantUrl(scenario.quizId, scenario.enrollment.getId()),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new AttemptGrantRequest("exam retake allowed"),
                                bearer(scenario.adminToken)),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void grantAttempt_afterAllAttemptsUsed_createsGrant() {
        TestScenario scenario = setupScenario();
        exhaustAttempts(scenario.studentToken, scenario.quizId);

        ResponseEntity<AttemptGrantView> response =
                rest.exchange(
                        grantUrl(scenario.quizId, scenario.enrollment.getId()),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new AttemptGrantRequest("exam retake allowed"),
                                bearer(scenario.adminToken)),
                        AttemptGrantView.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().grantedBy()).isEqualTo(scenario.admin.getId());
        assertThat(response.getBody().reason()).isEqualTo("exam retake allowed");
        assertThat(response.getBody().quizId()).isEqualTo(scenario.quizId);
        assertThat(response.getBody().enrollmentId()).isEqualTo(scenario.enrollment.getId());
    }

    @Test
    void studentCanStartThirdAttemptAfterGrant_andFourthAttemptRefusedWithAttemptLimitReached() {
        TestScenario scenario = setupScenario();
        exhaustAttempts(scenario.studentToken, scenario.quizId);

        ResponseEntity<AttemptGrantView> grantResponse =
                rest.exchange(
                        grantUrl(scenario.quizId, scenario.enrollment.getId()),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new AttemptGrantRequest("exam retake allowed"),
                                bearer(scenario.adminToken)),
                        AttemptGrantView.class);
        assertThat(grantResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<AttemptAllowanceView> allowance =
                rest.exchange(
                        url("/api/v1/quizzes/" + scenario.quizId + "/attempts/me/allowance"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.studentToken)),
                        AttemptAllowanceView.class);
        assertThat(allowance.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allowance.getBody()).isNotNull();
        assertThat(allowance.getBody().attemptsUsed()).isEqualTo(2);
        assertThat(allowance.getBody().attemptsAllowed()).isEqualTo(3);

        AttemptDetail thirdAttempt = startAttempt(scenario.studentToken, scenario.quizId);
        assertThat(thirdAttempt.attemptNumber()).isEqualTo(3);
        submit(scenario.studentToken, thirdAttempt.id());

        ResponseEntity<ApiError> fourthAttempt =
                rest.exchange(
                        url("/api/v1/quizzes/" + scenario.quizId + "/attempts"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new StartAttemptRequest(null, null), bearer(scenario.studentToken)),
                        ApiError.class);

        assertThat(fourthAttempt.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(fourthAttempt.getBody()).isNotNull();
        assertThat(fourthAttempt.getBody().code()).isEqualTo("ATTEMPT_LIMIT_REACHED");
    }

    @Test
    void secondGrant_forSameStudentAndQuiz_refusedWithInvalidStateTransition() {
        TestScenario scenario = setupScenario();
        exhaustAttempts(scenario.studentToken, scenario.quizId);

        ResponseEntity<AttemptGrantView> firstGrant =
                rest.exchange(
                        grantUrl(scenario.quizId, scenario.enrollment.getId()),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new AttemptGrantRequest("exam retake allowed"),
                                bearer(scenario.adminToken)),
                        AttemptGrantView.class);
        assertThat(firstGrant.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ApiError> secondGrant =
                rest.exchange(
                        grantUrl(scenario.quizId, scenario.enrollment.getId()),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new AttemptGrantRequest("second exam retake"),
                                bearer(scenario.adminToken)),
                        ApiError.class);

        assertThat(secondGrant.getStatusCode())
                .isIn(HttpStatus.CONFLICT, HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(secondGrant.getBody()).isNotNull();
        assertThat(secondGrant.getBody().code()).isEqualTo("INVALID_STATE_TRANSITION");
    }

    @Test
    void grantAttempt_withReasonShorterThanFiveCharacters_returnsValidationFailed() {
        TestScenario scenario = setupScenario();
        exhaustAttempts(scenario.studentToken, scenario.quizId);

        ResponseEntity<ApiError> response =
                rest.exchange(
                        grantUrl(scenario.quizId, scenario.enrollment.getId()),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new AttemptGrantRequest("bad"), bearer(scenario.adminToken)),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void studentCallingGrantEndpoint_returnsForbidden() {
        TestScenario scenario = setupScenario();
        exhaustAttempts(scenario.studentToken, scenario.quizId);

        ResponseEntity<ApiError> response =
                rest.exchange(
                        grantUrl(scenario.quizId, scenario.enrollment.getId()),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new AttemptGrantRequest("exam retake allowed"),
                                bearer(scenario.studentToken)),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("AUTH_FORBIDDEN");
    }

    @Test
    void listGrantsByCourseId_returnsGrantInPageEnvelope() {
        TestScenario scenario = setupScenario();
        exhaustAttempts(scenario.studentToken, scenario.quizId);

        ResponseEntity<AttemptGrantView> grantResponse =
                rest.exchange(
                        grantUrl(scenario.quizId, scenario.enrollment.getId()),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new AttemptGrantRequest("exam retake allowed"),
                                bearer(scenario.adminToken)),
                        AttemptGrantView.class);
        assertThat(grantResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID grantId = grantResponse.getBody().id();

        ResponseEntity<PageResponse<AttemptGrantView>> response =
                rest.exchange(
                        url("/api/v1/admin/attempt-grants?courseId=" + scenario.course.getId()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.adminToken)),
                        new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalElements()).isGreaterThanOrEqualTo(1);
        assertThat(response.getBody().page()).isZero();
        assertThat(response.getBody().size()).isPositive();
        assertThat(response.getBody().content()).extracting(AttemptGrantView::id).contains(grantId);
    }

    private TestScenario setupScenario() {
        Course course = enrollmentFixtures.publishedCourse(150);
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        User student = userFixtures.activeUser(RoleCode.STUDENT);

        Enrollment enrollment =
                enrollmentService.enroll(
                        course.getId(),
                        student.getId(),
                        EnrollmentSource.ADMIN,
                        admin.getId(),
                        null);

        UUID quizId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO quizzes (id, course_id, code, max_attempts, max_points, passing_percent, status, "
                        + "shuffle_questions, shuffle_options, reveal_policy, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, 2, 10, 60, 'PUBLISHED', false, false, 'AFTER_LAST_ATTEMPT', now(), now(), 0)",
                quizId,
                course.getId(),
                "QUIZ-" + UUID.randomUUID().toString().substring(0, 8));

        jdbc.update(
                "INSERT INTO quiz_translations (quiz_id, locale, title) "
                        + "VALUES (?, 'uz', 'Test Quiz')",
                quizId);

        UUID questionId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO questions (id, quiz_id, position, question_type, points, is_required, settings, created_at, updated_at, version) "
                        + "VALUES (?, ?, 0, 'SINGLE_CHOICE', 10, true, '{}', now(), now(), 0)",
                questionId,
                quizId);
        jdbc.update(
                "INSERT INTO question_translations (question_id, locale, prompt) "
                        + "VALUES (?, 'uz', 'Question 1')",
                questionId);

        UUID optionId1 = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO answer_options (id, question_id, position, is_correct) VALUES (?, ?, 0, true)",
                optionId1,
                questionId);
        jdbc.update(
                "INSERT INTO answer_option_translations (option_id, locale, text) VALUES (?, 'uz', 'Option 1')",
                optionId1);

        UUID optionId2 = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO answer_options (id, question_id, position, is_correct) VALUES (?, ?, 1, false)",
                optionId2,
                questionId);
        jdbc.update(
                "INSERT INTO answer_option_translations (option_id, locale, text) VALUES (?, 'uz', 'Option 2')",
                optionId2);

        String adminToken = login(admin);
        String studentToken = login(student);

        return new TestScenario(
                course, quizId, enrollment, admin, student, adminToken, studentToken);
    }

    private void exhaustAttempts(String studentToken, UUID quizId) {
        submit(studentToken, startAttempt(studentToken, quizId).id());
        submit(studentToken, startAttempt(studentToken, quizId).id());
    }

    private AttemptDetail startAttempt(String studentToken, UUID quizId) {
        ResponseEntity<AttemptDetail> response =
                rest.exchange(
                        url("/api/v1/quizzes/" + quizId + "/attempts"),
                        HttpMethod.POST,
                        new HttpEntity<>(new StartAttemptRequest(null, null), bearer(studentToken)),
                        AttemptDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private AttemptDetail submit(String studentToken, UUID attemptId) {
        ResponseEntity<AttemptDetail> response =
                rest.exchange(
                        url("/api/v1/attempts/" + attemptId + "/submit"),
                        HttpMethod.POST,
                        new HttpEntity<>(bearer(studentToken)),
                        AttemptDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private String login(User user) {
        ResponseEntity<TokenResponse> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().accessToken();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String grantUrl(UUID quizId, UUID enrollmentId) {
        return url(
                "/api/v1/admin/quizzes/"
                        + quizId
                        + "/enrollments/"
                        + enrollmentId
                        + "/attempt-grants");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private record TestScenario(
            Course course,
            UUID quizId,
            Enrollment enrollment,
            User admin,
            User student,
            String adminToken,
            String studentToken) {}
}
