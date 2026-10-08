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
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.progress.api.dto.ProgressDtos.CoursePlanView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.LessonPathView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.PlanLesson;
import uz.aifu.lms.progress.app.PathRules.LockReason;
import uz.aifu.lms.progress.app.PathRules.StepState;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Read API of the learning path (Phase 5, B11): the course plan with the gate's lock, steps and
 * lock reason, and {@code GET /lessons/{id}/path}.
 */
@Tag("it")
class LessonPathReadApiIT extends AbstractIntegrationTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @LocalServerPort private int port;

    @Test
    void plan_forPathEnabledCourse_locksLesson2AndShowsLesson1Steps() {
        Scenario s = setup();

        ResponseEntity<CoursePlanView> response =
                rest.exchange(
                        url("/api/v1/courses/" + s.courseId + "/plan/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(s.studentToken)),
                        CoursePlanView.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CoursePlanView plan = response.getBody();
        assertThat(plan).isNotNull();
        PlanLesson l1 = lesson(plan, s.l1Id);
        PlanLesson l2 = lesson(plan, s.l2Id);
        assertThat(l1.locked()).isFalse();
        assertThat(l1.lockReason()).isNull();
        assertThat(l1.waived()).isFalse();
        assertThat(l1.steps()).isNotNull();
        assertThat(l1.steps().lecture()).isEqualTo(StepState.TODO);
        assertThat(l1.steps().test()).isEqualTo(StepState.TODO);
        assertThat(l1.steps().practice()).isEqualTo(StepState.TODO);
        assertThat(l2.locked()).isTrue();
        assertThat(l2.lockReason()).isEqualTo(LockReason.PREVIOUS_LESSON_INCOMPLETE);
        assertThat(l2.steps().test()).isEqualTo(StepState.NONE);
        assertThat(plan.midterm()).isNull();
    }

    @Test
    void lessonPath_forStudent_returnsStepsAndActivityIds() {
        Scenario s = setup();

        ResponseEntity<LessonPathView> response =
                rest.exchange(
                        url("/api/v1/lessons/" + s.l1Id + "/path"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(s.studentToken)),
                        LessonPathView.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LessonPathView view = response.getBody();
        assertThat(view).isNotNull();
        assertThat(view.open()).isTrue();
        assertThat(view.steps().lecture()).isEqualTo(StepState.TODO);
        assertThat(view.steps().test()).isEqualTo(StepState.TODO);
        assertThat(view.steps().practice()).isEqualTo(StepState.TODO);
        assertThat(view.testActivityId()).isEqualTo(s.testActivityId);
        assertThat(view.practiceActivityId()).isEqualTo(s.practiceActivityId);
        assertThat(view.lockReason()).isNull();
    }

    @Test
    void lessonPath_forLockedLesson_isAnsweredWithLockReason() {
        Scenario s = setup();

        ResponseEntity<LessonPathView> response =
                rest.exchange(
                        url("/api/v1/lessons/" + s.l2Id + "/path"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(s.studentToken)),
                        LessonPathView.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().open()).isFalse();
        assertThat(response.getBody().lockReason())
                .isEqualTo(LockReason.PREVIOUS_LESSON_INCOMPLETE);
    }

    @Test
    void lessonPath_forTeacher_isOpen() {
        Scenario s = setup();

        ResponseEntity<LessonPathView> response =
                rest.exchange(
                        url("/api/v1/lessons/" + s.l2Id + "/path"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(s.teacherToken)),
                        LessonPathView.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().open()).isTrue();
        assertThat(response.getBody().lockReason()).isNull();
    }

    @Test
    void lessonPath_forStudentOfAnotherCourse_isRefused() {
        Scenario s = setup();
        Course other = enrollmentFixtures.publishedCourse(150);
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        User outsider = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                other.getId(), outsider.getId(), EnrollmentSource.ADMIN, admin.getId(), null);

        ResponseEntity<String> response =
                rest.exchange(
                        url("/api/v1/lessons/" + s.l1Id + "/path"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(outsider))),
                        String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND);
    }

    // ---------------------------------------------------------------- scaffolding

    private record Scenario(
            UUID courseId,
            UUID l1Id,
            UUID l2Id,
            UUID testActivityId,
            UUID practiceActivityId,
            String studentToken,
            String teacherToken) {}

    private static PlanLesson lesson(CoursePlanView plan, UUID id) {
        return plan.modules().stream()
                .flatMap(m -> m.lessons().stream())
                .filter(l -> l.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private Scenario setup() {
        Course course = enrollmentFixtures.publishedCourse(150);
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        User teacher = userFixtures.activeUser(RoleCode.TEACHER);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
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
        UUID l1Id = insertLesson(moduleId, 0, "Dars 1");
        UUID l2Id = insertLesson(moduleId, 1, "Dars 2");

        UUID quizId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO quizzes (id, course_id, code, max_attempts, max_points, passing_percent, status, "
                        + "shuffle_questions, shuffle_options, reveal_policy, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, 2, 10, 60, 'PUBLISHED', false, false, 'AFTER_LAST_ATTEMPT', now(), now(), 0)",
                quizId,
                course.getId(),
                "QUIZ-" + UUID.randomUUID().toString().substring(0, 8));
        UUID taskId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO coding_tasks (id, course_id, code, status, max_points, scoring_strategy, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, 'PUBLISHED', 100, 'PER_TEST_WEIGHTED', now(), now(), 0)",
                taskId,
                course.getId(),
                "TASK-" + UUID.randomUUID().toString().substring(0, 8));
        UUID testActivityId =
                insertActivity(course.getId(), moduleId, l1Id, 0, "QUIZ", quizId, "TEST");
        UUID practiceActivityId =
                insertActivity(
                        course.getId(), moduleId, l1Id, 1, "CODING_TASK", taskId, "PRACTICE");

        jdbc.update(
                "INSERT INTO course_learning_paths (course_id, enabled, enabled_at, enabled_by, min_reading_fraction, created_at, updated_at, version) "
                        + "VALUES (?, true, now(), ?, 0.50, now(), now(), 0)",
                course.getId(),
                admin.getId());

        return new Scenario(
                course.getId(),
                l1Id,
                l2Id,
                testActivityId,
                practiceActivityId,
                login(student),
                login(teacher));
    }

    private UUID insertLesson(UUID moduleId, int position, String title) {
        UUID id = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO lessons (id, module_id, position, session_type, status, is_required, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, 'LECTURE', 'PUBLISHED', true, now(), now(), 0)",
                id,
                moduleId,
                position);
        jdbc.update(
                "INSERT INTO lesson_translations (lesson_id, locale, title) VALUES (?, 'uz', ?)",
                id,
                title);
        return id;
    }

    private UUID insertActivity(
            UUID courseId,
            UUID moduleId,
            UUID lessonId,
            int position,
            String type,
            UUID referenceId,
            String pathStep) {
        UUID id = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO activities (id, course_id, module_id, lesson_id, position, activity_type, reference_id, "
                        + "status, is_graded, collaboration_mode, variant_strategy, path_step, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, 'PUBLISHED', false, 'INDIVIDUAL', 'NONE', ?, now(), now(), 0)",
                id,
                courseId,
                moduleId,
                lessonId,
                position,
                type,
                referenceId,
                pathStep);
        return id;
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
