package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
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
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.AssessmentComponent;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.AttemptDetail;
import uz.aifu.lms.assessment.api.dto.AttemptDtos.StartAttemptRequest;
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
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.grading.api.dto.GradeDtos.EnrollmentGradeSummary;
import uz.aifu.lms.grading.api.dto.GradeDtos.RecordManualGradeRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.progress.api.dto.ProgressDtos.LessonPathView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.ReadingBeatRequest;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * ADR-004 Layer 3's acceptance test, referenced by CLAUDE.md §7.3 and the per-phase quality gate
 * (phase-plan.md) but never previously written as its own class — a real, explicit gap closed in
 * Phase 12. Asserts the platform-wide invariant across several resource types at once: a student
 * reading another student's own-scoped resource by id gets {@code 404}, never {@code 403} — ADR-004
 * is explicit that existence is not leaked. Coding-submission ownership isolation is already
 * covered by {@code CodingSubmissionIT.submission_ownedByAnotherStudent_isNotVisible}; not
 * duplicated here.
 */
class StudentDataIsolationIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollments;
    @Autowired private CourseService courseService;
    @Autowired private JdbcTemplate jdbc;
    @LocalServerPort private int port;

    @Test
    void quizAttempt_ownedByAnotherStudent_isNotVisible_butOwnAttemptIs() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        QuizDetail quiz = createQuiz(adminToken, course.getId());
        QuestionDetail question = createQuestion(adminToken, quiz.id());
        addOption(adminToken, question.id(), true);
        addOption(adminToken, question.id(), false);
        publishQuiz(adminToken, quiz.id());

        User owner = userFixtures.activeUser(RoleCode.STUDENT);
        User intruder = userFixtures.activeUser(RoleCode.STUDENT);
        enrollments.enroll(
                course.getId(), owner.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        enrollments.enroll(
                course.getId(), intruder.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String ownerToken = login(owner);
        String intruderToken = login(intruder);

        AttemptDetail ownerAttempt = startAttempt(ownerToken, quiz.id());

        ResponseEntity<ApiError> stolen =
                rest.exchange(
                        url("/api/v1/attempts/" + ownerAttempt.id()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(intruderToken)),
                        ApiError.class);
        assertThat(stolen.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(stolen.getBody().code()).isEqualTo("RESOURCE_NOT_FOUND");

        ResponseEntity<AttemptDetail> own =
                rest.exchange(
                        url("/api/v1/attempts/" + ownerAttempt.id()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(ownerToken)),
                        AttemptDetail.class);
        assertThat(own.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void grades_ownedByAnotherStudent_areNotVisible_butOwnGradesAre() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        AssessmentComponent jn = soleTopLevelComponent(course.getId());

        User owner = userFixtures.activeUser(RoleCode.STUDENT);
        User intruder = userFixtures.activeUser(RoleCode.STUDENT);
        Enrollment ownerEnrollment =
                enrollments.enroll(
                        course.getId(), owner.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        enrollments.enroll(
                course.getId(), intruder.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String ownerToken = login(owner);
        String intruderToken = login(intruder);

        rest.exchange(
                url("/api/v1/teacher/enrollments/" + ownerEnrollment.getId() + "/grades"),
                HttpMethod.POST,
                new HttpEntity<>(
                        new RecordManualGradeRequest(
                                jn.getId(), BigDecimal.valueOf(80), BigDecimal.valueOf(100), null),
                        bearer(adminToken)),
                String.class);

        ResponseEntity<ApiError> stolen =
                rest.exchange(
                        url("/api/v1/enrollments/" + ownerEnrollment.getId() + "/grades"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(intruderToken)),
                        ApiError.class);
        assertThat(stolen.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(stolen.getBody().code()).isEqualTo("RESOURCE_NOT_FOUND");

        ResponseEntity<EnrollmentGradeSummary> own =
                rest.exchange(
                        url("/api/v1/enrollments/" + ownerEnrollment.getId() + "/grades"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(ownerToken)),
                        EnrollmentGradeSummary.class);
        assertThat(own.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(own.getBody().entries()).isNotEmpty();
    }

    @Test
    void lessonPathAndReadingHeartbeat_ofAnotherCoursesStudent_areNotVisible_butOwnAre() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        Course course = enrollmentFixtures.publishedCourse(150);
        UUID lessonId = publishedLesson(course.getId());

        User owner = userFixtures.activeUser(RoleCode.STUDENT);
        User intruder = userFixtures.activeUser(RoleCode.STUDENT); // never enrolled
        enrollments.enroll(
                course.getId(), owner.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        String ownerToken = login(owner);
        String intruderToken = login(intruder);

        ResponseEntity<ApiError> stolenPath =
                rest.exchange(
                        url("/api/v1/lessons/" + lessonId + "/path"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(intruderToken)),
                        ApiError.class);
        assertThat(stolenPath.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<ApiError> stolenBeat =
                rest.exchange(
                        url("/api/v1/lessons/" + lessonId + "/reading"),
                        HttpMethod.POST,
                        new HttpEntity<>(new ReadingBeatRequest(0, false), bearer(intruderToken)),
                        ApiError.class);
        assertThat(stolenBeat.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<LessonPathView> ownPath =
                rest.exchange(
                        url("/api/v1/lessons/" + lessonId + "/path"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(ownerToken)),
                        LessonPathView.class);
        assertThat(ownPath.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<LessonPathView> ownBeat =
                rest.exchange(
                        url("/api/v1/lessons/" + lessonId + "/reading"),
                        HttpMethod.POST,
                        new HttpEntity<>(new ReadingBeatRequest(0, false), bearer(ownerToken)),
                        LessonPathView.class);
        assertThat(ownBeat.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private UUID publishedLesson(UUID courseId) {
        UUID moduleId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO course_modules (id, course_id, position, status, created_at, updated_at, version) "
                        + "VALUES (?, ?, 0, 'PUBLISHED', now(), now(), 0)",
                moduleId,
                courseId);
        jdbc.update(
                "INSERT INTO course_module_translations (module_id, locale, title) VALUES (?, 'uz', 'Modul')",
                moduleId);
        UUID lessonId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO lessons (id, module_id, position, session_type, status, is_required, estimated_minutes, created_at, updated_at, version) "
                        + "VALUES (?, ?, 0, 'LECTURE', 'PUBLISHED', true, 1, now(), now(), 0)",
                lessonId,
                moduleId);
        jdbc.update(
                "INSERT INTO lesson_translations (lesson_id, locale, title) VALUES (?, 'uz', 'Dars')",
                lessonId);
        return lessonId;
    }

    private AssessmentComponent soleTopLevelComponent(UUID courseId) {
        return courseService.listAssessmentComponents(courseId).stream()
                .filter(c -> c.getParent() == null)
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
        return created.getBody();
    }

    private QuestionDetail createQuestion(String adminToken, UUID quizId) {
        ResponseEntity<QuestionDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/quizzes/" + quizId + "/questions"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateQuestionRequest(
                                        QuestionType.SINGLE_CHOICE, BigDecimal.TEN),
                                bearer(adminToken)),
                        QuestionDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void addOption(String adminToken, UUID questionId, boolean isCorrect) {
        ResponseEntity<Object> response =
                rest.exchange(
                        url("/api/v1/teacher/questions/" + questionId + "/options"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateAnswerOptionRequest(isCorrect, null), bearer(adminToken)),
                        Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
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
