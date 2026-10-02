package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.AssessmentComponent;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.analytics.api.dto.AnalyticsDtos.CourseCompletionSummary;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.files.api.dto.FileDtos.FileDetail;
import uz.aifu.lms.grading.api.dto.GradeDtos.RecordManualGradeRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.search.api.dto.SearchDtos.SearchResultView;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 11 (analytics, notifications, search, files) — real HTTP against real Postgres, no mocks.
 */
class Phase11IT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollments;
    @Autowired private CourseService courseService;
    @LocalServerPort private int port;

    @Test
    void enrolling_publishesNotification_readableAndMarkableByTheStudent() {
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String studentToken = login(student);

        enrollments.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);

        ResponseEntity<PageResponse> listResponse =
                rest.exchange(
                        url("/api/v1/notifications/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(studentToken)),
                        PageResponse.class);
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> content = listResponse.getBody().content();
        assertThat(content).isNotEmpty();

        ResponseEntity<PageResponse> unreadOnly =
                rest.exchange(
                        url("/api/v1/notifications/me?unreadOnly=true"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(studentToken)),
                        PageResponse.class);
        assertThat(unreadOnly.getBody().content()).isNotEmpty();
    }

    @Test
    void recordingAManualGrade_publishesGradePublishedNotification() {
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        var enrollment =
                enrollments.enroll(
                        course.getId(),
                        student.getId(),
                        EnrollmentSource.ADMIN,
                        admin.getId(),
                        null);
        AssessmentComponent jn = soleTopLevelComponent(course.getId());

        ResponseEntity<String> recordResponse =
                rest.exchange(
                        url("/api/v1/teacher/enrollments/" + enrollment.getId() + "/grades"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new RecordManualGradeRequest(
                                        jn.getId(),
                                        BigDecimal.valueOf(70),
                                        BigDecimal.valueOf(100),
                                        null),
                                bearer(adminToken)),
                        String.class);
        assertThat(recordResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String studentToken = login(student);
        ResponseEntity<PageResponse> response =
                rest.exchange(
                        url("/api/v1/notifications/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(studentToken)),
                        PageResponse.class);
        List<?> content = response.getBody().content();
        assertThat(content)
                .anyMatch(row -> ((java.util.Map<?, ?>) row).get("type").equals("GRADE_PUBLISHED"));
        // the notification says, typed, what it is about: enough to name the course and open it
        java.util.Map<?, ?> grade =
                content.stream()
                        .map(row -> (java.util.Map<?, ?>) row)
                        .filter(row -> row.get("type").equals("GRADE_PUBLISHED"))
                        .findFirst()
                        .orElseThrow();
        java.util.Map<?, ?> context = (java.util.Map<?, ?>) grade.get("context");
        assertThat(context.get("courseId")).isEqualTo(course.getId().toString());
        assertThat(context.get("enrollmentId")).isEqualTo(enrollment.getId().toString());
        assertThat(context.get("finalScore")).isEqualTo("70.00");
    }

    @Test
    void search_findsAPublishedCourse_byItsTitle() {
        Course course = enrollmentFixtures.publishedCourse(150);
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);

        // limit=100 (the service cap): this suite's shared Testcontainers Postgres accumulates a
        // "Fixture course"/"Fixture subject" row from every other IT class in the same JVM run, so
        // a small default limit combined with tied ts_rank on identical titles is genuinely flaky
        // — the fix is requesting enough rows to guarantee this course is included, not a narrower
        // query (see support/AbstractIntegrationTest's class doc on why fixtures never collide but
        // also never disappear within one run).
        ResponseEntity<SearchResultView[]> response =
                rest.exchange(
                        url("/api/v1/search?q=Fixture&locale=uz&limit=100"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        SearchResultView[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .extracting(SearchResultView::entityId)
                .contains(course.getId());
    }

    @Test
    void search_matchesWordsAsTheyAreTyped_andOnlyInCoursesTheCallerCanOpen() {
        Course course = enrollmentFixtures.publishedCourse(150);
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        User enrolled = userFixtures.activeUser(RoleCode.STUDENT);
        User outsider = userFixtures.activeUser(RoleCode.STUDENT);
        enrollments.enroll(
                course.getId(), enrolled.getId(), EnrollmentSource.ADMIN, admin.getId(), null);

        // "Fixt" is the beginning of "Fixture course …": a prefix finds it
        assertThat(search(login(enrolled), "Fixt"))
                .extracting(SearchResultView::entityId)
                .containsExactly(course.getId());
        assertThat(search(login(enrolled), "Fixt").get(0).courseId()).isEqualTo(course.getId());
        assertThat(search(login(enrolled), "Fixt").get(0).resultType()).isEqualTo("COURSE");
        // a student who is not in the course cannot find it, however published it is
        assertThat(search(login(outsider), "Fixt")).isEmpty();
        // nothing typed can be read as query syntax
        assertThat(search(login(enrolled), "Fixt & | ! ( :* ')")).isNotEmpty();
        assertThat(search(login(enrolled), "!!! ((( ")).isEmpty();
    }

    private List<SearchResultView> search(String token, String text) {
        ResponseEntity<SearchResultView[]> response =
                rest.exchange(
                        url("/api/v1/search?locale=uz&limit=100&q={q}"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(token)),
                        SearchResultView[].class,
                        text);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return List.of(response.getBody());
    }

    @Test
    void analyticsCourseCompletion_isCourseEditorGated() {
        Course course = enrollmentFixtures.publishedCourse(150);
        User admin0 = userFixtures.activeUser(RoleCode.ADMIN);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollments.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin0.getId(), null);
        User unrelatedTeacher = userFixtures.activeUser(RoleCode.TEACHER);
        String unrelatedToken = login(unrelatedTeacher);

        ResponseEntity<String> forbidden =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + course.getId() + "/analytics/completion"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(unrelatedToken)),
                        String.class);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        ResponseEntity<CourseCompletionSummary> ok =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + course.getId() + "/analytics/completion"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        CourseCompletionSummary.class);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ok.getBody().courseId()).isEqualTo(course.getId());
    }

    @Test
    void fileUpload_privateVisibility_onlyUploaderAndAdminCanRead_thenDeleteHidesIt() {
        User teacher = userFixtures.activeUser(RoleCode.TEACHER);
        User otherTeacher = userFixtures.activeUser(RoleCode.TEACHER);
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String teacherToken = login(teacher);
        String otherToken = login(otherTeacher);
        String adminToken = login(admin);

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add(
                "file",
                new ByteArrayResource("hello".getBytes(StandardCharsets.UTF_8)) {
                    @Override
                    public String getFilename() {
                        return "note.txt";
                    }
                });
        HttpHeaders uploadHeaders = bearer(teacherToken);
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<FileDetail> uploadResponse =
                rest.exchange(
                        url("/api/v1/files?visibility=PRIVATE"),
                        HttpMethod.POST,
                        new HttpEntity<>(form, uploadHeaders),
                        FileDetail.class);
        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID fileId = uploadResponse.getBody().id();

        ResponseEntity<FileDetail> ownerRead =
                rest.exchange(
                        url("/api/v1/files/" + fileId),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(teacherToken)),
                        FileDetail.class);
        assertThat(ownerRead.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> otherRead =
                rest.exchange(
                        url("/api/v1/files/" + fileId),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(otherToken)),
                        String.class);
        assertThat(otherRead.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<FileDetail> adminRead =
                rest.exchange(
                        url("/api/v1/files/" + fileId),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        FileDetail.class);
        assertThat(adminRead.getStatusCode()).isEqualTo(HttpStatus.OK);

        rest.exchange(
                url("/api/v1/files/" + fileId),
                HttpMethod.DELETE,
                new HttpEntity<>(bearer(adminToken)),
                Void.class);

        ResponseEntity<String> afterDelete =
                rest.exchange(
                        url("/api/v1/files/" + fileId),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(teacherToken)),
                        String.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private AssessmentComponent soleTopLevelComponent(UUID courseId) {
        return courseService.listAssessmentComponents(courseId).stream()
                .filter(c -> c.getParent() == null)
                .findFirst()
                .orElseThrow();
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
