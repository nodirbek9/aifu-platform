package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.StartAttemptRequest;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonDetail;
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

/**
 * Integration tests for the server-side learning path gate (ADR-015, Phase 5).
 *
 * <p>Verifies lesson gating, quiz attempt gating, teacher bypass, disabled path bypass,
 * independent-study lesson bypass, and unlocking upon prerequisite completion against real
 * PostgreSQL.
 */
@Tag("it")
class LearningPathGateIT extends AbstractIntegrationTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @LocalServerPort private int port;

    @Test
    void studentReadingL2_beforeFinishingL1_returns422WithLessonPrerequisiteNotMet() {
        TestScenario scenario = setupScenario(true);

        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/lessons/" + scenario.l2Id),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.studentToken)),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("LESSON_PREREQUISITE_NOT_MET");
        assertThat(response.getBody().details())
                .containsEntry("reason", "PREVIOUS_LESSON_INCOMPLETE")
                .containsEntry("blockingLessonId", scenario.l1Id.toString());
    }

    @Test
    void studentReadingL1_firstLessonInChain_returns200() {
        TestScenario scenario = setupScenario(true);

        ResponseEntity<LessonDetail> response =
                rest.exchange(
                        url("/api/v1/lessons/" + scenario.l1Id),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.studentToken)),
                        LessonDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(scenario.l1Id);
    }

    @Test
    void studentStartingL2QuizAttempt_beforeFinishingL1_returns422LessonPrerequisiteNotMet() {
        TestScenario scenario = setupScenario(true);

        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/quizzes/" + scenario.l2QuizId + "/attempts"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new StartAttemptRequest(null, scenario.l2QuizActivityId),
                                bearer(scenario.studentToken)),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("LESSON_PREREQUISITE_NOT_MET");
        assertThat(response.getBody().details())
                .containsEntry("reason", "PREVIOUS_LESSON_INCOMPLETE");
    }

    @Test
    void teacherReadingL2_isNeverGated_returns200() {
        TestScenario scenario = setupScenario(true);

        ResponseEntity<LessonDetail> response =
                rest.exchange(
                        url("/api/v1/lessons/" + scenario.l2Id),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.teacherToken)),
                        LessonDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(scenario.l2Id);
    }

    @Test
    void courseWithPathDisabled_studentReadingL2_returns200() {
        TestScenario scenario = setupScenario(false);

        ResponseEntity<LessonDetail> response =
                rest.exchange(
                        url("/api/v1/lessons/" + scenario.l2Id),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.studentToken)),
                        LessonDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(scenario.l2Id);
    }

    @Test
    void independentStudyLessonPlacedAfterL2_isOpenForStudent_returns200() {
        TestScenario scenario = setupScenario(true);

        ResponseEntity<LessonDetail> response =
                rest.exchange(
                        url("/api/v1/lessons/" + scenario.l3IndependentId),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.studentToken)),
                        LessonDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(scenario.l3IndependentId);
    }

    @Test
    void afterL1LessonProgressCompleted_l2OpensForStudent_returns200() {
        TestScenario scenario = setupScenario(true);

        jdbc.update(
                """
                INSERT INTO lesson_progress (
                    id, enrollment_id, lesson_id, status, view_count, time_spent_seconds,
                    first_accessed_at, last_accessed_at, completed_at, read_completed_at,
                    active_reading_seconds, updated_at, version
                ) VALUES (gen_random_uuid(), ?, ?, 'COMPLETED', 1, 60, now(), now(), now(), now(), 60, now(), 0)
                """,
                scenario.enrollment.getId(),
                scenario.l1Id);

        ResponseEntity<LessonDetail> response =
                rest.exchange(
                        url("/api/v1/lessons/" + scenario.l2Id),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.studentToken)),
                        LessonDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(scenario.l2Id);
    }

    // ---------------------------------------------------------------- scaffolding

    private record TestScenario(
            Course course,
            UUID moduleId,
            UUID l1Id,
            UUID l2Id,
            UUID l3IndependentId,
            UUID l1QuizId,
            UUID l1TaskId,
            UUID l2QuizId,
            UUID l2QuizActivityId,
            Enrollment enrollment,
            User student,
            User teacher,
            String studentToken,
            String teacherToken) {}

    private TestScenario setupScenario(boolean pathEnabled) {
        Course course = enrollmentFixtures.publishedCourse(150);
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        User teacher = userFixtures.activeUser(RoleCode.TEACHER);

        Enrollment enrollment =
                enrollmentService.enroll(
                        course.getId(),
                        student.getId(),
                        EnrollmentSource.ADMIN,
                        admin.getId(),
                        null);

        jdbc.update(
                "INSERT INTO course_teachers (course_id, teacher_user_id, teacher_role, assigned_at, assigned_by) "
                        + "VALUES (?, ?, 'LEAD', now(), ?)",
                course.getId(),
                teacher.getId(),
                admin.getId());

        UUID moduleId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO course_modules (id, course_id, position, status, created_at, updated_at, version) "
                        + "VALUES (?, ?, 0, 'PUBLISHED', now(), now(), 0)",
                moduleId,
                course.getId());
        jdbc.update(
                "INSERT INTO course_module_translations (module_id, locale, title) VALUES (?, 'uz', 'Modul 1')",
                moduleId);

        UUID l1Id = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO lessons (id, module_id, position, session_type, status, is_required, created_at, updated_at, version) "
                        + "VALUES (?, ?, 0, 'LECTURE', 'PUBLISHED', true, now(), now(), 0)",
                l1Id,
                moduleId);
        jdbc.update(
                "INSERT INTO lesson_translations (lesson_id, locale, title) VALUES (?, 'uz', 'Dars 1')",
                l1Id);

        UUID l2Id = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO lessons (id, module_id, position, session_type, status, is_required, created_at, updated_at, version) "
                        + "VALUES (?, ?, 1, 'LECTURE', 'PUBLISHED', true, now(), now(), 0)",
                l2Id,
                moduleId);
        jdbc.update(
                "INSERT INTO lesson_translations (lesson_id, locale, title) VALUES (?, 'uz', 'Dars 2')",
                l2Id);

        UUID l3IndependentId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO lessons (id, module_id, position, session_type, status, is_required, created_at, updated_at, version) "
                        + "VALUES (?, ?, 2, 'INDEPENDENT_STUDY', 'PUBLISHED', true, now(), now(), 0)",
                l3IndependentId,
                moduleId);
        jdbc.update(
                "INSERT INTO lesson_translations (lesson_id, locale, title) VALUES (?, 'uz', 'Mustaqil talim')",
                l3IndependentId);

        UUID l1QuizId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO quizzes (id, course_id, code, max_attempts, max_points, passing_percent, status, "
                        + "shuffle_questions, shuffle_options, reveal_policy, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, 2, 10, 60, 'PUBLISHED', false, false, 'AFTER_LAST_ATTEMPT', now(), now(), 0)",
                l1QuizId,
                course.getId(),
                "QUIZ-" + UUID.randomUUID().toString().substring(0, 8));

        UUID l1TaskId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO coding_tasks (id, course_id, code, status, max_points, scoring_strategy, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, 'PUBLISHED', 100, 'PER_TEST_WEIGHTED', now(), now(), 0)",
                l1TaskId,
                course.getId(),
                "TASK-" + UUID.randomUUID().toString().substring(0, 8));

        UUID l2QuizId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO quizzes (id, course_id, code, max_attempts, max_points, passing_percent, status, "
                        + "shuffle_questions, shuffle_options, reveal_policy, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, 2, 10, 60, 'PUBLISHED', false, false, 'AFTER_LAST_ATTEMPT', now(), now(), 0)",
                l2QuizId,
                course.getId(),
                "QUIZ-" + UUID.randomUUID().toString().substring(0, 8));

        jdbc.update(
                "INSERT INTO activities (id, course_id, module_id, lesson_id, position, activity_type, reference_id, "
                        + "status, is_graded, collaboration_mode, variant_strategy, path_step, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, ?, 0, 'QUIZ', ?, 'PUBLISHED', false, 'INDIVIDUAL', 'NONE', 'TEST', now(), now(), 0)",
                UUID.randomUUID(),
                course.getId(),
                moduleId,
                l1Id,
                l1QuizId);

        jdbc.update(
                "INSERT INTO activities (id, course_id, module_id, lesson_id, position, activity_type, reference_id, "
                        + "status, is_graded, collaboration_mode, variant_strategy, path_step, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, ?, 1, 'CODING_TASK', ?, 'PUBLISHED', false, 'INDIVIDUAL', 'NONE', 'PRACTICE', now(), now(), 0)",
                UUID.randomUUID(),
                course.getId(),
                moduleId,
                l1Id,
                l1TaskId);

        UUID l2QuizActivityId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO activities (id, course_id, module_id, lesson_id, position, activity_type, reference_id, "
                        + "status, is_graded, collaboration_mode, variant_strategy, path_step, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, ?, 0, 'QUIZ', ?, 'PUBLISHED', false, 'INDIVIDUAL', 'NONE', 'TEST', now(), now(), 0)",
                l2QuizActivityId,
                course.getId(),
                moduleId,
                l2Id,
                l2QuizId);

        jdbc.update(
                "INSERT INTO course_learning_paths (course_id, enabled, enabled_at, enabled_by, min_reading_fraction, created_at, updated_at, version) "
                        + "VALUES (?, ?, now(), ?, 0.50, now(), now(), 0)",
                course.getId(),
                pathEnabled,
                admin.getId());

        String studentToken = login(student);
        String teacherToken = login(teacher);

        return new TestScenario(
                course,
                moduleId,
                l1Id,
                l2Id,
                l3IndependentId,
                l1QuizId,
                l1TaskId,
                l2QuizId,
                l2QuizActivityId,
                enrollment,
                student,
                teacher,
                studentToken,
                teacherToken);
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

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
