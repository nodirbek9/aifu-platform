package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.ChangeCodingTaskStatusRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CodingTaskDetail;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CreateCodingTaskRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CreateHintRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CreateTestCaseRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.HintView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.ProgrammingLanguageView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.StudentHintView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.TestCaseView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpdateCodingTaskRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpdateTestCaseRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpsertCodingTaskTranslationRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpsertHintTranslationRequest;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.UpsertLanguageRequest;
import uz.aifu.lms.coding.domain.ComparisonMode;
import uz.aifu.lms.coding.domain.ScoringStrategy;
import uz.aifu.lms.coding.domain.TestCaseVisibility;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.ActivityDetail;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.ChangeActivityStatusRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.CreateActivityRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.UpdateActivityRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.UpsertActivityTranslationRequest;
import uz.aifu.lms.curriculum.domain.ActivityType;
import uz.aifu.lms.curriculum.domain.Difficulty;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.execution.api.dto.RunDtos.RunRequest;
import uz.aifu.lms.execution.api.dto.RunDtos.RunTestView;
import uz.aifu.lms.execution.api.dto.RunDtos.RunView;
import uz.aifu.lms.execution.api.dto.RunDtos.VerificationView;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.CreateSubmissionRequest;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.SubmissionDetail;
import uz.aifu.lms.execution.domain.CodeRunMode;
import uz.aifu.lms.execution.domain.CodeRunStatus;
import uz.aifu.lms.execution.domain.CodeRunVerdict;
import uz.aifu.lms.grading.api.dto.FeedbackDtos.CreateFeedbackRequest;
import uz.aifu.lms.grading.api.dto.FeedbackDtos.MyFeedbackView;
import uz.aifu.lms.grading.domain.FeedbackTargetType;
import uz.aifu.lms.grading.domain.FeedbackVisibility;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.notification.api.dto.NotificationDtos.NotificationView;
import uz.aifu.lms.notification.domain.NotificationType;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.CurriculumFixtures;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Everything around a coding task that is not a graded submission (product spec S-04.4, S-05.1,
 * S-05.4, S-05.5, S-05.8, T-05): ungraded runs, hints opened one at a time, verifying the reference
 * solutions, and a student reading the teacher's feedback.
 *
 * <p>No execution worker runs in this test (see {@code CodingSubmissionIT}); what the worker would
 * write is inserted with plain JDBC, in exactly the shape {@code CodeRunResultRepository} writes.
 */
class CodeRunIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private CurriculumFixtures curriculumFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private JdbcTemplate jdbc;
    @LocalServerPort private int port;

    /** A published task (python with a solution, one example test, one hidden test, two hints). */
    private final class World {
        final User admin = userFixtures.activeUser(RoleCode.ADMIN);
        final String adminToken = login(admin);
        final Course course = enrollmentFixtures.publishedCourse(150);
        final CodingTaskDetail task = createPublishedTask(adminToken, course.getId());
        final User student = enroll();
        final String studentToken = login(student);

        User enroll() {
            User user = userFixtures.activeUser(RoleCode.STUDENT);
            enrollmentService.enroll(
                    course.getId(), user.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
            return user;
        }

        TestCaseView example() {
            return task.testCases().stream().filter(TestCaseView::isExample).findFirst().get();
        }

        TestCaseView hidden() {
            return task.testCases().stream().filter(t -> !t.isExample()).findFirst().get();
        }
    }

    // ------------------------------------------------------------------ run / run examples

    @Test
    void run_isQueued_thenShowsWhatTheWorkerWrote_toItsAuthorOnly() {
        World w = new World();

        ResponseEntity<RunView> queued =
                post(
                        w.studentToken,
                        "/api/v1/runs",
                        new RunRequest(w.task.id(), "python", "print(input())", "5", null),
                        RunView.class);
        assertThat(queued.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(queued.getBody().mode()).isEqualTo(CodeRunMode.CUSTOM);
        assertThat(queued.getBody().status()).isEqualTo(CodeRunStatus.QUEUED);
        UUID runId = queued.getBody().id();
        assertThat(
                        jdbc.queryForObject(
                                "select stdin from code_runs where id = ?", String.class, runId))
                .isEqualTo("5");

        completeCustom(runId, "5\n");
        RunView done = get(w.studentToken, "/api/v1/runs/" + runId, RunView.class).getBody();
        assertThat(done.status()).isEqualTo(CodeRunStatus.COMPLETED);
        assertThat(done.verdict()).isEqualTo(CodeRunVerdict.OK);
        assertThat(done.stdout()).isEqualTo("5\n");
        assertThat(done.exitCode()).isZero();

        // somebody else's run does not exist; a run never becomes a submission
        assertThat(get(login(w.enroll()), "/api/v1/runs/" + runId, ApiError.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from submissions where coding_task_id = ?",
                                Long.class,
                                w.task.id()))
                .isZero();
    }

    @Test
    void runExamples_showsEachExampleTest_andNeverTheDataOfAHiddenOne() {
        World w = new World();
        UUID runId =
                post(
                                w.studentToken,
                                "/api/v1/runs",
                                new RunRequest(
                                        w.task.id(),
                                        "python",
                                        "print(6)",
                                        "ignored",
                                        CodeRunMode.EXAMPLES),
                                RunView.class)
                        .getBody()
                        .id();
        assertThat(
                        jdbc.queryForObject(
                                "select stdin from code_runs where id = ?", String.class, runId))
                .isEmpty();

        // The worker runs the example tests only. A hidden row is inserted here on purpose, to
        // prove the response would still withhold its data.
        insertTestResult(runId, w.example().id(), 0, true, "ACCEPTED", "6");
        insertTestResult(runId, w.hidden().id(), 1, false, "WRONG_ANSWER", "6");
        jdbc.update(
                "update code_runs set status = 'COMPLETED', verdict = 'WRONG_ANSWER',"
                        + " tests_passed = 1, tests_total = 2, completed_at = ? where id = ?",
                Timestamp.from(Instant.now()),
                runId);

        RunView done = get(w.studentToken, "/api/v1/runs/" + runId, RunView.class).getBody();
        assertThat(done.testsPassed()).isEqualTo(1);
        assertThat(done.tests()).hasSize(2);
        RunTestView example = done.tests().get(0);
        assertThat(example.passed()).isTrue();
        assertThat(example.stdin()).isEqualTo("3\n1 2 3");
        assertThat(example.expectedStdout()).isEqualTo("6");
        assertThat(example.actualStdout()).isEqualTo("6");
        RunTestView hidden = done.tests().get(1);
        assertThat(hidden.passed()).isFalse();
        assertThat(hidden.verdict()).isEqualTo("WRONG_ANSWER");
        assertThat(hidden.stdin()).isNull();
        assertThat(hidden.expectedStdout()).isNull();
        assertThat(hidden.actualStdout()).isNull();
    }

    @Test
    void run_guards_language_task_mode_andConcurrency() {
        World w = new World();

        // the three teaching languages are offered for free runs (no task)
        List<ProgrammingLanguageView> languages =
                rest.exchange(
                                url("/api/v1/programming-languages"),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(w.studentToken)),
                                new ParameterizedTypeReference<List<ProgrammingLanguageView>>() {})
                        .getBody();
        assertThat(languages)
                .extracting(ProgrammingLanguageView::code)
                .contains("python", "java", "cpp");

        assertThat(code(w.studentToken, new RunRequest(null, "rust", "fn main(){}", null, null)))
                .isEqualTo("LANGUAGE_NOT_ENABLED");
        // the fixture task accepts python only
        assertThat(
                        code(
                                w.studentToken,
                                new RunRequest(w.task.id(), "java", "class Main{}", "", null)))
                .isEqualTo("LANGUAGE_NOT_ENABLED");
        assertThat(
                        code(
                                w.studentToken,
                                new RunRequest(
                                        null, "python", "print(1)", null, CodeRunMode.EXAMPLES)))
                .isEqualTo("VALIDATION_FAILED");
        assertThat(
                        code(
                                w.studentToken,
                                new RunRequest(
                                        w.task.id(),
                                        "python",
                                        "print(1)",
                                        null,
                                        CodeRunMode.VERIFY)))
                .isEqualTo("VALIDATION_FAILED");

        // a student who is not in the course cannot run against its task
        String outsider = login(userFixtures.activeUser(RoleCode.STUDENT));
        assertThat(
                        post(
                                        outsider,
                                        "/api/v1/runs",
                                        new RunRequest(w.task.id(), "python", "print(1)", "", null),
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        // two runs may wait at once (a free C++ run and a task run); the third is refused
        assertThat(
                        post(
                                        w.studentToken,
                                        "/api/v1/runs",
                                        new RunRequest(null, "cpp", "int main(){}", "", null),
                                        RunView.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.ACCEPTED);
        assertThat(
                        post(
                                        w.studentToken,
                                        "/api/v1/runs",
                                        new RunRequest(w.task.id(), "python", "print(1)", "", null),
                                        RunView.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.ACCEPTED);
        ResponseEntity<ApiError> third =
                post(
                        w.studentToken,
                        "/api/v1/runs",
                        new RunRequest(w.task.id(), "python", "print(1)", "", null),
                        ApiError.class);
        assertThat(third.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(third.getBody().code()).isEqualTo("TOO_MANY_CONCURRENT_SUBMISSIONS");
    }

    // ------------------------------------------------------------------ hints

    @Test
    void hints_openOneAtATime_inOrder() {
        World w = new World();
        String path = "/api/v1/coding-tasks/" + w.task.id() + "/hints";

        List<StudentHintView> closed = hints(w.studentToken, HttpMethod.GET, path);
        assertThat(closed).hasSize(2);
        assertThat(closed).extracting(StudentHintView::revealed).containsOnly(false);
        assertThat(closed).extracting(StudentHintView::body).containsOnlyNulls();

        // the second cannot be opened before the first
        assertThat(
                        post(
                                        w.studentToken,
                                        path + "/" + closed.get(1).id() + "/reveal",
                                        null,
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        List<StudentHintView> first =
                hints(w.studentToken, HttpMethod.POST, path + "/" + closed.get(0).id() + "/reveal");
        assertThat(first.get(0).body()).isEqualTo("Yig'indini hisoblang");
        assertThat(first.get(1).body()).isNull();

        hints(w.studentToken, HttpMethod.POST, path + "/" + closed.get(1).id() + "/reveal");
        List<StudentHintView> open = hints(w.studentToken, HttpMethod.GET, path);
        assertThat(open).extracting(StudentHintView::body).doesNotContainNull();

        // another student starts with everything closed
        assertThat(hints(login(w.enroll()), HttpMethod.GET, path))
                .extracting(StudentHintView::revealed)
                .containsOnly(false);
    }

    // ------------------------------------------------------------------ verify solutions

    @Test
    void verification_queuesEveryReferenceSolution_againstAllTests() {
        World w = new World();
        String path = "/api/v1/teacher/coding-tasks/" + w.task.id() + "/verification";

        assertThat(post(w.studentToken, path, null, String.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<VerificationView> started =
                post(w.adminToken, path, null, VerificationView.class);
        assertThat(started.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(started.getBody().allPassed()).isFalse();
        assertThat(started.getBody().languagesWithoutSolution()).isEmpty();
        assertThat(started.getBody().runs()).hasSize(1);
        RunView run = started.getBody().runs().get(0);
        assertThat(run.mode()).isEqualTo(CodeRunMode.VERIFY);
        assertThat(run.languageCode()).isEqualTo("python");
        assertThat(
                        jdbc.queryForObject(
                                "select source_code from code_runs where id = ?",
                                String.class,
                                run.id()))
                .isEqualTo("print(sum([1,2,3]))");

        // the reference solution fails the hidden test: the teacher sees that test in full
        insertTestResult(run.id(), w.example().id(), 0, true, "ACCEPTED", "6");
        insertTestResult(run.id(), w.hidden().id(), 1, false, "WRONG_ANSWER", "6");
        jdbc.update(
                "update code_runs set status = 'COMPLETED', verdict = 'WRONG_ANSWER',"
                        + " tests_passed = 1, tests_total = 2, completed_at = ? where id = ?",
                Timestamp.from(Instant.now()),
                run.id());
        VerificationView failed = get(w.adminToken, path, VerificationView.class).getBody();
        assertThat(failed.allPassed()).isFalse();
        RunTestView hidden = failed.runs().get(0).tests().get(1);
        assertThat(hidden.expectedStdout()).isEqualTo("0");
        assertThat(hidden.actualStdout()).isEqualTo("6");

        // a later, passing verification replaces it
        UUID second =
                post(w.adminToken, path, null, VerificationView.class).getBody().runs().get(0).id();
        assertThat(second).isNotEqualTo(run.id());
        jdbc.update(
                "update code_runs set status = 'COMPLETED', verdict = 'ACCEPTED', tests_passed = 2,"
                        + " tests_total = 2, completed_at = ? where id = ?",
                Timestamp.from(Instant.now()),
                second);
        assertThat(get(w.adminToken, path, VerificationView.class).getBody().allPassed()).isTrue();
    }

    // ------------------------------------------------------------------ feedback

    @Test
    void feedback_reachesItsStudent_withoutInternalNotes_andNotifiesThem() {
        World w = new World();
        SubmissionDetail submission =
                post(
                                w.studentToken,
                                "/api/v1/submissions",
                                new CreateSubmissionRequest(
                                        w.task.id(), "python", "print(1)", null),
                                SubmissionDetail.class)
                        .getBody();

        for (FeedbackVisibility visibility : FeedbackVisibility.values()) {
            assertThat(
                            post(
                                            w.adminToken,
                                            "/api/v1/teacher/feedback",
                                            new CreateFeedbackRequest(
                                                    FeedbackTargetType.SUBMISSION,
                                                    submission.id(),
                                                    visibility,
                                                    "Note: " + visibility),
                                            String.class)
                                    .getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);
        }

        String mine = "/api/v1/feedback/me?targetType=SUBMISSION&targetId=" + submission.id();
        List<MyFeedbackView> visible =
                rest.exchange(
                                url(mine),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(w.studentToken)),
                                new ParameterizedTypeReference<List<MyFeedbackView>>() {})
                        .getBody();
        assertThat(visible).hasSize(1);
        assertThat(visible.get(0).bodyMarkdown()).isEqualTo("Note: STUDENT_VISIBLE");
        assertThat(visible.get(0).authorName()).isEqualTo(w.admin.getFullName());

        assertThat(get(login(w.enroll()), mine, ApiError.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        PageResponse<NotificationView> notifications =
                rest.exchange(
                                url("/api/v1/notifications/me"),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(w.studentToken)),
                                new ParameterizedTypeReference<PageResponse<NotificationView>>() {})
                        .getBody();
        assertThat(notifications.content())
                .filteredOn(n -> n.type() == NotificationType.TEACHER_FEEDBACK)
                .hasSize(1)
                .first()
                .satisfies(n -> assertThat(n.payloadJson()).contains(w.task.id().toString()));
    }

    // ------------------------------------------------------------------ activity binding

    @Test
    void submission_withoutAnActivityId_countsForTheOneActivityOfItsTask() {
        World w = new World();
        Lesson lesson = curriculumFixtures.publishedLesson(w.course);
        UUID activityId =
                post(
                                w.adminToken,
                                "/api/v1/teacher/courses/" + w.course.getId() + "/activities",
                                new CreateActivityRequest(
                                        null,
                                        lesson.getId(),
                                        ActivityType.CODING_TASK,
                                        false,
                                        null,
                                        null),
                                ActivityDetail.class)
                        .getBody()
                        .id();
        String activity = "/api/v1/teacher/activities/" + activityId;
        exchange(
                w.adminToken,
                HttpMethod.PATCH,
                activity,
                new UpdateActivityRequest(w.task.id(), null, null, null, null, null, null));
        exchange(
                w.adminToken,
                HttpMethod.PUT,
                activity + "/translations/uz",
                new UpsertActivityTranslationRequest("Amaliy topshiriq", null));
        exchange(
                w.adminToken,
                HttpMethod.PUT,
                activity + "/status",
                new ChangeActivityStatusRequest(PublicationStatus.PUBLISHED));

        // the task page submits with the task id only, as from a lesson's practice block
        SubmissionDetail submission =
                post(
                                w.studentToken,
                                "/api/v1/submissions",
                                new CreateSubmissionRequest(
                                        w.task.id(), "python", "print(6)", null),
                                SubmissionDetail.class)
                        .getBody();
        assertThat(
                        jdbc.queryForObject(
                                "select activity_id from submissions where id = ?",
                                UUID.class,
                                submission.id()))
                .isEqualTo(activityId);
    }

    // ------------------------------------------------------------------ fixtures

    private CodingTaskDetail createPublishedTask(String adminToken, UUID courseId) {
        UUID taskId =
                post(
                                adminToken,
                                "/api/v1/teacher/courses/" + courseId + "/coding-tasks",
                                new CreateCodingTaskRequest("RUN-" + UUID.randomUUID()),
                                CodingTaskDetail.class)
                        .getBody()
                        .id();
        String task = "/api/v1/teacher/coding-tasks/" + taskId;
        exchange(
                adminToken,
                HttpMethod.PATCH,
                task,
                new UpdateCodingTaskRequest(
                        Difficulty.EASY,
                        new BigDecimal("100"),
                        ScoringStrategy.PER_TEST_EQUAL,
                        10,
                        2000,
                        5000,
                        262144));
        exchange(
                adminToken,
                HttpMethod.PUT,
                task + "/translations/uz",
                new UpsertCodingTaskTranslationRequest(
                        "Yig'indi", "Sonlar yig'indisi", null, null, null));
        exchange(
                adminToken,
                HttpMethod.POST,
                task + "/languages/python",
                new UpsertLanguageRequest("n = int(input())", "print(sum([1,2,3]))"));
        exchange(
                adminToken,
                HttpMethod.POST,
                task + "/test-cases",
                new CreateTestCaseRequest("3\n1 2 3", "6"));
        exchange(
                adminToken,
                HttpMethod.POST,
                task + "/test-cases",
                new CreateTestCaseRequest("4\n-5 5 -3 3", "0"));
        for (String body : List.of("Yig'indini hisoblang", "sum() funksiyasidan foydalaning")) {
            HintView hint =
                    post(adminToken, task + "/hints", new CreateHintRequest(null), HintView.class)
                            .getBody();
            exchange(
                    adminToken,
                    HttpMethod.PUT,
                    "/api/v1/teacher/coding-task-hints/" + hint.id() + "/translations/uz",
                    new UpsertHintTranslationRequest(body));
        }

        CodingTaskDetail draft = get(adminToken, task, CodingTaskDetail.class).getBody();
        updateTestCase(adminToken, draft.testCases().get(0).id(), true, "3\n1 2 3", "6");
        updateTestCase(adminToken, draft.testCases().get(1).id(), false, "4\n-5 5 -3 3", "0");
        exchange(
                adminToken,
                HttpMethod.PUT,
                task + "/status",
                new ChangeCodingTaskStatusRequest(PublicationStatus.PUBLISHED));
        CodingTaskDetail published = get(adminToken, task, CodingTaskDetail.class).getBody();
        assertThat(published.status()).isEqualTo(PublicationStatus.PUBLISHED);
        assertThat(published.hints()).hasSize(2);
        return published;
    }

    private void updateTestCase(
            String adminToken, UUID testCaseId, boolean example, String stdin, String expected) {
        exchange(
                adminToken,
                HttpMethod.PATCH,
                "/api/v1/teacher/coding-task-test-cases/" + testCaseId,
                new UpdateTestCaseRequest(
                        null,
                        example ? TestCaseVisibility.VISIBLE : TestCaseVisibility.HIDDEN,
                        example,
                        BigDecimal.ONE,
                        stdin,
                        expected,
                        ComparisonMode.TRIMMED,
                        null,
                        null));
    }

    /** What the worker writes for a finished CUSTOM run. */
    private void completeCustom(UUID runId, String stdout) {
        jdbc.update(
                "update code_runs set status = 'COMPLETED', verdict = 'OK', stdout = ?, stderr = '',"
                        + " exit_code = 0, time_ms = 12, completed_at = ? where id = ?",
                stdout,
                Timestamp.from(Instant.now()),
                runId);
    }

    private void insertTestResult(
            UUID runId, UUID testCaseId, int position, boolean passed, String verdict, String out) {
        jdbc.update(
                "insert into code_run_test_results (run_id, test_case_id, position, passed, verdict,"
                        + " time_ms, exit_code, stdout_excerpt, stderr_excerpt)"
                        + " values (?, ?, ?, ?, ?, 7, 0, ?, '')",
                runId,
                testCaseId,
                position,
                passed,
                verdict,
                out);
    }

    private String code(String token, RunRequest request) {
        return post(token, "/api/v1/runs", request, ApiError.class).getBody().code();
    }

    private List<StudentHintView> hints(String token, HttpMethod method, String path) {
        ResponseEntity<List<StudentHintView>> response =
                rest.exchange(
                        url(path),
                        method,
                        new HttpEntity<>(bearer(token)),
                        new ParameterizedTypeReference<List<StudentHintView>>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private void exchange(String token, HttpMethod method, String path, Object body) {
        ResponseEntity<String> response =
                rest.exchange(
                        url(path), method, new HttpEntity<>(body, bearer(token)), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful())
                .as("%s %s -> %s %s", method, path, response.getStatusCode(), response.getBody())
                .isTrue();
    }

    private <T> ResponseEntity<T> get(String token, String path, Class<T> type) {
        return rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(bearer(token)), type);
    }

    private <T> ResponseEntity<T> post(String token, String path, Object body, Class<T> type) {
        return rest.exchange(
                url(path), HttpMethod.POST, new HttpEntity<>(body, bearer(token)), type);
    }

    private String login(User user) {
        return rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class)
                .getBody()
                .accessToken();
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
