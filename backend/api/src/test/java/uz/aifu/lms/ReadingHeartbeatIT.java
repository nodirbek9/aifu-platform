package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.common.events.QuizAttemptGradedEvent;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonDetail;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.progress.api.dto.ProgressDtos.LessonPathView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.LessonProgressView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.ReadingBeatRequest;
import uz.aifu.lms.progress.api.dto.ProgressDtos.RecordLessonProgressRequest;
import uz.aifu.lms.progress.app.PathRules.StepState;
import uz.aifu.lms.progress.app.ProgressService;
import uz.aifu.lms.progress.domain.ProgressStatus;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Integration tests for the server-measured reading heartbeat and completion roll-ups (ADR-015,
 * Phase 5, B6 and B7).
 */
@Tag("it")
class ReadingHeartbeatIT extends AbstractIntegrationTest {

    @MockBean private Clock clock;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private ProgressService progressService;
    @LocalServerPort private int port;

    private final AtomicReference<Instant> currentInstant =
            new AtomicReference<>(Instant.parse("2026-10-08T08:00:00Z"));

    @BeforeEach
    void setUpClock() {
        currentInstant.set(Instant.parse("2026-10-08T08:00:00Z"));
        when(clock.instant()).thenAnswer(inv -> currentInstant.get());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void beatsEvery15s_reachGreenOnlyAfterRequiredTimeAndAtEnd() {
        TestScenario scenario = setupScenario(true);

        // Beat 1 at t=0: first beat initializes lastHeartbeatAt, credits 0s, snapshots
        // requiredReadingSeconds=30
        ResponseEntity<LessonPathView> r1 =
                sendBeat(scenario.l1Id, 0, false, scenario.studentToken);
        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r1.getBody()).isNotNull();
        assertThat(r1.getBody().activeReadingSeconds()).isZero();
        assertThat(r1.getBody().requiredReadingSeconds()).isEqualTo(30);
        assertThat(r1.getBody().reachedEnd()).isFalse();
        assertThat(r1.getBody().open()).isTrue();
        assertThat(r1.getBody().steps().lecture()).isEqualTo(StepState.TODO);

        // Beat 2 at t=15s: credits 15s, reachedEnd still false
        currentInstant.updateAndGet(i -> i.plusSeconds(15));
        ResponseEntity<LessonPathView> r2 =
                sendBeat(scenario.l1Id, 0, false, scenario.studentToken);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r2.getBody()).isNotNull();
        assertThat(r2.getBody().activeReadingSeconds()).isEqualTo(15);
        assertThat(r2.getBody().reachedEnd()).isFalse();
        assertThat(r2.getBody().steps().lecture()).isEqualTo(StepState.TODO);

        // Beat 3 at t=30s: credits 15s (total 30s = required), but atEnd is still false -> lecture
        // still TODO
        currentInstant.updateAndGet(i -> i.plusSeconds(15));
        ResponseEntity<LessonPathView> r3 =
                sendBeat(scenario.l1Id, 0, false, scenario.studentToken);
        assertThat(r3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r3.getBody()).isNotNull();
        assertThat(r3.getBody().activeReadingSeconds()).isEqualTo(30);
        assertThat(r3.getBody().reachedEnd()).isFalse();
        assertThat(r3.getBody().steps().lecture()).isEqualTo(StepState.TODO);

