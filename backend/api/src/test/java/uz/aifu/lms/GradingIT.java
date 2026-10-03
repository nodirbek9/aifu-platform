package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.AssessmentComponent;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.AttemptDetail;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.RecordAnswerRequest;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.SelectedOptionInput;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.StartAttemptRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.AnswerOptionView;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.CreateAnswerOptionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.CreateQuestionRequest;
import uz.aifu.lms.assessment.api.dto.QuestionDtos.QuestionDetail;
import uz.aifu.lms.assessment.api.dto.QuizDtos.ChangeQuizStatusRequest;
import uz.aifu.lms.assessment.api.dto.QuizDtos.CreateQuizRequest;
import uz.aifu.lms.assessment.api.dto.QuizDtos.QuizDetail;
import uz.aifu.lms.assessment.api.dto.QuizDtos.UpsertQuizTranslationRequest;
import uz.aifu.lms.assessment.domain.QuestionType;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.grading.api.dto.FeedbackDtos.CreateFeedbackRequest;
import uz.aifu.lms.grading.api.dto.FeedbackDtos.FeedbackView;
import uz.aifu.lms.grading.api.dto.GradeDtos.CourseGradebookRow;
import uz.aifu.lms.grading.api.dto.GradeDtos.EnrollmentGradeSummary;
import uz.aifu.lms.grading.api.dto.GradeDtos.GradeEntryView;
import uz.aifu.lms.grading.api.dto.GradeDtos.RecordManualGradeRequest;
import uz.aifu.lms.grading.api.dto.GradeDtos.VoidGradeRequest;
import uz.aifu.lms.grading.domain.FeedbackTargetType;
import uz.aifu.lms.grading.domain.FeedbackVisibility;
import uz.aifu.lms.grading.domain.GradeEntryStatus;
import uz.aifu.lms.grading.domain.GradeSource;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 9 DoD (docs/planning/phase-plan.md): a graded quiz attempt auto-creates a published {@code
 * GradeEntry} and recomputes {@code Enrollment}'s derived final-score snapshot (ADR-011) — all
 * against real Postgres, through the actual HTTP endpoints. {@code FinalScoreCalculator}'s own
 * rubric/band-boundary logic is covered by {@code FinalScoreCalculatorTest} (unit); this class
 * covers the append-only write path, the event-driven auto-grade wiring, and authorization.
 */
class GradingIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private CourseService courseService;
    @LocalServerPort private int port;

    @Test
    void gradedQuizAttempt_autoCreatesPublishedGradeEntry_andRecomputesFinalScore() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        AssessmentComponent jn = soleTopLevelComponent(course.getId());

        QuizDetail quiz = createQuiz(adminToken, course.getId());
        QuestionDetail question = createQuestion(adminToken, quiz.id(), "100");
        AnswerOptionView correct = addOption(adminToken, question.id(), true);
        addOption(adminToken, question.id(), false);
        publishQuiz(adminToken, quiz.id());

        UUID moduleId = createModule(adminToken, course.getId());
        UUID activityId =
                createGradedQuizActivity(
                        adminToken, course.getId(), moduleId, quiz.id(), jn.getId());

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        var enrollment =
                enrollmentService.enroll(
                        course.getId(),
                        student.getId(),
                        EnrollmentSource.ADMIN,
                        admin.getId(),
                        null);
        String studentToken = login(student);

        AttemptDetail attempt = startAttempt(studentToken, quiz.id(), activityId);
        answer(studentToken, attempt.id(), question.id(), correct.id());
        submit(studentToken, attempt.id());

        EnrollmentGradeSummary summary = myGrades(studentToken, enrollment.getId());
        assertThat(summary.entries()).hasSize(1);
        GradeEntryView entry = summary.entries().get(0);
        assertThat(entry.source()).isEqualTo(GradeSource.QUIZ);
        assertThat(entry.status()).isEqualTo(GradeEntryStatus.PUBLISHED);
        assertThat(entry.score()).isEqualByComparingTo("100");
        assertThat(summary.finalScore()).isEqualByComparingTo("100.00");
        assertThat(summary.finalGradeSymbol()).isEqualTo("5");
        assertThat(summary.admittedToFinal()).isTrue();
    }

    @Test
    void manualGrade_thenOverride_supersedesNotDuplicates_andVoidRemovesFromCurrent() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        AssessmentComponent jn = soleTopLevelComponent(course.getId());

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        var enrollment =
                enrollmentService.enroll(
                        course.getId(),
                        student.getId(),
                        EnrollmentSource.ADMIN,
                        admin.getId(),
                        null);
        String studentToken = login(student);

        GradeEntryView first =
                recordManualGrade(adminToken, enrollment.getId(), jn.getId(), "70", "100");
        assertThat(first.revision()).isEqualTo(1);
        EnrollmentGradeSummary afterFirst = myGrades(studentToken, enrollment.getId());
        assertThat(afterFirst.finalScore()).isEqualByComparingTo("70.00");

        GradeEntryView second =
                recordManualGrade(adminToken, enrollment.getId(), jn.getId(), "85", "100");
        assertThat(second.revision()).isEqualTo(2);
        assertThat(second.supersedesId()).isEqualTo(first.id());

        EnrollmentGradeSummary afterSecond = myGrades(studentToken, enrollment.getId());
        // Exactly one CURRENT entry for this component — the superseded revision 1 is gone from
        // the current view, never duplicated (ADR-011's own "consumers never see revisions" claim).
        assertThat(afterSecond.entries()).hasSize(1);
        assertThat(afterSecond.entries().get(0).id()).isEqualTo(second.id());
        assertThat(afterSecond.finalScore()).isEqualByComparingTo("85.00");
        // the grades page: the scheme with what was earned, and the correction kept in the history
        assertThat(afterSecond.courseId()).isEqualTo(course.getId());
        assertThat(afterSecond.components())
                .singleElement()
                .satisfies(
                        component -> {
                            assertThat(component.id()).isEqualTo(jn.getId());
                            assertThat(component.earned()).isEqualByComparingTo("85");
                            assertThat(component.graded()).isTrue();
                        });
        assertThat(afterSecond.complete()).isTrue();
        assertThat(afterSecond.history())
                .extracting(GradeEntryView::id)
                .containsExactly(second.id(), first.id());
        assertThat(afterSecond.history().get(1).status()).isEqualTo(GradeEntryStatus.REVISED);

        // the course gradebook: one row per student with the same summary
        CourseGradebookRow[] book =
                rest.exchange(
                                url("/api/v1/teacher/courses/" + course.getId() + "/gradebook"),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(adminToken)),
                                CourseGradebookRow[].class)
                        .getBody();
        assertThat(book).hasSize(1);
        assertThat(book[0].studentUserId()).isEqualTo(student.getId());
        assertThat(book[0].grades().finalScore()).isEqualByComparingTo("85.00");
        assertThat(book[0].grades().components().get(0).earned()).isEqualByComparingTo("85");
        assertThat(
                        rest.exchange(
                                        url(
                                                "/api/v1/teacher/courses/"
                                                        + course.getId()
                                                        + "/gradebook"),
                                        HttpMethod.GET,
                                        new HttpEntity<>(bearer(studentToken)),
                                        String.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        voidGrade(adminToken, second.id(), "Data entry error");
        EnrollmentGradeSummary afterVoid = myGrades(studentToken, enrollment.getId());
        assertThat(afterVoid.entries()).isEmpty();
        assertThat(afterVoid.finalScore()).isEqualByComparingTo("0.00");
        assertThat(afterVoid.components().get(0).graded()).isFalse();
        assertThat(afterVoid.complete()).isFalse();
        assertThat(afterVoid.history())
                .extracting(GradeEntryView::status)
                .containsExactly(GradeEntryStatus.VOIDED, GradeEntryStatus.REVISED);
    }

    @Test
    void teacherCannotWriteFeedbackOnAnotherTeachersCourse() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);

        User student = userFixtures.activeUser(RoleCode.STUDENT);
        var enrollment =
                enrollmentService.enroll(
                        course.getId(),
                        student.getId(),
                        EnrollmentSource.ADMIN,
                        admin.getId(),
                        null);

        User otherTeacher = userFixtures.activeUser(RoleCode.TEACHER);
        String otherTeacherToken = login(otherTeacher);

        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/teacher/feedback"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateFeedbackRequest(
                                        FeedbackTargetType.ENROLLMENT,
                                        enrollment.getId(),
                                        FeedbackVisibility.STUDENT_VISIBLE,
                                        "Great work!"),
                                bearer(otherTeacherToken)),
                        ApiError.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void courseOwnerCanWriteAndReadFeedbackOnEnrollment() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        var enrollment =
                enrollmentService.enroll(
                        course.getId(),
                        student.getId(),
                        EnrollmentSource.ADMIN,
                        admin.getId(),
                        null);

        ResponseEntity<FeedbackView> created =
                rest.exchange(
                        url("/api/v1/teacher/feedback"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateFeedbackRequest(
                                        FeedbackTargetType.ENROLLMENT,
                                        enrollment.getId(),
                                        FeedbackVisibility.STUDENT_VISIBLE,
                                        "Keep it up!"),
                                bearer(adminToken)),
                        FeedbackView.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<FeedbackView[]> listed =
                rest.exchange(
                        url(
                                "/api/v1/teacher/feedback?targetType=ENROLLMENT&targetId="
                                        + enrollment.getId()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        FeedbackView[].class);
        assertThat(listed.getBody()).hasSize(1);
        assertThat(listed.getBody()[0].bodyMarkdown()).isEqualTo("Keep it up!");
    }

    // ---------------------------------------------------------------- scaffolding

    private AssessmentComponent soleTopLevelComponent(UUID courseId) {
        List<AssessmentComponent> components = courseService.listAssessmentComponents(courseId);
        return components.stream()
                .filter(AssessmentComponent::isTopLevel)
                .findFirst()
                .orElseThrow();
    }

    private QuizDetail createQuiz(String adminToken, UUID courseId) {
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
                url("/api/v1/teacher/quizzes/" + quizId + "/translations/uz"),
                HttpMethod.PUT,
                new HttpEntity<>(
                        new UpsertQuizTranslationRequest("Test quiz", null, null),
                        bearer(adminToken)),
                QuizDetail.class);
        return rest.exchange(
                        url("/api/v1/teacher/quizzes/" + quizId),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        QuizDetail.class)
                .getBody();
    }

    private QuestionDetail createQuestion(String adminToken, UUID quizId, String points) {
        ResponseEntity<QuestionDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/quizzes/" + quizId + "/questions"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateQuestionRequest(
                                        QuestionType.SINGLE_CHOICE, new BigDecimal(points)),
                                bearer(adminToken)),
                        QuestionDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private AnswerOptionView addOption(String adminToken, UUID questionId, boolean isCorrect) {
        ResponseEntity<AnswerOptionView> response =
                rest.exchange(
                        url("/api/v1/teacher/questions/" + questionId + "/options"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateAnswerOptionRequest(isCorrect, null), bearer(adminToken)),
                        AnswerOptionView.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void publishQuiz(String adminToken, UUID quizId) {
        rest.exchange(
                url("/api/v1/teacher/quizzes/" + quizId + "/status"),
                HttpMethod.PUT,
                new HttpEntity<>(
                        new ChangeQuizStatusRequest(PublicationStatus.PUBLISHED),
                        bearer(adminToken)),
                QuizDetail.class);
    }

    @SuppressWarnings("unchecked")
    private UUID createModule(String adminToken, UUID courseId) {
        ResponseEntity<Map<String, Object>> created =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + courseId + "/modules"),
                        HttpMethod.POST,
                        new HttpEntity<>(Map.of(), bearer(adminToken)),
                        (Class<Map<String, Object>>) (Class<?>) Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID moduleId = UUID.fromString((String) created.getBody().get("id"));
        rest.exchange(
                url("/api/v1/teacher/modules/" + moduleId + "/translations/uz"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("title", "Fixture module"), bearer(adminToken)),
                Object.class);
        rest.exchange(
                url("/api/v1/teacher/modules/" + moduleId + "/status"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("status", "PUBLISHED"), bearer(adminToken)),
                Object.class);
        return moduleId;
    }

    @SuppressWarnings("unchecked")
    private UUID createGradedQuizActivity(
            String adminToken,
            UUID courseId,
            UUID moduleId,
            UUID quizId,
            UUID assessmentComponentId) {
        Map<String, Object> createBody = new java.util.HashMap<>();
        createBody.put("moduleId", moduleId.toString());
        createBody.put("activityType", "QUIZ");
        createBody.put("isGraded", true);
        createBody.put("assessmentComponentId", assessmentComponentId.toString());
        createBody.put("maxPoints", 100);
        ResponseEntity<Map<String, Object>> created =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + courseId + "/activities"),
                        HttpMethod.POST,
                        new HttpEntity<>(createBody, bearer(adminToken)),
                        (Class<Map<String, Object>>) (Class<?>) Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID activityId = UUID.fromString((String) created.getBody().get("id"));

        Map<String, Object> updateBody = new java.util.HashMap<>();
        updateBody.put("referenceId", quizId.toString());
        updateBody.put("collaborationMode", "INDIVIDUAL");
        updateBody.put("variantStrategy", "NONE");
        ResponseEntity<Object> updated =
                rest.exchange(
                        url("/api/v1/teacher/activities/" + activityId),
                        HttpMethod.PATCH,
                        new HttpEntity<>(updateBody, bearer(adminToken)),
                        Object.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);

        rest.exchange(
                url("/api/v1/teacher/activities/" + activityId + "/translations/uz"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("title", "Graded quiz activity"), bearer(adminToken)),
                Object.class);
        rest.exchange(
                url("/api/v1/teacher/activities/" + activityId + "/status"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("status", "PUBLISHED"), bearer(adminToken)),
                Object.class);
        return activityId;
    }

    private AttemptDetail startAttempt(String studentToken, UUID quizId, UUID activityId) {
        ResponseEntity<AttemptDetail> response =
                rest.exchange(
                        url("/api/v1/quizzes/" + quizId + "/attempts"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new StartAttemptRequest(null, activityId), bearer(studentToken)),
                        AttemptDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void answer(
            String studentToken, UUID attemptId, UUID questionId, UUID correctOptionId) {
        rest.exchange(
                url("/api/v1/attempts/" + attemptId + "/questions/" + questionId + "/answer"),
                HttpMethod.POST,
                new HttpEntity<>(
                        new RecordAnswerRequest(
                                null,
                                null,
                                List.of(new SelectedOptionInput(correctOptionId, null))),
                        bearer(studentToken)),
                Object.class);
    }

    private void submit(String studentToken, UUID attemptId) {
        ResponseEntity<AttemptDetail> response =
                rest.exchange(
                        url("/api/v1/attempts/" + attemptId + "/submit"),
                        HttpMethod.POST,
                        new HttpEntity<>(bearer(studentToken)),
                        AttemptDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private EnrollmentGradeSummary myGrades(String studentToken, UUID enrollmentId) {
        ResponseEntity<EnrollmentGradeSummary> response =
                rest.exchange(
                        url("/api/v1/enrollments/" + enrollmentId + "/grades"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(studentToken)),
                        EnrollmentGradeSummary.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private GradeEntryView recordManualGrade(
            String adminToken, UUID enrollmentId, UUID componentId, String score, String maxScore) {
        ResponseEntity<GradeEntryView> response =
                rest.exchange(
                        url("/api/v1/teacher/enrollments/" + enrollmentId + "/grades"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new RecordManualGradeRequest(
                                        componentId,
                                        new BigDecimal(score),
                                        new BigDecimal(maxScore),
                                        null),
                                bearer(adminToken)),
                        GradeEntryView.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void voidGrade(String adminToken, UUID gradeEntryId, String reason) {
        ResponseEntity<Void> response =
                rest.exchange(
                        url("/api/v1/teacher/grades/" + gradeEntryId + "/void"),
                        HttpMethod.POST,
                        new HttpEntity<>(new VoidGradeRequest(reason), bearer(adminToken)),
                        Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
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
