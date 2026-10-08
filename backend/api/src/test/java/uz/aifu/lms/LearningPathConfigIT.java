package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
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
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonDetail;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.progress.api.dto.LearningPathDtos.LearningPathConfigRequest;
import uz.aifu.lms.progress.api.dto.LearningPathDtos.LearningPathConfigView;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Integration tests for learning path configuration and rollout waivers (Phase 5, B10, ADR-017,
 * ADR-018).
 */
@Tag("it")
class LearningPathConfigIT extends AbstractIntegrationTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @LocalServerPort private int port;

    @Test
    void studentWhoOpenedL5_keepsL1Through5OpenAfterEnable_andL6LockedUntilL5Done() {
        TestScenario scenario = setupScenario();

        // Student has opened lesson 5 (status IN_PROGRESS)
        UUID l5Id = scenario.lessonIds.get(4);
        UUID l6Id = scenario.lessonIds.get(5);
        jdbc.update(
                """
                INSERT INTO lesson_progress (
                    id, enrollment_id, lesson_id, status, view_count, time_spent_seconds,
                    first_accessed_at, last_accessed_at,
                    active_reading_seconds, updated_at, version
                ) VALUES (gen_random_uuid(), ?, ?, 'IN_PROGRESS', 1, 30, now(), now(), 30, now(), 0)
                """,
                scenario.enrollment.getId(),
                l5Id);

        // Admin enables learning path
        ResponseEntity<LearningPathConfigView> putResp =
                rest.exchange(
                        configUrl(scenario.course.getId()),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new LearningPathConfigRequest(true, null, null, null),
                                bearer(scenario.adminToken)),
                        LearningPathConfigView.class);
        assertThat(putResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResp.getBody()).isNotNull();
        assertThat(putResp.getBody().enabled()).isTrue();
        assertThat(putResp.getBody().enabledAt()).isNotNull();
        assertThat(putResp.getBody().enabledBy()).isEqualTo(scenario.admin.getId());

        // Student can access lessons 1 through 5
        for (int i = 0; i < 5; i++) {
            UUID lessonId = scenario.lessonIds.get(i);
            ResponseEntity<LessonDetail> getResp =
                    rest.exchange(
                            url("/api/v1/lessons/" + lessonId),
                            HttpMethod.GET,
                            new HttpEntity<>(bearer(scenario.studentToken)),
                            LessonDetail.class);
            assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResp.getBody()).isNotNull();
            assertThat(getResp.getBody().id()).isEqualTo(lessonId);
        }

        // Lesson 6 is locked with LESSON_PREREQUISITE_NOT_MET because Lesson 5 is incomplete
        ResponseEntity<ApiError> getL6Resp =
                rest.exchange(
                        url("/api/v1/lessons/" + l6Id),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.studentToken)),
                        ApiError.class);
        assertThat(getL6Resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(getL6Resp.getBody()).isNotNull();
        assertThat(getL6Resp.getBody().code()).isEqualTo("LESSON_PREREQUISITE_NOT_MET");
        assertThat(getL6Resp.getBody().details())
                .containsEntry("reason", "PREVIOUS_LESSON_INCOMPLETE")
                .containsEntry("blockingLessonId", l5Id.toString());

        // Complete lesson 5
        jdbc.update(
                """
                UPDATE lesson_progress
                SET status = 'COMPLETED', completed_at = now()
                WHERE enrollment_id = ? AND lesson_id = ?
                """,
                scenario.enrollment.getId(),
                l5Id);

        // Now lesson 6 opens
        ResponseEntity<LessonDetail> getL6AfterResp =
                rest.exchange(
                        url("/api/v1/lessons/" + l6Id),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.studentToken)),
                        LessonDetail.class);
        assertThat(getL6AfterResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getL6AfterResp.getBody()).isNotNull();
        assertThat(getL6AfterResp.getBody().id()).isEqualTo(l6Id);
    }

    @Test
    void enablingTwice_doesNotWaiveMore() {
        TestScenario scenario = setupScenario();

        // Student opened lesson 5
        UUID l5Id = scenario.lessonIds.get(4);
        UUID l6Id = scenario.lessonIds.get(5);
        jdbc.update(
                """
                INSERT INTO lesson_progress (
                    id, enrollment_id, lesson_id, status, view_count, time_spent_seconds,
                    first_accessed_at, last_accessed_at,
                    active_reading_seconds, updated_at, version
                ) VALUES (gen_random_uuid(), ?, ?, 'IN_PROGRESS', 1, 30, now(), now(), 30, now(), 0)
                """,
                scenario.enrollment.getId(),
                l5Id);

        // First enable
        ResponseEntity<LearningPathConfigView> firstEnable =
                rest.exchange(
                        configUrl(scenario.course.getId()),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new LearningPathConfigRequest(true, null, null, null),
                                bearer(scenario.adminToken)),
                        LearningPathConfigView.class);
        assertThat(firstEnable.getStatusCode()).isEqualTo(HttpStatus.OK);

        Integer waiverCount1 =
                jdbc.queryForObject(
                        "SELECT count(*) FROM lesson_progress WHERE enrollment_id = ? AND path_waived_at IS NOT NULL",
                        Integer.class,
                        scenario.enrollment.getId());
        assertThat(waiverCount1).isEqualTo(4);

        // Student opens lesson 6
        jdbc.update(
                """
                INSERT INTO lesson_progress (
                    id, enrollment_id, lesson_id, status, view_count, time_spent_seconds,
                    first_accessed_at, last_accessed_at,
                    active_reading_seconds, updated_at, version
                ) VALUES (gen_random_uuid(), ?, ?, 'IN_PROGRESS', 1, 10, now(), now(), 10, now(), 0)
                """,
                scenario.enrollment.getId(),
                l6Id);

        // Second enable call (while still enabled)
        ResponseEntity<LearningPathConfigView> secondEnable =
                rest.exchange(
                        configUrl(scenario.course.getId()),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new LearningPathConfigRequest(true, null, null, null),
                                bearer(scenario.adminToken)),
                        LearningPathConfigView.class);
        assertThat(secondEnable.getStatusCode()).isEqualTo(HttpStatus.OK);

        Integer waiverCount2 =
                jdbc.queryForObject(
                        "SELECT count(*) FROM lesson_progress WHERE enrollment_id = ? AND path_waived_at IS NOT NULL",
                        Integer.class,
                        scenario.enrollment.getId());
        assertThat(waiverCount2).isEqualTo(4);

        // Disable then re-enable
        rest.exchange(
                configUrl(scenario.course.getId()),
                HttpMethod.PUT,
                new HttpEntity<>(
                        new LearningPathConfigRequest(false, null, null, null),
                        bearer(scenario.adminToken)),
                LearningPathConfigView.class);

        rest.exchange(
                configUrl(scenario.course.getId()),
                HttpMethod.PUT,
                new HttpEntity<>(
                        new LearningPathConfigRequest(true, null, null, null),
                        bearer(scenario.adminToken)),
                LearningPathConfigView.class);

        Integer waiverCount3 =
                jdbc.queryForObject(
                        "SELECT count(*) FROM lesson_progress WHERE enrollment_id = ? AND path_waived_at IS NOT NULL",
                        Integer.class,
                        scenario.enrollment.getId());
        assertThat(waiverCount3).isEqualTo(4);
    }

    @Test
    void defaultMidtermOpening_isMonday00AsiaTashkentInWeek8() {
        TestScenario scenario = setupScenario();

        UUID midtermQuizId = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO quizzes (id, course_id, code, max_attempts, max_points, passing_percent, status,
                    shuffle_questions, shuffle_options, reveal_policy, created_at, updated_at, version)
                VALUES (?, ?, ?, 2, 100, 60, 'PUBLISHED', false, false, 'AFTER_LAST_ATTEMPT', now(), now(), 0)
                """,
                midtermQuizId,
                scenario.course.getId(),
                "QUIZ-" + UUID.randomUUID().toString().substring(0, 8));
        jdbc.update(
                "INSERT INTO quiz_translations (quiz_id, locale, title) VALUES (?, 'uz', 'Oraliq Imtihon')",
                midtermQuizId);

        UUID anchorLessonId = scenario.lessonIds.get(2);
        UUID midtermActivityId = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO activities (id, course_id, module_id, lesson_id, position, activity_type, reference_id,
                    status, is_graded, collaboration_mode, variant_strategy, path_step, created_at, updated_at, version)
                VALUES (?, ?, ?, ?, 0, 'QUIZ', ?, 'PUBLISHED', false, 'INDIVIDUAL', 'NONE', 'MIDTERM', now(), now(), 0)
                """,
                midtermActivityId,
                scenario.course.getId(),
                scenario.moduleId,
                anchorLessonId,
                midtermQuizId);

        // PUT with midtermOpensAt = null computes week 8 Monday 00:00 Asia/Tashkent
        ResponseEntity<LearningPathConfigView> response =
                rest.exchange(
                        configUrl(scenario.course.getId()),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new LearningPathConfigRequest(
                                        true, midtermActivityId, anchorLessonId, null),
                                bearer(scenario.adminToken)),
                        LearningPathConfigView.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().midtermActivityId()).isEqualTo(midtermActivityId);
        assertThat(response.getBody().midtermAfterLessonId()).isEqualTo(anchorLessonId);
        assertThat(response.getBody().midtermOpensAt()).isNotNull();

        ZonedDateTime tashkentTime =
                response.getBody().midtermOpensAt().atZone(ZoneId.of("Asia/Tashkent"));
        assertThat(tashkentTime.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(tashkentTime.getHour()).isZero();
        assertThat(tashkentTime.getMinute()).isZero();
        assertThat(tashkentTime.getSecond()).isZero();

        LocalDate termStartsOn = scenario.course.getTerm().getStartsOn();
        LocalDate week1Monday =
                termStartsOn.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate expectedWeek8Monday = week1Monday.plusWeeks(7);
        assertThat(tashkentTime.toLocalDate()).isEqualTo(expectedWeek8Monday);

        Timestamp activityAvailableFrom =
                jdbc.queryForObject(
                        "SELECT available_from FROM activities WHERE id = ?",
                        Timestamp.class,
                        midtermActivityId);
        assertThat(activityAvailableFrom).isNotNull();
        assertThat(activityAvailableFrom.toInstant())
                .isEqualTo(response.getBody().midtermOpensAt());
    }

    @Test
    void teacherOrStudentCallingPut_returns403() {
        TestScenario scenario = setupScenario();

        ResponseEntity<ApiError> teacherResp =
                rest.exchange(
                        configUrl(scenario.course.getId()),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new LearningPathConfigRequest(true, null, null, null),
                                bearer(scenario.teacherToken)),
                        ApiError.class);
        assertThat(teacherResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(teacherResp.getBody()).isNotNull();
        assertThat(teacherResp.getBody().code()).isEqualTo("AUTH_FORBIDDEN");

        ResponseEntity<ApiError> studentResp =
                rest.exchange(
                        configUrl(scenario.course.getId()),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new LearningPathConfigRequest(true, null, null, null),
                                bearer(scenario.studentToken)),
                        ApiError.class);
        assertThat(studentResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(studentResp.getBody()).isNotNull();
        assertThat(studentResp.getBody().code()).isEqualTo("AUTH_FORBIDDEN");
    }

    @Test
    void invalidMidtermActivity_returnsValidationFailed() {
        TestScenario scenario = setupScenario();

        // 1. Activity has path_step TEST rather than MIDTERM
        UUID testQuizId = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO quizzes (id, course_id, code, max_attempts, max_points, passing_percent, status,
                    shuffle_questions, shuffle_options, reveal_policy, created_at, updated_at, version)
                VALUES (?, ?, ?, 2, 100, 60, 'PUBLISHED', false, false, 'AFTER_LAST_ATTEMPT', now(), now(), 0)
                """,
                testQuizId,
                scenario.course.getId(),
                "QUIZ-" + UUID.randomUUID().toString().substring(0, 8));
        jdbc.update(
                "INSERT INTO quiz_translations (quiz_id, locale, title) VALUES (?, 'uz', 'Test Quiz')",
                testQuizId);

        UUID nonMidtermActivityId = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO activities (id, course_id, module_id, lesson_id, position, activity_type, reference_id,
                    status, is_graded, collaboration_mode, variant_strategy, path_step, created_at, updated_at, version)
                VALUES (?, ?, ?, ?, 0, 'QUIZ', ?, 'PUBLISHED', false, 'INDIVIDUAL', 'NONE', 'TEST', now(), now(), 0)
                """,
                nonMidtermActivityId,
                scenario.course.getId(),
                scenario.moduleId,
                scenario.lessonIds.get(0),
                testQuizId);

        ResponseEntity<ApiError> notMidtermResp =
                rest.exchange(
                        configUrl(scenario.course.getId()),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new LearningPathConfigRequest(
                                        true,
                                        nonMidtermActivityId,
                                        scenario.lessonIds.get(0),
                                        null),
                                bearer(scenario.adminToken)),
                        ApiError.class);
        assertThat(notMidtermResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(notMidtermResp.getBody()).isNotNull();
        assertThat(notMidtermResp.getBody().code()).isEqualTo("VALIDATION_FAILED");

        // 2. Activity belongs to another course
        Course anotherCourse = enrollmentFixtures.publishedCourse(150);
        UUID anotherModuleId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO course_modules (id, course_id, position, status, created_at, updated_at, version) "
                        + "VALUES (?, ?, 0, 'PUBLISHED', now(), now(), 0)",
                anotherModuleId,
                anotherCourse.getId());
        jdbc.update(
                "INSERT INTO course_module_translations (module_id, locale, title) VALUES (?, 'uz', 'Modul X')",
                anotherModuleId);

        UUID anotherQuizId = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO quizzes (id, course_id, code, max_attempts, max_points, passing_percent, status,
                    shuffle_questions, shuffle_options, reveal_policy, created_at, updated_at, version)
                VALUES (?, ?, ?, 2, 100, 60, 'PUBLISHED', false, false, 'AFTER_LAST_ATTEMPT', now(), now(), 0)
                """,
                anotherQuizId,
                anotherCourse.getId(),
                "QUIZ-" + UUID.randomUUID().toString().substring(0, 8));
        jdbc.update(
                "INSERT INTO quiz_translations (quiz_id, locale, title) VALUES (?, 'uz', 'Other Quiz')",
                anotherQuizId);

        UUID foreignActivityId = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO activities (id, course_id, module_id, lesson_id, position, activity_type, reference_id,
                    status, is_graded, collaboration_mode, variant_strategy, path_step, created_at, updated_at, version)
                VALUES (?, ?, ?, null, 0, 'QUIZ', ?, 'PUBLISHED', false, 'INDIVIDUAL', 'NONE', 'MIDTERM', now(), now(), 0)
                """,
                foreignActivityId,
                anotherCourse.getId(),
                anotherModuleId,
                anotherQuizId);

        ResponseEntity<ApiError> foreignResp =
                rest.exchange(
                        configUrl(scenario.course.getId()),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new LearningPathConfigRequest(
                                        true, foreignActivityId, scenario.lessonIds.get(0), null),
                                bearer(scenario.adminToken)),
                        ApiError.class);
        assertThat(foreignResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(foreignResp.getBody()).isNotNull();
        assertThat(foreignResp.getBody().code()).isEqualTo("VALIDATION_FAILED");

        // 3. Anchor lesson provided without activity
        ResponseEntity<ApiError> missingActResp =
                rest.exchange(
                        configUrl(scenario.course.getId()),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new LearningPathConfigRequest(
                                        true, null, scenario.lessonIds.get(0), null),
                                bearer(scenario.adminToken)),
                        ApiError.class);
        assertThat(missingActResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(missingActResp.getBody()).isNotNull();
        assertThat(missingActResp.getBody().code()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void getLearningPathConfig_returnsConfig() {
        TestScenario scenario = setupScenario();

        ResponseEntity<LearningPathConfigView> response =
                rest.exchange(
                        configUrl(scenario.course.getId()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.adminToken)),
                        LearningPathConfigView.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().enabled()).isFalse();
        assertThat(response.getBody().minReadingFraction()).isNotNull();
    }

    // ---------------------------------------------------------------- scaffolding

    private record TestScenario(
            Course course,
            UUID moduleId,
            List<UUID> lessonIds,
            Enrollment enrollment,
            User admin,
            User teacher,
            User student,
            String adminToken,
            String teacherToken,
            String studentToken) {}

    private TestScenario setupScenario() {
        Course course = enrollmentFixtures.publishedCourse(150);
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        User teacher = userFixtures.activeUser(RoleCode.TEACHER);
        User student = userFixtures.activeUser(RoleCode.STUDENT);

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

        List<UUID> lessonIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            UUID lessonId = UUID.randomUUID();
            lessonIds.add(lessonId);
            jdbc.update(
                    "INSERT INTO lessons (id, module_id, position, session_type, status, is_required, created_at, updated_at, version) "
                            + "VALUES (?, ?, ?, 'LECTURE', 'PUBLISHED', true, now(), now(), 0)",
                    lessonId,
                    moduleId,
                    i);
            jdbc.update(
                    "INSERT INTO lesson_translations (lesson_id, locale, title) VALUES (?, 'uz', ?)",
                    lessonId,
                    "Dars " + (i + 1));
        }

        String adminToken = login(admin);
        String teacherToken = login(teacher);
        String studentToken = login(student);

        return new TestScenario(
                course,
                moduleId,
                lessonIds,
                enrollment,
                admin,
                teacher,
                student,
                adminToken,
                teacherToken,
                studentToken);
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

    private String configUrl(UUID courseId) {
        return url("/api/v1/admin/courses/" + courseId + "/learning-path");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
