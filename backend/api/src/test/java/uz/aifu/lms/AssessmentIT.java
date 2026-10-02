package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.AttemptDetail;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.RecordAnswerRequest;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.SelectedOptionInput;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.StartAttemptRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.AnswerOptionView;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.CreateAnswerKeyRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.CreateAnswerOptionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.CreateQuestionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.QuestionDetail;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.UpdateQuestionRequest;
import uz.aifu.lms.assessment.api.dto.QuizDtos.ChangeQuizStatusRequest;
import uz.aifu.lms.assessment.api.dto.QuizDtos.CreateQuizRequest;
import uz.aifu.lms.assessment.api.dto.QuizDtos.QuizDetail;
import uz.aifu.lms.assessment.api.dto.QuizDtos.UpdateQuizRequest;
import uz.aifu.lms.assessment.api.dto.QuizDtos.UpsertQuizTranslationRequest;
import uz.aifu.lms.assessment.domain.AttemptStatus;
import uz.aifu.lms.assessment.domain.MatchMode;
import uz.aifu.lms.assessment.domain.QuestionType;
import uz.aifu.lms.assessment.domain.RevealPolicy;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.common.idempotency.IdempotencyKeys;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 7 DoD (docs/planning/phase-plan.md): grading per question type (incl. partial credit),
 * attempt-limit enforcement, concurrent double-submit, expiry, and correct answers hidden until
 * {@code revealPolicy} allows — all against real Postgres, through the actual HTTP endpoints.
 */
class AssessmentIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @LocalServerPort private int port;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void singleChoiceQuestion_gradedCorrectly() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        QuizDetail quiz = createQuiz(adminToken, course.getId(), RevealPolicy.IMMEDIATELY, null);
        QuestionDetail question =
                createQuestion(adminToken, quiz.id(), QuestionType.SINGLE_CHOICE, "2");
        AnswerOptionView correct = addOption(adminToken, question.id(), true, null);
        addOption(adminToken, question.id(), false, null);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        publishQuiz(adminToken, quiz.id());
        AttemptDetail attempt = startAttempt(studentToken, quiz.id());
        answer(
                studentToken,
                attempt.id(),
                question.id(),
                new RecordAnswerRequest(
                        null, null, List.of(new SelectedOptionInput(correct.id(), null))));
        AttemptDetail graded = submit(studentToken, attempt.id(), null);

        assertThat(graded.status()).isEqualTo(AttemptStatus.GRADED);
        assertThat(graded.score()).isEqualByComparingTo(new BigDecimal("2"));
        assertThat(graded.percent()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(graded.passed()).isTrue();
        assertThat(graded.questions().get(0).isCorrect()).isTrue();
    }

    @Test
    void multipleChoiceQuestion_partialCreditScoresProportionally() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        QuizDetail quiz = createQuiz(adminToken, course.getId(), RevealPolicy.IMMEDIATELY, null);
        QuestionDetail question =
                createQuestion(adminToken, quiz.id(), QuestionType.MULTIPLE_CHOICE, "4");
        updateQuestionSettings(
                adminToken, question.id(), new BigDecimal("4"), "{\"partialCredit\":true}");
        AnswerOptionView correctA = addOption(adminToken, question.id(), true, null);
        AnswerOptionView correctB = addOption(adminToken, question.id(), true, null);
        addOption(adminToken, question.id(), false, null);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        publishQuiz(adminToken, quiz.id());
        AttemptDetail attempt = startAttempt(studentToken, quiz.id());
        // only one of the two correct options selected -> half credit
        answer(
                studentToken,
                attempt.id(),
                question.id(),
                new RecordAnswerRequest(
                        null, null, List.of(new SelectedOptionInput(correctA.id(), null))));
        AttemptDetail graded = submit(studentToken, attempt.id(), null);

        assertThat(graded.score()).isEqualByComparingTo(new BigDecimal("2.00"));
    }

    @Test
    void shortAnswerAndNumeric_gradedViaAnswerKeys() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        QuizDetail quiz = createQuiz(adminToken, course.getId(), RevealPolicy.IMMEDIATELY, null);

        QuestionDetail shortAnswer =
                createQuestion(adminToken, quiz.id(), QuestionType.SHORT_ANSWER, "1");
        addAnswerKey(adminToken, shortAnswer.id(), MatchMode.CASE_INSENSITIVE, "Paris", null, null);

        QuestionDetail numeric = createQuestion(adminToken, quiz.id(), QuestionType.NUMERIC, "1");
        addAnswerKey(
                adminToken,
                numeric.id(),
                MatchMode.NUMERIC_TOLERANCE,
                "3.14",
                new BigDecimal("0.01"),
                null);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        publishQuiz(adminToken, quiz.id());
        AttemptDetail attempt = startAttempt(studentToken, quiz.id());
        answer(
                studentToken,
                attempt.id(),
                shortAnswer.id(),
                new RecordAnswerRequest("paris", null, null));
        answer(
                studentToken,
                attempt.id(),
                numeric.id(),
                new RecordAnswerRequest(null, new BigDecimal("3.145"), null));
        AttemptDetail graded = submit(studentToken, attempt.id(), null);

        assertThat(graded.score()).isEqualByComparingTo(new BigDecimal("2"));
        assertThat(graded.percent()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void matchingAndOrdering_gradedViaOrdinals() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        QuizDetail quiz = createQuiz(adminToken, course.getId(), RevealPolicy.IMMEDIATELY, null);

        QuestionDetail matching = createQuestion(adminToken, quiz.id(), QuestionType.MATCHING, "2");
        AnswerOptionView m1 = addOption(adminToken, matching.id(), false, "pairA");
        AnswerOptionView m2 = addOption(adminToken, matching.id(), false, "pairA");
        AnswerOptionView m3 = addOption(adminToken, matching.id(), false, "pairB");
        AnswerOptionView m4 = addOption(adminToken, matching.id(), false, "pairB");

        QuestionDetail ordering = createQuestion(adminToken, quiz.id(), QuestionType.ORDERING, "1");
        AnswerOptionView o1 = addOption(adminToken, ordering.id(), false, null); // position 0
        AnswerOptionView o2 = addOption(adminToken, ordering.id(), false, null); // position 1
        AnswerOptionView o3 = addOption(adminToken, ordering.id(), false, null); // position 2

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        publishQuiz(adminToken, quiz.id());
        AttemptDetail attempt = startAttempt(studentToken, quiz.id());
        answer(
                studentToken,
                attempt.id(),
                matching.id(),
                new RecordAnswerRequest(
                        null,
                        null,
                        List.of(
                                new SelectedOptionInput(m1.id(), 0),
                                new SelectedOptionInput(m2.id(), 0),
                                new SelectedOptionInput(m3.id(), 1),
                                new SelectedOptionInput(m4.id(), 1))));
        answer(
                studentToken,
                attempt.id(),
                ordering.id(),
                new RecordAnswerRequest(
                        null,
                        null,
                        List.of(
                                new SelectedOptionInput(o1.id(), 0),
                                new SelectedOptionInput(o2.id(), 1),
                                new SelectedOptionInput(o3.id(), 2))));
        AttemptDetail graded = submit(studentToken, attempt.id(), null);

        assertThat(graded.score()).isEqualByComparingTo(new BigDecimal("3"));
    }

    @Test
    void attemptLimit_isEnforced() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        QuizDetail quiz = createQuiz(adminToken, course.getId(), RevealPolicy.IMMEDIATELY, 1);
        QuestionDetail question =
                createQuestion(adminToken, quiz.id(), QuestionType.TRUE_FALSE, "1");
        addOption(adminToken, question.id(), true, null);
        addOption(adminToken, question.id(), false, null);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        publishQuiz(adminToken, quiz.id());
        AttemptDetail first = startAttempt(studentToken, quiz.id());
        submit(studentToken, first.id(), null);

        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/quizzes/" + quiz.id() + "/attempts"),
                        HttpMethod.POST,
                        new HttpEntity<>(new StartAttemptRequest(null, null), bearer(studentToken)),
                        ApiError.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("ATTEMPT_LIMIT_REACHED");
    }

    @Test
    void concurrentDoubleSubmit_onlyOneWinsWithoutIdempotencyKey() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        QuizDetail quiz = createQuiz(adminToken, course.getId(), RevealPolicy.IMMEDIATELY, null);
        QuestionDetail question =
                createQuestion(adminToken, quiz.id(), QuestionType.TRUE_FALSE, "1");
        addOption(adminToken, question.id(), true, null);
        addOption(adminToken, question.id(), false, null);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);
        publishQuiz(adminToken, quiz.id());
        AttemptDetail attempt = startAttempt(studentToken, quiz.id());

        int attempts = 2;
        CountDownLatch ready = new CountDownLatch(attempts);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        List<Integer> statuses = java.util.Collections.synchronizedList(new ArrayList<>());
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < attempts; i++) {
                futures.add(
                        pool.submit(
                                () -> {
                                    ready.countDown();
                                    try {
                                        start.await();
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        return;
                                    }
                                    ResponseEntity<AttemptDetail> response =
                                            rest.exchange(
                                                    url(
                                                            "/api/v1/attempts/"
                                                                    + attempt.id()
                                                                    + "/submit"),
                                                    HttpMethod.POST,
                                                    new HttpEntity<>(bearer(studentToken)),
                                                    AttemptDetail.class);
                                    statuses.add(response.getStatusCode().value());
                                }));
            }
            ready.await(10, TimeUnit.SECONDS);
            start.countDown();
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdown();
        }

        long succeeded = statuses.stream().filter(s -> s == 200).count();
        assertThat(succeeded)
                .as("exactly one concurrent submit must win: %s", statuses)
                .isEqualTo(1);
    }

    @Test
    void submitIdempotencyKey_replaysInsteadOfErroring() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        QuizDetail quiz = createQuiz(adminToken, course.getId(), RevealPolicy.IMMEDIATELY, null);
        QuestionDetail question =
                createQuestion(adminToken, quiz.id(), QuestionType.TRUE_FALSE, "1");
        addOption(adminToken, question.id(), true, null);
        addOption(adminToken, question.id(), false, null);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);
        publishQuiz(adminToken, quiz.id());
        AttemptDetail attempt = startAttempt(studentToken, quiz.id());

        String key = "test-key-" + UUID.randomUUID();
        AttemptDetail first = submit(studentToken, attempt.id(), key);
        AttemptDetail replay = submit(studentToken, attempt.id(), key);

        assertThat(replay.id()).isEqualTo(first.id());
        assertThat(replay.status()).isEqualTo(AttemptStatus.GRADED);
    }

    @Test
    void expiredAttempt_cannotBeSubmitted() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        QuizDetail quiz = createQuiz(adminToken, course.getId(), RevealPolicy.IMMEDIATELY, null);
        updateQuizTimeLimit(adminToken, quiz.id(), 1);
        QuestionDetail question =
                createQuestion(adminToken, quiz.id(), QuestionType.TRUE_FALSE, "1");
        addOption(adminToken, question.id(), true, null);
        addOption(adminToken, question.id(), false, null);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);
        publishQuiz(adminToken, quiz.id());
        AttemptDetail attempt = startAttempt(studentToken, quiz.id());

        Thread.sleep(1500);

        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/attempts/" + attempt.id() + "/submit"),
                        HttpMethod.POST,
                        new HttpEntity<>(bearer(studentToken)),
                        ApiError.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("ATTEMPT_ALREADY_SUBMITTED");
    }

    @Test
    void revealPolicyNever_hidesCorrectAnswersAndExplanationForever() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        QuizDetail quiz = createQuiz(adminToken, course.getId(), RevealPolicy.NEVER, null);
        QuestionDetail question =
                createQuestion(adminToken, quiz.id(), QuestionType.TRUE_FALSE, "1");
        AnswerOptionView correct = addOption(adminToken, question.id(), true, null);
        addOption(adminToken, question.id(), false, null);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        publishQuiz(adminToken, quiz.id());
        AttemptDetail attempt = startAttempt(studentToken, quiz.id());
        answer(
                studentToken,
                attempt.id(),
                question.id(),
                new RecordAnswerRequest(
                        null, null, List.of(new SelectedOptionInput(correct.id(), null))));
        AttemptDetail graded = submit(studentToken, attempt.id(), null);

        assertThat(graded.questions().get(0).isCorrect()).isNull();
        assertThat(graded.questions().get(0).pointsAwarded()).isNull();
        // the served option list never carries isCorrect regardless of reveal policy
        ResponseEntity<QuestionDetail> teacherView =
                rest.exchange(
                        url("/api/v1/teacher/questions/" + question.id()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        QuestionDetail.class);
        assertThat(teacherView.getBody().options())
                .extracting(AnswerOptionView::isCorrect)
                .contains(true);
    }

    /**
     * Regression test for a real defect found via {@code docker compose up --build} smoke testing
     * against the seeded {@code DEV-QUIZ-DB} fixture (docs/BUILD-STATUS.md "Defects found and fixed
     * — Phase 7"): {@code ProgressService.doRecordActivityAttempt} threw {@code
     * TransactionRequiredException} because it ran with plain {@code @Transactional} inside {@code
     * AttemptService.doSubmit}'s {@code AFTER_COMMIT} callback, where the original transaction's
     * resources are already unbound from the thread. No unit/other IT test exercised this path —
     * every other test starts an attempt with {@code activityId = null}, which short-circuits the
     * listener before it ever touches the database.
     */
    @Test
    void gradedAttemptLinkedToActivity_updatesActivityAndCourseProgress() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        QuizDetail quiz = createQuiz(adminToken, course.getId(), RevealPolicy.IMMEDIATELY, null);
        QuestionDetail question =
                createQuestion(adminToken, quiz.id(), QuestionType.TRUE_FALSE, "1");
        AnswerOptionView correct = addOption(adminToken, question.id(), true, null);
        addOption(adminToken, question.id(), false, null);
        publishQuiz(adminToken, quiz.id());

        UUID moduleId = createModule(adminToken, course.getId());
        UUID activityId = createQuizActivity(adminToken, course.getId(), moduleId, quiz.id());

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String studentToken = login(student);

        ResponseEntity<uz.aifu.lms.progress.api.dto.ProgressDtos.CourseProgressView> before =
                rest.exchange(
                        url("/api/v1/courses/" + course.getId() + "/progress/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(studentToken)),
                        uz.aifu.lms.progress.api.dto.ProgressDtos.CourseProgressView.class);
        assertThat(before.getBody().activitiesCompleted()).isZero();

        ResponseEntity<AttemptDetail> started =
                rest.exchange(
                        url("/api/v1/quizzes/" + quiz.id() + "/attempts"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new StartAttemptRequest(null, activityId), bearer(studentToken)),
                        AttemptDetail.class);
        assertThat(started.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID attemptId = started.getBody().id();
        answer(
                studentToken,
                attemptId,
                question.id(),
                new RecordAnswerRequest(
                        null, null, List.of(new SelectedOptionInput(correct.id(), null))));
        AttemptDetail graded = submit(studentToken, attemptId, null);
        assertThat(graded.status()).isEqualTo(AttemptStatus.GRADED);
        assertThat(graded.passed()).isTrue();

        ResponseEntity<uz.aifu.lms.progress.api.dto.ProgressDtos.CourseProgressView> after =
                rest.exchange(
                        url("/api/v1/courses/" + course.getId() + "/progress/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(studentToken)),
                        uz.aifu.lms.progress.api.dto.ProgressDtos.CourseProgressView.class);
        assertThat(after.getBody().activitiesTotal()).isEqualTo(1);
        assertThat(after.getBody().activitiesCompleted()).isEqualTo(1);
    }

    // ---------------------------------------------------------------- scaffolding

    private UUID createModule(String adminToken, UUID courseId) {
        ResponseEntity<Map<String, Object>> response =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + courseId + "/modules"),
                        HttpMethod.POST,
                        new HttpEntity<>(Map.of(), bearer(adminToken)),
                        (Class<Map<String, Object>>) (Class<?>) Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID moduleId = UUID.fromString((String) response.getBody().get("id"));

        rest.exchange(
                url("/api/v1/teacher/modules/" + moduleId + "/translations/uz"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("title", "Test module"), bearer(adminToken)),
                Object.class);
        rest.exchange(
                url("/api/v1/teacher/modules/" + moduleId + "/status"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("status", "PUBLISHED"), bearer(adminToken)),
                Object.class);
        return moduleId;
    }

    @SuppressWarnings("unchecked")
    private UUID createQuizActivity(String adminToken, UUID courseId, UUID moduleId, UUID quizId) {
        ResponseEntity<Map<String, Object>> created =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + courseId + "/activities"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new java.util.HashMap<>(
                                        Map.of(
                                                "moduleId",
                                                moduleId.toString(),
                                                "activityType",
                                                "QUIZ",
                                                "isGraded",
                                                false)),
                                bearer(adminToken)),
                        (Class<Map<String, Object>>) (Class<?>) Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID activityId = UUID.fromString((String) created.getBody().get("id"));

        Map<String, Object> updateBody = new java.util.HashMap<>();
        updateBody.put("referenceId", quizId.toString());
        updateBody.put("collaborationMode", "INDIVIDUAL");
        updateBody.put("variantStrategy", "NONE");
        rest.exchange(
                url("/api/v1/teacher/activities/" + activityId),
                HttpMethod.PATCH,
                new HttpEntity<>(updateBody, bearer(adminToken)),
                Object.class);

        rest.exchange(
                url("/api/v1/teacher/activities/" + activityId + "/translations/uz"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("title", "Test quiz activity"), bearer(adminToken)),
                Object.class);

        ResponseEntity<Object> published =
                rest.exchange(
                        url("/api/v1/teacher/activities/" + activityId + "/status"),
                        HttpMethod.PUT,
                        new HttpEntity<>(Map.of("status", "PUBLISHED"), bearer(adminToken)),
                        Object.class);
        assertThat(published.getStatusCode()).isEqualTo(HttpStatus.OK);
        return activityId;
    }

    private QuizDetail createQuiz(
            String adminToken, UUID courseId, RevealPolicy revealPolicy, Integer maxAttempts) {
        ResponseEntity<QuizDetail> created =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + courseId + "/quizzes"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateQuizRequest("Q-" + UUID.randomUUID()),
                                bearer(adminToken)),
                        QuizDetail.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID quizId = created.getBody().id();

        rest.exchange(
                url("/api/v1/teacher/quizzes/" + quizId),
                HttpMethod.PATCH,
                new HttpEntity<>(
                        new UpdateQuizRequest(
                                null,
                                null,
                                new BigDecimal("60"),
                                null,
                                maxAttempts,
                                null,
                                false,
                                false,
                                revealPolicy),
                        bearer(adminToken)),
                QuizDetail.class);

        rest.exchange(
                url("/api/v1/teacher/quizzes/" + quizId + "/translations/uz"),
                HttpMethod.PUT,
                new HttpEntity<>(
                        new UpsertQuizTranslationRequest("Test quiz", null, null),
                        bearer(adminToken)),
                QuizDetail.class);

        ResponseEntity<QuizDetail> updated =
                rest.exchange(
                        url("/api/v1/teacher/quizzes/" + quizId),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        QuizDetail.class);
        return updated.getBody();
    }

    private QuestionDetail createQuestion(
            String adminToken, UUID quizId, QuestionType type, String points) {
        ResponseEntity<QuestionDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/quizzes/" + quizId + "/questions"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateQuestionRequest(type, new BigDecimal(points)),
                                bearer(adminToken)),
                        QuestionDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void updateQuestionSettings(
            String adminToken, UUID questionId, BigDecimal points, String settingsJson)
            throws Exception {
        rest.exchange(
                url("/api/v1/teacher/questions/" + questionId),
                HttpMethod.PATCH,
                new HttpEntity<>(
                        new UpdateQuestionRequest(
                                points, null, true, objectMapper.readTree(settingsJson)),
                        bearer(adminToken)),
                QuestionDetail.class);
    }

    private AnswerOptionView addOption(
            String adminToken, UUID questionId, boolean isCorrect, String matchKey) {
        ResponseEntity<AnswerOptionView> response =
                rest.exchange(
                        url("/api/v1/teacher/questions/" + questionId + "/options"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateAnswerOptionRequest(isCorrect, matchKey),
                                bearer(adminToken)),
                        AnswerOptionView.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void addAnswerKey(
            String adminToken,
            UUID questionId,
            MatchMode matchMode,
            String expectedValue,
            BigDecimal tolerance,
            String locale) {
        rest.exchange(
                url("/api/v1/teacher/questions/" + questionId + "/answer-keys"),
                HttpMethod.POST,
                new HttpEntity<>(
                        new CreateAnswerKeyRequest(matchMode, expectedValue, tolerance, locale),
                        bearer(adminToken)),
                Object.class);
    }

    private void updateQuizTimeLimit(String adminToken, UUID quizId, int timeLimitSeconds) {
        rest.exchange(
                url("/api/v1/teacher/quizzes/" + quizId),
                HttpMethod.PATCH,
                new HttpEntity<>(
                        new UpdateQuizRequest(
                                null,
                                null,
                                new BigDecimal("60"),
                                timeLimitSeconds,
                                null,
                                null,
                                false,
                                false,
                                RevealPolicy.IMMEDIATELY),
                        bearer(adminToken)),
                QuizDetail.class);
    }

    private void publishQuiz(String adminToken, UUID quizId) {
        ResponseEntity<QuizDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/quizzes/" + quizId + "/status"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new ChangeQuizStatusRequest(PublicationStatus.PUBLISHED),
                                bearer(adminToken)),
                        QuizDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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

    private void answer(
            String studentToken, UUID attemptId, UUID questionId, RecordAnswerRequest request) {
        ResponseEntity<Object> response =
                rest.exchange(
                        url(
                                "/api/v1/attempts/"
                                        + attemptId
                                        + "/questions/"
                                        + questionId
                                        + "/answer"),
                        HttpMethod.POST,
                        new HttpEntity<>(request, bearer(studentToken)),
                        Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private AttemptDetail submit(String studentToken, UUID attemptId, String idempotencyKey) {
        HttpHeaders headers = bearer(studentToken);
        if (idempotencyKey != null) {
            headers.set(IdempotencyKeys.HEADER, idempotencyKey);
        }
        ResponseEntity<AttemptDetail> response =
                rest.exchange(
                        url("/api/v1/attempts/" + attemptId + "/submit"),
                        HttpMethod.POST,
                        new HttpEntity<>(headers),
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