        // Beat 4 at t=45s with atEnd=true: reachedEnd becomes true, lecture becomes DONE
        currentInstant.updateAndGet(i -> i.plusSeconds(15));
        ResponseEntity<LessonPathView> r4 = sendBeat(scenario.l1Id, 0, true, scenario.studentToken);
        assertThat(r4.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r4.getBody()).isNotNull();
        assertThat(r4.getBody().reachedEnd()).isTrue();
        assertThat(r4.getBody().steps().lecture()).isEqualTo(StepState.DONE);
    }

    @Test
    void beatsEvery1s_creditNothingExtra() {
        TestScenario scenario = setupScenario(true);

        // Beat at t=0
        sendBeat(scenario.l1Id, 0, false, scenario.studentToken);

        // Beat at t=15s (credits 15s)
        currentInstant.updateAndGet(i -> i.plusSeconds(15));
        ResponseEntity<LessonPathView> r15 =
                sendBeat(scenario.l1Id, 0, false, scenario.studentToken);
        assertThat(r15.getBody()).isNotNull();
        assertThat(r15.getBody().activeReadingSeconds()).isEqualTo(15);

        // Beat at t=16s (1s gap < 10s min gap -> credits 0)
        currentInstant.updateAndGet(i -> i.plusSeconds(1));
        ResponseEntity<LessonPathView> r16 =
                sendBeat(scenario.l1Id, 0, false, scenario.studentToken);
        assertThat(r16.getBody()).isNotNull();
        assertThat(r16.getBody().activeReadingSeconds()).isEqualTo(15);

        // Beat at t=17s (2s gap < 10s min gap -> credits 0)
        currentInstant.updateAndGet(i -> i.plusSeconds(1));
        ResponseEntity<LessonPathView> r17 =
                sendBeat(scenario.l1Id, 0, false, scenario.studentToken);
        assertThat(r17.getBody()).isNotNull();
        assertThat(r17.getBody().activeReadingSeconds()).isEqualTo(15);
    }

    @Test
    void twoLessonsInParallel_shareClock_secondLessonGetsNoCreditWithin10s() {
        TestScenario scenario = setupScenario(true);

        // L1 beats at t=0
        sendBeat(scenario.l1Id, 0, false, scenario.studentToken);

        // L1 beats at t=15s (credits 15s, L1 lastHeartbeatAt = t=15s)
        currentInstant.updateAndGet(i -> i.plusSeconds(15));
        ResponseEntity<LessonPathView> rL1 =
                sendBeat(scenario.l1Id, 0, false, scenario.studentToken);
        assertThat(rL1.getBody()).isNotNull();
        assertThat(rL1.getBody().activeReadingSeconds()).isEqualTo(15);

        // L3 (independent study lesson, open for student) beats at t=20s (5s after L1)
        currentInstant.updateAndGet(i -> i.plusSeconds(5));
        ResponseEntity<LessonPathView> rL3 =
                sendBeat(scenario.l3IndependentId, 0, false, scenario.studentToken);
        assertThat(rL3.getBody()).isNotNull();
        // Since L1 has a heartbeat at t=15s (within last 10s of t=20s), L3 gets no credit
        assertThat(rL3.getBody().activeReadingSeconds()).isZero();
    }

    @Test
    void stickyFinalBeat_reachingEndOnce_subsequentBeatWithAtEndFalse_keepsLectureGreen() {
        TestScenario scenario = setupScenario(true);

        // Beat 1 at t=0 with atEnd=true (reachedEnd is marked)
        ResponseEntity<LessonPathView> r1 = sendBeat(scenario.l1Id, 0, true, scenario.studentToken);
        assertThat(r1.getBody()).isNotNull();
        assertThat(r1.getBody().reachedEnd()).isTrue();
        assertThat(r1.getBody().steps().lecture()).isEqualTo(StepState.TODO);

        // Advance 30s to satisfy required reading time
        currentInstant.updateAndGet(i -> i.plusSeconds(30));
        // Beat 2 at t=30s sends atEnd=false!
        ResponseEntity<LessonPathView> r2 =
                sendBeat(scenario.l1Id, 0, false, scenario.studentToken);
        assertThat(r2.getBody()).isNotNull();
        assertThat(r2.getBody().activeReadingSeconds()).isEqualTo(30);
        // reachedEnd must remain sticky true!
        assertThat(r2.getBody().reachedEnd()).isTrue();
        // Lecture step must be green!
        assertThat(r2.getBody().steps().lecture()).isEqualTo(StepState.DONE);
    }

    @Test
    void oldEndpoint_completedOnEnabledCourse_failsWithValidationFailed() {
        TestScenario scenario = setupScenario(true);

        // Old progress endpoint with COMPLETED on enabled course fails with 422
        ResponseEntity<ApiError> err =
                rest.exchange(
                        url("/api/v1/lessons/" + scenario.l1Id + "/progress"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new RecordLessonProgressRequest(ProgressStatus.COMPLETED, 0L),
                                bearer(scenario.studentToken)),
                        ApiError.class);
        assertThat(err.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(err.getBody()).isNotNull();
        assertThat(err.getBody().code()).isEqualTo("VALIDATION_FAILED");

        // Old progress endpoint with IN_PROGRESS succeeds
        ResponseEntity<LessonProgressView> ok =
                rest.exchange(
                        url("/api/v1/lessons/" + scenario.l1Id + "/progress"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new RecordLessonProgressRequest(ProgressStatus.IN_PROGRESS, 60L),
                                bearer(scenario.studentToken)),
                        LessonProgressView.class);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);

        // On a course with path disabled, COMPLETED still works as before
        TestScenario disabledScenario = setupScenario(false);
        ResponseEntity<LessonProgressView> disabledOk =
                rest.exchange(
                        url("/api/v1/lessons/" + disabledScenario.l1Id + "/progress"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new RecordLessonProgressRequest(ProgressStatus.COMPLETED, 60L),
                                bearer(disabledScenario.studentToken)),
                        LessonProgressView.class);
        assertThat(disabledOk.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(disabledOk.getBody()).isNotNull();
        assertThat(disabledOk.getBody().status()).isEqualTo(ProgressStatus.COMPLETED);
    }

    @Test
    void passTestQuizAndRead_lessonBecomesCompletedAndL2Opens() {
        TestScenario scenario = setupScenario(true);

        // Initially L2 is locked
        ResponseEntity<ApiError> l2Initial =
                rest.exchange(
                        url("/api/v1/lessons/" + scenario.l2Id),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.studentToken)),
                        ApiError.class);
        assertThat(l2Initial.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(l2Initial.getBody()).isNotNull();
        assertThat(l2Initial.getBody().code()).isEqualTo("LESSON_PREREQUISITE_NOT_MET");

        // Read L1 to completion (reading green)
        sendBeat(scenario.l1Id, 0, true, scenario.studentToken);
        currentInstant.updateAndGet(i -> i.plusSeconds(30));
        ResponseEntity<LessonPathView> readDone =
                sendBeat(scenario.l1Id, 0, true, scenario.studentToken);
        assertThat(readDone.getBody()).isNotNull();
        assertThat(readDone.getBody().steps().lecture()).isEqualTo(StepState.DONE);
        assertThat(readDone.getBody().steps().test()).isEqualTo(StepState.TODO);

        // L1 is NOT completed yet because TEST quiz is pending
        // Therefore L2 is STILL locked
        ResponseEntity<ApiError> l2StillLocked =
                rest.exchange(
                        url("/api/v1/lessons/" + scenario.l2Id),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.studentToken)),
                        ApiError.class);
        assertThat(l2StillLocked.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        // Now pass the TEST quiz via event
        progressService.doRecordActivityAttempt(
                new QuizAttemptGradedEvent(
                        UUID.randomUUID(),
                        scenario.l1QuizId,
                        scenario.enrollment.getId(),
                        scenario.l1QuizActivityId,
                        new BigDecimal("10.00"),
                        new BigDecimal("10.00"),
                        new BigDecimal("100.00"),
                        true,
                        clock.instant()));

        // Now L1 is completed and L2 must open!
        ResponseEntity<LessonDetail> l2Opened =
                rest.exchange(
                        url("/api/v1/lessons/" + scenario.l2Id),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(scenario.studentToken)),
                        LessonDetail.class);
        assertThat(l2Opened.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(l2Opened.getBody()).isNotNull();
        assertThat(l2Opened.getBody().id()).isEqualTo(scenario.l2Id);
    }

    // ---------------------------------------------------------------- scaffolding

    private record TestScenario(
            Course course,
            UUID moduleId,
            UUID l1Id,
            UUID l2Id,
            UUID l3IndependentId,
            UUID l1QuizId,
            UUID l1QuizActivityId,
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
                "INSERT INTO lessons (id, module_id, position, session_type, status, is_required, estimated_minutes, created_at, updated_at, version) "
                        + "VALUES (?, ?, 0, 'LECTURE', 'PUBLISHED', true, 1, now(), now(), 0)",
                l1Id,
                moduleId);
        jdbc.update(
                "INSERT INTO lesson_translations (lesson_id, locale, title) VALUES (?, 'uz', 'Dars 1')",
                l1Id);

        UUID l2Id = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO lessons (id, module_id, position, session_type, status, is_required, estimated_minutes, created_at, updated_at, version) "
                        + "VALUES (?, ?, 1, 'LECTURE', 'PUBLISHED', true, 1, now(), now(), 0)",
                l2Id,
                moduleId);
        jdbc.update(
                "INSERT INTO lesson_translations (lesson_id, locale, title) VALUES (?, 'uz', 'Dars 2')",
                l2Id);

        UUID l3IndependentId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO lessons (id, module_id, position, session_type, status, is_required, estimated_minutes, created_at, updated_at, version) "
                        + "VALUES (?, ?, 2, 'INDEPENDENT_STUDY', 'PUBLISHED', true, 1, now(), now(), 0)",
                l3IndependentId,
                moduleId);
        jdbc.update(
                "INSERT INTO lesson_translations (lesson_id, locale, title) VALUES (?, 'uz', 'Mustaqil talim')",
                l3IndependentId);

        jdbc.update(
                "INSERT INTO content_blocks (id, lesson_id, position, block_type, visibility, created_at, updated_at, version) "
                        + "VALUES (gen_random_uuid(), ?, 0, 'TEXT', 'ALL', now(), now(), 0)",
                l1Id);

        jdbc.update(
                "INSERT INTO content_blocks (id, lesson_id, position, block_type, visibility, created_at, updated_at, version) "
                        + "VALUES (gen_random_uuid(), ?, 0, 'TEXT', 'ALL', now(), now(), 0)",
                l3IndependentId);

        UUID l1QuizId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO quizzes (id, course_id, code, max_attempts, max_points, passing_percent, status, "
                        + "shuffle_questions, shuffle_options, reveal_policy, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, 2, 10, 60, 'PUBLISHED', false, false, 'AFTER_LAST_ATTEMPT', now(), now(), 0)",
                l1QuizId,
                course.getId(),
                "QUIZ-" + UUID.randomUUID().toString().substring(0, 8));

        UUID l1QuizActivityId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO activities (id, course_id, module_id, lesson_id, position, activity_type, reference_id, "
                        + "status, is_graded, collaboration_mode, variant_strategy, path_step, created_at, updated_at, version) "
                        + "VALUES (?, ?, ?, ?, 0, 'QUIZ', ?, 'PUBLISHED', false, 'INDIVIDUAL', 'NONE', 'TEST', now(), now(), 0)",
                l1QuizActivityId,
                course.getId(),
                moduleId,
                l1Id,
                l1QuizId);

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
                l1QuizActivityId,
                enrollment,
                student,
                teacher,
                studentToken,
                teacherToken);
    }

    private ResponseEntity<LessonPathView> sendBeat(
            UUID lessonId, int maxBlockPosition, boolean atEnd, String token) {
        return rest.exchange(
                url("/api/v1/lessons/" + lessonId + "/reading"),
                HttpMethod.POST,
                new HttpEntity<>(new ReadingBeatRequest(maxBlockPosition, atEnd), bearer(token)),
                LessonPathView.class);
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
