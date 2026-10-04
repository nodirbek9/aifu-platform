package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;
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
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.ChangeCodingTaskStatusRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CodingTaskDetail;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CodingTaskStudentDetail;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CreateCodingTaskRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CreateTestCaseRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.TestCaseView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpdateCodingTaskRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpsertCodingTaskTranslationRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpsertLanguageRequest;
import uz.aifu.lms.coding.domain.ScoringStrategy;
import uz.aifu.lms.coding.domain.TestCaseVisibility;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.common.idempotency.IdempotencyKeys;
import uz.aifu.lms.curriculum.domain.Difficulty;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.CreateSubmissionRequest;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.SubmissionDetail;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 8 DoD (docs/planning/phase-plan.md, skills/coding-execution-security/SKILL.md): submission
 * guards (idempotency, attempt-limit, ownership) and the hidden-test/{@code solutionCode} redaction
 * the student-facing mappers are responsible for — all against real Postgres, through the actual
 * HTTP endpoints. The sandbox run itself (worker + docker-run) is verified separately and live —
 * see docs/code-execution/security-model.md — since no {@code execution-worker} process runs in
 * this test; a graded result is simulated here by inserting {@code execution_results}/{@code
 * execution_test_results} rows directly, exactly the shape the worker itself would write.
 */
class CodingSubmissionIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private JdbcTemplate jdbc;
    @LocalServerPort private int port;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void submit_idempotencyKeyReplay_returnsOriginalSubmissionNotADuplicate() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        CodingTaskDetail task = createPublishedTask(adminToken, course.getId(), 5);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        String key = "idem-" + UUID.randomUUID();
        SubmissionDetail first = submit(studentToken, task.id(), "python", "print(1)", key);
        SubmissionDetail replay = submit(studentToken, task.id(), "python", "print(1)", key);

        assertThat(replay.id()).isEqualTo(first.id());
        assertThat(replay.attemptNumber()).isEqualTo(first.attemptNumber());
    }

    @Test
    void submit_attemptLimitReached_rejected() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        CodingTaskDetail task = createPublishedTask(adminToken, course.getId(), 1);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        submit(studentToken, task.id(), "python", "print(1)", "first-" + UUID.randomUUID());

        ResponseEntity<ApiError> second =
                rest.exchange(
                        url("/api/v1/submissions"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateSubmissionRequest(task.id(), "python", "print(1)", null),
                                bearer(studentToken)),
                        ApiError.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(second.getBody().code()).isEqualTo("ATTEMPT_LIMIT_REACHED");
    }

    @Test
    void studentTaskDetail_neverExposesSolutionCodeOrHiddenTestCases() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        CodingTaskDetail task = createPublishedTask(adminToken, course.getId(), 10);

        // Confirm the fixture actually attached a solution and a hidden case, then read back
        // through the STUDENT-facing endpoint and assert neither survives the mapper.
        assertThat(task.languages().get(0).solutionCode()).isEqualTo("print(sum([1,2,3]))");
        boolean fixtureHasHiddenCase =
                task.testCases().stream()
                        .anyMatch(t -> t.visibility() == TestCaseVisibility.HIDDEN);
        assertThat(fixtureHasHiddenCase).isTrue();

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        ResponseEntity<CodingTaskStudentDetail> response =
                rest.exchange(
                        url("/api/v1/courses/" + course.getId() + "/coding-tasks/" + task.id()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(studentToken)),
                        CodingTaskStudentDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CodingTaskStudentDetail studentView = response.getBody();

        // LanguageView (student shape) structurally has no solutionCode field at all — the
        // strongest possible guarantee. exampleTestCases only ever contains isExample=true rows,
        // so the HIDDEN non-example case the fixture created must never appear here.
        assertThat(studentView.languages()).hasSize(1);
        assertThat(studentView.exampleTestCases()).hasSize(1);
        assertThat(studentView.exampleTestCases().get(0).stdin()).isEqualTo("3\n1 2 3");
    }

    @Test
    void submissionDetail_redactsHiddenTestOutputForStudent_teacherSeesEverything() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        CodingTaskDetail task = createPublishedTask(adminToken, course.getId(), 10);
        TestCaseView hiddenCase =
                task.testCases().stream()
                        .filter(t -> t.visibility() == TestCaseVisibility.HIDDEN)
                        .findFirst()
                        .orElseThrow();

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        SubmissionDetail created =
                submit(studentToken, task.id(), "python", "print(1)", "sub-" + UUID.randomUUID());
        insertGradedResult(created.id(), hiddenCase.id());

        SubmissionDetail asStudent =
                rest.exchange(
                                url("/api/v1/submissions/" + created.id()),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(studentToken)),
                                SubmissionDetail.class)
                        .getBody();
        assertThat(asStudent.testResults()).hasSize(1);
        assertThat(asStudent.testResults().get(0).stdin()).isNull();
        assertThat(asStudent.testResults().get(0).expectedStdout()).isNull();
        assertThat(asStudent.testResults().get(0).actualStdout()).isNull();
        assertThat(asStudent.testResults().get(0).actualStderr()).isNull();
        // pass/fail and verdict ARE meant to reach the student — only raw output is redacted.
        assertThat(asStudent.testResults().get(0).passed()).isTrue();
        assertThat(asStudent.testResults().get(0).verdict()).isEqualTo("ACCEPTED");

        SubmissionDetail asTeacher =
                rest.exchange(
                                url(
                                        "/api/v1/teacher/coding-tasks/"
                                                + task.id()
                                                + "/submissions/"
                                                + created.id()),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(adminToken)),
                                SubmissionDetail.class)
                        .getBody();
        assertThat(asTeacher.testResults().get(0).stdin()).isEqualTo("4\n-5 5 -3 3");
        assertThat(asTeacher.testResults().get(0).expectedStdout()).isEqualTo("0");
        assertThat(asTeacher.testResults().get(0).actualStdout()).isEqualTo("0");
    }

    @Test
    void submission_ownedByAnotherStudent_isNotVisible() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        CodingTaskDetail task = createPublishedTask(adminToken, course.getId(), 10);

        User owner = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), owner.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String ownerToken = login(owner);
        SubmissionDetail submission =
                submit(ownerToken, task.id(), "python", "print(1)", "own-" + UUID.randomUUID());

        User intruder = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), intruder.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String intruderToken = login(intruder);

        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/submissions/" + submission.id()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(intruderToken)),
                        ApiError.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void submit_languageNotAttachedToTask_rejected() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        CodingTaskDetail task = createPublishedTask(adminToken, course.getId(), 10);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/submissions"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateSubmissionRequest(task.id(), "go", "package main", null),
                                bearer(studentToken)),
                        ApiError.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("LANGUAGE_NOT_ENABLED");
    }

    @Test
    void reviewQueue_listsTheCoursesSubmissions_withItsFilters() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        CodingTaskDetail task = createPublishedTask(adminToken, course.getId(), 10);
        UUID hiddenCase =
                task.testCases().stream()
                        .filter(t -> t.visibility() == TestCaseVisibility.HIDDEN)
                        .findFirst()
                        .orElseThrow()
                        .id();
        // the editor gets every language of the texts
        assertThat(task.translations())
                .singleElement()
                .satisfies(t -> assertThat(t.title()).isEqualTo("Test task"));
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        SubmissionDetail first =
                submit(studentToken, task.id(), "python", "print(1)", "q-" + UUID.randomUUID());
        SubmissionDetail second =
                submit(studentToken, task.id(), "python", "print(0)", "q-" + UUID.randomUUID());
        insertGradedResult(second.id(), hiddenCase);

        String base = "/api/v1/teacher/courses/" + course.getId() + "/submissions";
        JsonNode all = queue(adminToken, base);
        assertThat(all.get("totalElements").asInt()).isEqualTo(2);
        JsonNode newest = all.get("content").get(0);
        assertThat(newest.get("studentFullName").asText()).isEqualTo(student.getFullName());
        assertThat(newest.get("taskTitle").asText()).isEqualTo("Test task");
        assertThat(newest.get("hasFeedback").asBoolean()).isFalse();
        // no activity sets a deadline yet: nothing is late
        assertThat(newest.get("late").asBoolean()).isFalse();
        assertThat(newest.hasNonNull("dueAt")).isFalse();

        // the course sets the task with a deadline that has passed: the work is marked, not refused
        UUID module = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO course_modules (id, course_id, position) VALUES (?, ?, 900)",
                module,
                course.getId());
        jdbc.update(
                """
                INSERT INTO activities (course_id, module_id, activity_type, reference_id,
                                        is_graded, due_at, status)
                VALUES (?, ?, 'CODING_TASK', ?, false, now() - interval '1 day', 'PUBLISHED')
                """,
                course.getId(),
                module,
                task.id());
        JsonNode late = queue(adminToken, base).get("content").get(0);
        assertThat(late.get("late").asBoolean()).isTrue();
        assertThat(late.hasNonNull("dueAt")).isTrue();

        JsonNode latest = queue(adminToken, base + "?latestOnly=true");
        assertThat(latest.get("totalElements").asInt()).isEqualTo(1);
        assertThat(latest.get("content").get(0).get("submissionId").asText())
                .isEqualTo(second.id().toString());
        assertThat(queue(adminToken, base + "?verdict=ACCEPTED").get("totalElements").asInt())
                .isEqualTo(1);

        jdbc.update(
                """
                INSERT INTO feedback (id, target_type, target_id, author_user_id, visibility,
                                      body_markdown, created_at, updated_at)
                VALUES (?, 'SUBMISSION', ?, ?, 'STUDENT_VISIBLE', 'Yaxshi', now(), now())
                """,
                UUID.randomUUID(),
                first.id(),
                admin.getId());
        JsonNode waiting = queue(adminToken, base + "?needsFeedback=true");
        assertThat(waiting.get("totalElements").asInt()).isEqualTo(1);
        assertThat(waiting.get("content").get(0).get("submissionId").asText())
                .isEqualTo(second.id().toString());

        // a student, or a teacher of another course, is refused
        assertThat(
                        rest.exchange(
                                        url(base),
                                        HttpMethod.GET,
                                        new HttpEntity<>(bearer(studentToken)),
                                        String.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(
                        rest.exchange(
                                        url(base),
                                        HttpMethod.GET,
                                        new HttpEntity<>(
                                                bearer(
                                                        login(
                                                                userFixtures.activeUser(
                                                                        RoleCode.TEACHER)))),
                                        String.class)
                                .getStatusCode()
                                .is4xxClientError())
                .isTrue();
    }

    private JsonNode queue(String token, String path) throws Exception {
        ResponseEntity<String> response =
                rest.exchange(
                        url(path), HttpMethod.GET, new HttpEntity<>(bearer(token)), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return objectMapper.readTree(response.getBody());
    }

    // ---------------------------------------------------------------- fixture + result simulation

    /**
     * Creates a published coding task with one Python language attachment (with a {@code
     * solutionCode} that must never leak to a student), one VISIBLE example test case, and one
     * HIDDEN non-example test case — deliberately mirroring V010's {@code coding_task_test_cases}
     * table comment shape, so redaction tests have something real to redact.
     */
    private CodingTaskDetail createPublishedTask(
            String adminToken, UUID courseId, int maxAttempts) {
        ResponseEntity<CodingTaskDetail> created =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + courseId + "/coding-tasks"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateCodingTaskRequest("TASK-" + UUID.randomUUID()),
                                bearer(adminToken)),
                        CodingTaskDetail.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID taskId = created.getBody().id();

        rest.exchange(
                url("/api/v1/teacher/coding-tasks/" + taskId),
                HttpMethod.PATCH,
                new HttpEntity<>(
                        new UpdateCodingTaskRequest(
                                Difficulty.EASY,
                                new BigDecimal("100"),
                                ScoringStrategy.PER_TEST_EQUAL,
                                maxAttempts,
                                2000,
                                5000,
                                262144),
                        bearer(adminToken)),
                CodingTaskDetail.class);

        rest.exchange(
                url("/api/v1/teacher/coding-tasks/" + taskId + "/translations/uz"),
                HttpMethod.PUT,
                new HttpEntity<>(
                        new UpsertCodingTaskTranslationRequest(
                                "Test task", "Description", null, null, null),
                        bearer(adminToken)),
                CodingTaskDetail.class);

        rest.exchange(
                url("/api/v1/teacher/coding-tasks/" + taskId + "/languages/python"),
                HttpMethod.POST,
                new HttpEntity<>(
                        new UpsertLanguageRequest("n = int(input())", "print(sum([1,2,3]))"),
                        bearer(adminToken)),
                Object.class);

        rest.exchange(
                url("/api/v1/teacher/coding-tasks/" + taskId + "/test-cases"),
                HttpMethod.POST,
                new HttpEntity<>(new CreateTestCaseRequest("3\n1 2 3", "6"), bearer(adminToken)),
                Object.class);
        ResponseEntity<Object> hiddenCreated =
                rest.exchange(
                        url("/api/v1/teacher/coding-tasks/" + taskId + "/test-cases"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateTestCaseRequest("4\n-5 5 -3 3", "0"), bearer(adminToken)),
                        Object.class);
        assertThat(hiddenCreated.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // The first test case is left at its VISIBLE+example default (matches the entity's own
        // constructor default of HIDDEN — flip it explicitly so we control exactly which is which).
        CodingTaskDetail beforePublish = getTeacherTask(adminToken, taskId);
        var visible = beforePublish.testCases().get(0);
        var hidden = beforePublish.testCases().get(1);
        updateTestCase(adminToken, visible.id(), true);
        updateTestCase(adminToken, hidden.id(), false);

        rest.exchange(
                url("/api/v1/teacher/coding-tasks/" + taskId + "/status"),
                HttpMethod.PUT,
                new HttpEntity<>(
                        new ChangeCodingTaskStatusRequest(PublicationStatus.PUBLISHED),
                        bearer(adminToken)),
                CodingTaskDetail.class);

        return getTeacherTask(adminToken, taskId);
    }

    private void updateTestCase(String adminToken, UUID testCaseId, boolean example) {
        var body =
                new uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpdateTestCaseRequest(
                        null,
                        example ? TestCaseVisibility.VISIBLE : TestCaseVisibility.HIDDEN,
                        example,
                        BigDecimal.ONE,
                        example ? "3\n1 2 3" : "4\n-5 5 -3 3",
                        example ? "6" : "0",
                        uz.aifu.lms.coding.domain.ComparisonMode.TRIMMED,
                        null,
                        null);
        rest.exchange(
                url("/api/v1/teacher/coding-task-test-cases/" + testCaseId),
                HttpMethod.PATCH,
                new HttpEntity<>(body, bearer(adminToken)),
                Object.class);
    }

    private CodingTaskDetail getTeacherTask(String adminToken, UUID taskId) {
        return rest.exchange(
                        url("/api/v1/teacher/coding-tasks/" + taskId),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        CodingTaskDetail.class)
                .getBody();
    }

    /**
     * Writes {@code execution_results}/{@code execution_test_results} exactly the shape {@code
     * execution-worker} itself would (see {@code ResultWriteBackRepository}) — plain JDBC, since
     * {@code execution.domain.ExecutionResult}/{@code ExecutionTestResult} are deliberately
     * read-only entities from {@code api}'s side (no public constructor) and no worker process runs
     * in this test. Each {@code jdbc.update} call commits its own connection immediately (no
     * test-method transaction wraps it), so it is fully visible to the HTTP call that follows —
     * unlike a class-level {@code @Transactional}, which {@code AbstractIntegrationTest}'s own
     * class doc explains would NOT be visible to a request served on a different thread/connection.
     */
    private void insertGradedResult(UUID submissionId, UUID hiddenTestCaseId) {
        UUID resultId = UUID.randomUUID();
        Timestamp now = Timestamp.from(java.time.Instant.now());
        jdbc.update(
                """
                INSERT INTO execution_results
                    (id, submission_id, engine, sandbox_profile, compile_success, exit_code,
                     total_time_ms, output_truncated, created_at)
                VALUES (?, ?, 'noop', 'test-simulated', true, 0, 5, false, ?)
                """,
                resultId,
                submissionId,
                now);
        jdbc.update(
                """
                INSERT INTO execution_test_results
                    (execution_result_id, test_case_id, passed, verdict, time_ms, exit_code,
                     stdout_excerpt, stderr_excerpt, points_awarded)
                VALUES (?, ?, true, 'ACCEPTED', 5, 0, '0', '', 100)
                """,
                resultId,
                hiddenTestCaseId);
        jdbc.update(
                """
                UPDATE submissions SET status = 'COMPLETED', verdict = 'ACCEPTED', score = 100,
                    max_score = 100, tests_passed = 1, tests_total = 1, completed_at = ?,
                    version = version + 1
                WHERE id = ?
                """,
                now,
                submissionId);
    }

    private String login(User user) {
        ResponseEntity<TokenResponse> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class);
        return response.getBody().accessToken();
    }

    private SubmissionDetail submit(
            String studentToken,
            UUID taskId,
            String language,
            String sourceCode,
            String idempotencyKey) {
        HttpHeaders headers = bearer(studentToken);
        headers.set(IdempotencyKeys.HEADER, idempotencyKey);
        ResponseEntity<SubmissionDetail> response =
                rest.exchange(
                        url("/api/v1/submissions"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateSubmissionRequest(taskId, language, sourceCode, null),
                                headers),
                        SubmissionDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
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
