package uz.aifu.lms.grading;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.grading.api.dto.GradeDtos.EnrollmentGradeSummary;
import uz.aifu.lms.grading.api.dto.GradeDtos.GradeEntryView;
import uz.aifu.lms.grading.domain.GradeEntryStatus;
import uz.aifu.lms.grading.domain.GradeSource;
import uz.aifu.lms.grading.domain.GradeSourceRefType;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Verifies owner decision that with multiple quiz attempts, the BEST score counts: A first attempt
 * 90 then a second attempt 40 leaves one published grade of 90; then a third attempt scoring 100
 * supersedes it with revision 2.
 */
@Tag("it")
class BestScoreGradeIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private CourseService courseService;
    @LocalServerPort private int port;

    @Test
    void quizAttempts_bestScoreCounts_first90_second40Keeps90_third100SupersedesWithRevision2() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        AssessmentComponent jn = soleTopLevelComponent(course.getId());

        QuizDetail quiz = createQuiz(adminToken, course.getId());

        // Question 1: 50 points
        QuestionDetail q1 = createQuestion(adminToken, quiz.id(), "50");
        AnswerOptionView q1Correct = addOption(adminToken, q1.id(), true);
        AnswerOptionView q1Incorrect = addOption(adminToken, q1.id(), false);

        // Question 2: 40 points
        QuestionDetail q2 = createQuestion(adminToken, quiz.id(), "40");
        AnswerOptionView q2Correct = addOption(adminToken, q2.id(), true);
        AnswerOptionView q2Incorrect = addOption(adminToken, q2.id(), false);

        // Question 3: 10 points
        QuestionDetail q3 = createQuestion(adminToken, quiz.id(), "10");
        AnswerOptionView q3Correct = addOption(adminToken, q3.id(), true);
        AnswerOptionView q3Incorrect = addOption(adminToken, q3.id(), false);

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

        // --- ATTEMPT 1: score 90 (Q1 correct: 50, Q2 correct: 40, Q3 incorrect: 0) ---
        AttemptDetail attempt1 = startAttempt(studentToken, quiz.id(), activityId);
        answer(studentToken, attempt1.id(), q1.id(), q1Correct.id());
        answer(studentToken, attempt1.id(), q2.id(), q2Correct.id());
        answer(studentToken, attempt1.id(), q3.id(), q3Incorrect.id());
        submit(studentToken, attempt1.id());

        EnrollmentGradeSummary afterFirst = myGrades(studentToken, enrollment.getId());
        assertThat(afterFirst.entries()).hasSize(1);
        GradeEntryView firstGrade = afterFirst.entries().get(0);
        assertThat(firstGrade.source()).isEqualTo(GradeSource.QUIZ);
        assertThat(firstGrade.sourceRefType()).isEqualTo(GradeSourceRefType.QUIZ_ATTEMPT);
        assertThat(firstGrade.sourceRefId()).isEqualTo(attempt1.id());
        assertThat(firstGrade.status()).isEqualTo(GradeEntryStatus.PUBLISHED);
        assertThat(firstGrade.revision()).isEqualTo(1);
        assertThat(firstGrade.score()).isEqualByComparingTo("90");
        assertThat(firstGrade.maxScore()).isEqualByComparingTo("100");
        assertThat(afterFirst.finalScore()).isEqualByComparingTo("90.00");

        // --- ATTEMPT 2: score 40 (Q1 incorrect: 0, Q2 correct: 40, Q3 incorrect: 0) ---
        AttemptDetail attempt2 = startAttempt(studentToken, quiz.id(), activityId);
        answer(studentToken, attempt2.id(), q1.id(), q1Incorrect.id());
        answer(studentToken, attempt2.id(), q2.id(), q2Correct.id());
        answer(studentToken, attempt2.id(), q3.id(), q3Incorrect.id());
        submit(studentToken, attempt2.id());

        // A second attempt 40 leaves one published grade of 90:
        // do NOT supersede it and do not create a new row
        EnrollmentGradeSummary afterSecond = myGrades(studentToken, enrollment.getId());
        assertThat(afterSecond.entries()).hasSize(1);
        GradeEntryView gradeAfterSecond = afterSecond.entries().get(0);
        assertThat(gradeAfterSecond.id()).isEqualTo(firstGrade.id());
        assertThat(gradeAfterSecond.revision()).isEqualTo(1);
        assertThat(gradeAfterSecond.score()).isEqualByComparingTo("90");
        assertThat(gradeAfterSecond.maxScore()).isEqualByComparingTo("100");
        assertThat(gradeAfterSecond.status()).isEqualTo(GradeEntryStatus.PUBLISHED);
        assertThat(afterSecond.history()).hasSize(1);
        assertThat(afterSecond.finalScore()).isEqualByComparingTo("90.00");

        // --- ATTEMPT 3: score 100 (Q1 correct: 50, Q2 correct: 40, Q3 correct: 10) ---
        AttemptDetail attempt3 = startAttempt(studentToken, quiz.id(), activityId);
        answer(studentToken, attempt3.id(), q1.id(), q1Correct.id());
        answer(studentToken, attempt3.id(), q2.id(), q2Correct.id());
        answer(studentToken, attempt3.id(), q3.id(), q3Correct.id());
        submit(studentToken, attempt3.id());

        // A third scoring 100 supersedes it with revision 2
        EnrollmentGradeSummary afterThird = myGrades(studentToken, enrollment.getId());
        assertThat(afterThird.entries()).hasSize(1);
        GradeEntryView thirdGrade = afterThird.entries().get(0);
        assertThat(thirdGrade.source()).isEqualTo(GradeSource.QUIZ);
        assertThat(thirdGrade.sourceRefType()).isEqualTo(GradeSourceRefType.QUIZ_ATTEMPT);
        assertThat(thirdGrade.sourceRefId()).isEqualTo(attempt3.id());
        assertThat(thirdGrade.status()).isEqualTo(GradeEntryStatus.PUBLISHED);
        assertThat(thirdGrade.revision()).isEqualTo(2);
        assertThat(thirdGrade.supersedesId()).isEqualTo(firstGrade.id());
        assertThat(thirdGrade.score()).isEqualByComparingTo("100");
        assertThat(thirdGrade.maxScore()).isEqualByComparingTo("100");
        assertThat(afterThird.finalScore()).isEqualByComparingTo("100.00");

        // History contains revision 2 (PUBLISHED, 100) and revision 1 (REVISED, 90)
        assertThat(afterThird.history()).hasSize(2);
        assertThat(afterThird.history().get(0).id()).isEqualTo(thirdGrade.id());
        assertThat(afterThird.history().get(0).status()).isEqualTo(GradeEntryStatus.PUBLISHED);
        assertThat(afterThird.history().get(0).revision()).isEqualTo(2);
        assertThat(afterThird.history().get(0).score()).isEqualByComparingTo("100");

        assertThat(afterThird.history().get(1).id()).isEqualTo(firstGrade.id());
        assertThat(afterThird.history().get(1).status()).isEqualTo(GradeEntryStatus.REVISED);
        assertThat(afterThird.history().get(1).revision()).isEqualTo(1);
        assertThat(afterThird.history().get(1).score()).isEqualByComparingTo("90");
    }

    // ---------------------------------------------------------------- scaffolding helpers

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
