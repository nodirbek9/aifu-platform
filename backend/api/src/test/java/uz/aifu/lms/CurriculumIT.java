package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

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
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.ActivityDetail;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.CreateActivityRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.UpdateActivityRequest;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.ContentBlockView;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.CreateContentBlockRequest;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.UpsertContentBlockTranslationRequest;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonDetail;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ChangeModuleStatusRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.CreateModuleRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ModuleDetail;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ModuleSummary;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ReorderModulesRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.UpsertModuleTranslationRequest;
import uz.aifu.lms.curriculum.app.ModuleService;
import uz.aifu.lms.curriculum.domain.ActivityType;
import uz.aifu.lms.curriculum.domain.CollaborationMode;
import uz.aifu.lms.curriculum.domain.CourseModule;
import uz.aifu.lms.curriculum.domain.VariantStrategy;
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
 * Phase 5 domain invariants (docs/planning/phase-plan.md): locale fallback metadata, translation
 * completeness gate, unpublished content invisible to students, reorder integrity, and hostile HTML
 * rejection — all against real Postgres.
 */
class CurriculumIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private ModuleService moduleService;
    @LocalServerPort private int port;

    @Test
    void publishModule_withoutTranslation_isRejected() {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        ModuleDetail module = createModule(adminToken, course.getId());

        ResponseEntity<ApiError> response =
                changeModuleStatus(adminToken, module.id(), "PUBLISHED", ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("TRANSLATION_INCOMPLETE");
    }

    @Test
    void moduleList_missingRequestedLocale_fallsBackAndFlagsIt() {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        ModuleDetail module = createModule(adminToken, course.getId());
        upsertModuleTranslation(adminToken, module.id(), "uz", "Diskret tuzilmalar");
        changeModuleStatus(adminToken, module.id(), "PUBLISHED", ModuleDetail.class);

        ResponseEntity<PageResponse<ModuleSummary>> response =
                rest.exchange(
                        url("/api/v1/courses/" + course.getId() + "/modules?locale=ru"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        new ParameterizedTypeReference<>() {});

        ModuleSummary summary = response.getBody().content().get(0);
        assertThat(summary.title()).isEqualTo("Diskret tuzilmalar");
        assertThat(summary.titleIsFallback()).isTrue();
    }

    @Test
    void unpublishedModule_invisibleToStudent_visibleToAdmin() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
        createModule(adminToken, course.getId()); // stays DRAFT: never translated/published

        ResponseEntity<PageResponse<ModuleSummary>> asStudent =
                rest.exchange(
                        url("/api/v1/courses/" + course.getId() + "/modules"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(student))),
                        new ParameterizedTypeReference<>() {});
        assertThat(asStudent.getBody().content()).isEmpty();

        ResponseEntity<PageResponse<ModuleDetail>> asAdmin =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + course.getId() + "/modules"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        new ParameterizedTypeReference<>() {});
        assertThat(asAdmin.getBody().content()).hasSize(1);
    }

    @Test
    void unpublishedLesson_directGet_isNotFoundForEnrolledStudent() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);

        ModuleDetail module = createModule(adminToken, course.getId());
        upsertModuleTranslation(adminToken, module.id(), "uz", "Modul 1");
        changeModuleStatus(adminToken, module.id(), "PUBLISHED", ModuleDetail.class);
        LessonDetail lesson = createLesson(adminToken, module.id()); // never published

        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/lessons/" + lesson.id()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(student))),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void reorder_modulesPersistNewOrder() {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        ModuleDetail m1 = createModule(adminToken, course.getId());
        ModuleDetail m2 = createModule(adminToken, course.getId());
        ModuleDetail m3 = createModule(adminToken, course.getId());
        assertThat(List.of(m1.position(), m2.position(), m3.position())).containsExactly(0, 1, 2);

        ResponseEntity<Void> reorder =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + course.getId() + "/modules/reorder"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new ReorderModulesRequest(List.of(m3.id(), m1.id(), m2.id())),
                                bearer(adminToken)),
                        Void.class);
        assertThat(reorder.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        List<CourseModule> reordered = moduleService.listByCourse(course.getId());
        assertThat(reordered)
                .extracting(CourseModule::getId)
                .containsExactly(m3.id(), m1.id(), m2.id());
        assertThat(reordered).extracting(CourseModule::getPosition).containsExactly(0, 1, 2);
    }

    @Test
    void contentBlock_hostileHtml_isRejected() throws Exception {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        ModuleDetail module = createModule(adminToken, course.getId());
        LessonDetail lesson = createLesson(adminToken, module.id());
        ContentBlockView block = createTextBlock(adminToken, lesson.id());

        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/teacher/blocks/" + block.id() + "/translations/uz"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new UpsertContentBlockTranslationRequest(
                                        objectMapper()
                                                .readTree(
                                                        "{\"html\":\"<p>hi</p><script>alert(1)</script>\"}")),
                                bearer(adminToken)),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("UNSAFE_CONTENT_REJECTED");
    }

    @Test
    void contentBlock_sanitizedContent_rendersToEnrolledStudent() throws Exception {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(admin);
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                course.getId(), student.getId(), EnrollmentSource.ADMIN, admin.getId(), null);

        ModuleDetail module = createModule(adminToken, course.getId());
        upsertModuleTranslation(adminToken, module.id(), "uz", "Modul 1");
        changeModuleStatus(adminToken, module.id(), "PUBLISHED", ModuleDetail.class);
        LessonDetail lesson = createLesson(adminToken, module.id());
        upsertLessonTranslation(adminToken, lesson.id(), "uz", "Dars 1");
        ContentBlockView block = createTextBlock(adminToken, lesson.id());
        rest.exchange(
                url("/api/v1/teacher/blocks/" + block.id() + "/translations/uz"),
                HttpMethod.PUT,
                new HttpEntity<>(
                        new UpsertContentBlockTranslationRequest(
                                objectMapper()
                                        .readTree(
                                                "{\"html\":\"<p>Salom <strong>dunyo</strong></p>\"}")),
                        bearer(adminToken)),
                ContentBlockView.class);
        changeLessonStatus(adminToken, lesson.id(), "PUBLISHED");

        ResponseEntity<LessonDetail> response =
                rest.exchange(
                        url("/api/v1/lessons/" + lesson.id()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(login(student))),
                        LessonDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().blocks()).hasSize(1);
        assertThat(response.getBody().blocks().get(0).payload().get("html").asText())
                .contains("<strong>dunyo</strong>");
    }

    @Test
    void quizActivity_withoutReference_isRejected() {
        String adminToken = adminToken();
        Course course = enrollmentFixtures.publishedCourse(150);
        ModuleDetail module = createModule(adminToken, course.getId());
        LessonDetail lesson = createLesson(adminToken, module.id());

        ResponseEntity<ActivityDetail> created =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + course.getId() + "/activities"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateActivityRequest(
                                        null, lesson.id(), ActivityType.QUIZ, false, null, null),
                                bearer(adminToken)),
                        ActivityDetail.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/teacher/activities/" + created.getBody().id()),
                        HttpMethod.PATCH,
                        new HttpEntity<>(
                                new UpdateActivityRequest(
                                        null,
                                        null,
                                        null,
                                        CollaborationMode.INDIVIDUAL,
                                        VariantStrategy.NONE,
                                        null,
                                        null),
                                bearer(adminToken)),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_FAILED");
    }

    private ModuleDetail createModule(String adminToken, UUID courseId) {
        ResponseEntity<ModuleDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + courseId + "/modules"),
                        HttpMethod.POST,
                        new HttpEntity<>(new CreateModuleRequest(null, null), bearer(adminToken)),
                        ModuleDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void upsertModuleTranslation(
            String adminToken, UUID moduleId, String locale, String title) {
        ResponseEntity<ModuleDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/modules/" + moduleId + "/translations/" + locale),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new UpsertModuleTranslationRequest(title, null),
                                bearer(adminToken)),
                        ModuleDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private <T> ResponseEntity<T> changeModuleStatus(
            String adminToken, UUID moduleId, String status, Class<T> responseType) {
        return rest.exchange(
                url("/api/v1/teacher/modules/" + moduleId + "/status"),
                HttpMethod.PUT,
                new HttpEntity<>(
                        new ChangeModuleStatusRequest(
                                uz.aifu.lms.common.domain.PublicationStatus.valueOf(status)),
                        bearer(adminToken)),
                responseType);
    }

    private LessonDetail createLesson(String adminToken, UUID moduleId) {
        ResponseEntity<LessonDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/modules/" + moduleId + "/lessons"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new uz.aifu.lms.curriculum.api.dto.LessonDtos.CreateLessonRequest(
                                        null, uz.aifu.lms.academics.domain.SessionType.LECTURE),
                                bearer(adminToken)),
                        LessonDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void upsertLessonTranslation(
            String adminToken, UUID lessonId, String locale, String title) {
        rest.exchange(
                url("/api/v1/teacher/lessons/" + lessonId + "/translations/" + locale),
                HttpMethod.PUT,
                new HttpEntity<>(
                        new uz.aifu.lms.curriculum.api.dto.LessonDtos
                                .UpsertLessonTranslationRequest(title, null),
                        bearer(adminToken)),
                LessonDetail.class);
    }

    private void changeLessonStatus(String adminToken, UUID lessonId, String status) {
        ResponseEntity<LessonDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/lessons/" + lessonId + "/status"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new uz.aifu.lms.curriculum.api.dto.LessonDtos
                                        .ChangeLessonStatusRequest(
                                        uz.aifu.lms.common.domain.PublicationStatus.valueOf(
                                                status)),
                                bearer(adminToken)),
                        LessonDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ContentBlockView createTextBlock(String adminToken, UUID lessonId) {
        ResponseEntity<ContentBlockView> response =
                rest.exchange(
                        url("/api/v1/teacher/lessons/" + lessonId + "/blocks"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateContentBlockRequest(
                                        uz.aifu.lms.curriculum.domain.ContentBlockType.TEXT,
                                        null,
                                        null,
                                        true,
                                        uz.aifu.lms.curriculum.domain.BlockVisibility.ALL),
                                bearer(adminToken)),
                        ContentBlockView.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private String adminToken() {
        return login(userFixtures.activeUser(RoleCode.ADMIN));
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

    private com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
