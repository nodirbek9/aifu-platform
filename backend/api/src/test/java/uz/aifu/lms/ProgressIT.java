package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.CreateLessonRequest;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonDetail;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.UpsertLessonTranslationRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ChangeModuleStatusRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.CreateModuleRequest;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.ModuleDetail;
import uz.aifu.lms.curriculum.api.dto.ModuleDtos.UpsertModuleTranslationRequest;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.progress.api.dto.ProgressDtos.CourseProgressView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.LessonProgressView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.RecordLessonProgressRequest;
import uz.aifu.lms.progress.api.dto.ProgressDtos.StudentCourseProgressSummary;
import uz.aifu.lms.progress.domain.ProgressStatus;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 6 DoD (docs/planning/phase-plan.md): idempotent completion, percentage roll-up, concurrent
 * updates (optimistic-lock retry), {@code last_accessed_at}, and cross-student isolation — all
 * against real Postgres, through the actual HTTP endpoints.
 */
class ProgressIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @LocalServerPort private int port;

    @Test
    void completingALessonTwice_doesNotDoubleCount() {
        User adminUser = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(adminUser);
        CourseWithLessons setup = courseWithPublishedLessons(adminToken, 2);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                setup.course.getId(),
                student.getId(),
                EnrollmentSource.ADMIN,
                adminUser.getId(),
                null);
        String studentToken = login(student);

        ResponseEntity<LessonProgressView> first =
                recordProgress(studentToken, setup.lessonIds.get(0), ProgressStatus.COMPLETED);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        var firstCompletedAt = first.getBody().completedAt();
        assertThat(firstCompletedAt).isNotNull();

        ResponseEntity<LessonProgressView> second =
                recordProgress(studentToken, setup.lessonIds.get(0), ProgressStatus.COMPLETED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        // PostgreSQL rounds to microseconds; the first response still carries the JVM clock's finer
        // value, so the two may differ by up to one microsecond.
        assertThat(second.getBody().completedAt())
                .isCloseTo(
                        firstCompletedAt,
                        org.assertj.core.api.Assertions.within(
                                1, java.time.temporal.ChronoUnit.MICROS));
        assertThat(second.getBody().viewCount()).isEqualTo(2);

        CourseProgressView courseProgress = myCourseProgress(studentToken, setup.course.getId());
        assertThat(courseProgress.lessonsCompleted()).isEqualTo(1);
        assertThat(courseProgress.lessonsTotal()).isEqualTo(2);
    }

    @Test
    void courseCompletionPercent_matchesLessonRollup() {
        User adminUser = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(adminUser);
        CourseWithLessons setup = courseWithPublishedLessons(adminToken, 4);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                setup.course.getId(),
                student.getId(),
                EnrollmentSource.ADMIN,
                adminUser.getId(),
                null);
        String studentToken = login(student);

        recordProgress(studentToken, setup.lessonIds.get(0), ProgressStatus.COMPLETED);
        recordProgress(studentToken, setup.lessonIds.get(1), ProgressStatus.COMPLETED);

        CourseProgressView courseProgress = myCourseProgress(studentToken, setup.course.getId());
        assertThat(courseProgress.lessonsTotal()).isEqualTo(4);
        assertThat(courseProgress.lessonsCompleted()).isEqualTo(2);
        assertThat(courseProgress.completionPercent()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(courseProgress.status()).isEqualTo(ProgressStatus.IN_PROGRESS);
        assertThat(courseProgress.modules()).hasSize(1);
        assertThat(courseProgress.modules().get(0).completionPercent())
                .isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void concurrentProgressUpdates_bothSucceed_rollupStaysConsistent() throws Exception {
        User adminUser = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(adminUser);
        CourseWithLessons setup = courseWithPublishedLessons(adminToken, 2);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                setup.course.getId(),
                student.getId(),
                EnrollmentSource.ADMIN,
                adminUser.getId(),
                null);
        String studentToken = login(student);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Integer> statuses = java.util.Collections.synchronizedList(new ArrayList<>());
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (UUID lessonId : setup.lessonIds) {
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
                                    ResponseEntity<LessonProgressView> response =
                                            recordProgress(
                                                    studentToken,
                                                    lessonId,
                                                    ProgressStatus.COMPLETED);
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

        assertThat(statuses)
                .as("both concurrent writers must succeed: %s", statuses)
                .containsExactly(200, 200);

        CourseProgressView courseProgress = myCourseProgress(studentToken, setup.course.getId());
        assertThat(courseProgress.lessonsCompleted())
                .as("no lost update: both completions must be reflected in the roll-up")
                .isEqualTo(2);
        assertThat(courseProgress.status()).isEqualTo(ProgressStatus.COMPLETED);
    }

    @Test
    void lastAccessedAt_advancesOnEachRecordedAccess() throws InterruptedException {
        User adminUser = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(adminUser);
        CourseWithLessons setup = courseWithPublishedLessons(adminToken, 1);
        User student = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                setup.course.getId(),
                student.getId(),
                EnrollmentSource.ADMIN,
                adminUser.getId(),
                null);
        String studentToken = login(student);

        ResponseEntity<LessonProgressView> first =
                recordProgress(studentToken, setup.lessonIds.get(0), ProgressStatus.IN_PROGRESS);
        var firstAccessedAt = first.getBody().lastAccessedAt();
        assertThat(firstAccessedAt).isNotNull();

        Thread.sleep(10);
        ResponseEntity<LessonProgressView> second =
                recordProgress(studentToken, setup.lessonIds.get(0), ProgressStatus.IN_PROGRESS);
        assertThat(second.getBody().lastAccessedAt()).isAfterOrEqualTo(firstAccessedAt);

        CourseProgressView courseProgress = myCourseProgress(studentToken, setup.course.getId());
        assertThat(courseProgress.lastAccessedAt()).isNotNull();
    }

    @Test
    void studentB_cannotSeeStudentAsProgress() {
        User adminUser = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(adminUser);
        CourseWithLessons setup = courseWithPublishedLessons(adminToken, 1);
        User studentA = userFixtures.activeUser(RoleCode.STUDENT);
        User studentB = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                setup.course.getId(),
                studentA.getId(),
                EnrollmentSource.ADMIN,
                adminUser.getId(),
                null);
        enrollmentService.enroll(
                setup.course.getId(),
                studentB.getId(),
                EnrollmentSource.ADMIN,
                adminUser.getId(),
                null);
        String tokenA = login(studentA);
        String tokenB = login(studentB);

        recordProgress(tokenA, setup.lessonIds.get(0), ProgressStatus.COMPLETED);

        CourseProgressView progressOfA = myCourseProgress(tokenA, setup.course.getId());
        CourseProgressView progressOfB = myCourseProgress(tokenB, setup.course.getId());

        assertThat(progressOfA.lessonsCompleted()).isEqualTo(1);
        assertThat(progressOfB.lessonsCompleted())
                .as("student B's own progress must never reflect student A's completions")
                .isEqualTo(0);
        assertThat(progressOfB.enrollmentId()).isNotEqualTo(progressOfA.enrollmentId());
    }

    @Test
    void teacherRoster_showsEachStudentsOwnProgress() {
        User adminUser = userFixtures.activeUser(RoleCode.ADMIN);
        String adminToken = login(adminUser);
        CourseWithLessons setup = courseWithPublishedLessons(adminToken, 2);
        User studentA = userFixtures.activeUser(RoleCode.STUDENT);
        User studentB = userFixtures.activeUser(RoleCode.STUDENT);
        enrollmentService.enroll(
                setup.course.getId(),
                studentA.getId(),
                EnrollmentSource.ADMIN,
                adminUser.getId(),
                null);
        enrollmentService.enroll(
                setup.course.getId(),
                studentB.getId(),
                EnrollmentSource.ADMIN,
                adminUser.getId(),
                null);
        recordProgress(login(studentA), setup.lessonIds.get(0), ProgressStatus.COMPLETED);

        ResponseEntity<PageResponse<StudentCourseProgressSummary>> response =
                rest.exchange(
                        url("/api/v1/teacher/courses/" + setup.course.getId() + "/progress"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<StudentCourseProgressSummary> roster = response.getBody().content();
        assertThat(roster).hasSize(2);
        var summaryA =
                roster.stream()
                        .filter(s -> s.studentUserId().equals(studentA.getId()))
                        .findFirst()
                        .orElseThrow();
        var summaryB =
                roster.stream()
                        .filter(s -> s.studentUserId().equals(studentB.getId()))
                        .findFirst()
                        .orElseThrow();
        assertThat(summaryA.lessonsCompleted()).isEqualTo(1);
        assertThat(summaryB.lessonsCompleted()).isEqualTo(0);
    }

    // ---------------------------------------------------------------- scaffolding

    private record CourseWithLessons(Course course, UUID moduleId, List<UUID> lessonIds) {}

    private CourseWithLessons courseWithPublishedLessons(String adminToken, int lessonCount) {
        Course course = enrollmentFixtures.publishedCourse(150);
        ModuleDetail module = createModule(adminToken, course.getId());
        upsertModuleTranslation(adminToken, module.id(), "uz", "Modul 1");
        changeModuleStatus(adminToken, module.id());
        List<UUID> lessonIds = new ArrayList<>();
        for (int i = 0; i < lessonCount; i++) {
            LessonDetail lesson = createLesson(adminToken, module.id());
            upsertLessonTranslation(adminToken, lesson.id(), "uz", "Dars " + (i + 1));
            changeLessonStatus(adminToken, lesson.id());
            lessonIds.add(lesson.id());
        }
        return new CourseWithLessons(course, module.id(), lessonIds);
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

    private void changeModuleStatus(String adminToken, UUID moduleId) {
        ResponseEntity<ModuleDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/modules/" + moduleId + "/status"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new ChangeModuleStatusRequest(PublicationStatus.PUBLISHED),
                                bearer(adminToken)),
                        ModuleDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private LessonDetail createLesson(String adminToken, UUID moduleId) {
        ResponseEntity<LessonDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/modules/" + moduleId + "/lessons"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateLessonRequest(null, SessionType.LECTURE),
                                bearer(adminToken)),
                        LessonDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void upsertLessonTranslation(
            String adminToken, UUID lessonId, String locale, String title) {
        ResponseEntity<LessonDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/lessons/" + lessonId + "/translations/" + locale),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new UpsertLessonTranslationRequest(title, null),
                                bearer(adminToken)),
                        LessonDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void changeLessonStatus(String adminToken, UUID lessonId) {
        ResponseEntity<LessonDetail> response =
                rest.exchange(
                        url("/api/v1/teacher/lessons/" + lessonId + "/status"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new uz.aifu.lms.curriculum.api.dto.LessonDtos
                                        .ChangeLessonStatusRequest(PublicationStatus.PUBLISHED),
                                bearer(adminToken)),
                        LessonDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ResponseEntity<LessonProgressView> recordProgress(
            String token, UUID lessonId, ProgressStatus status) {
        return rest.exchange(
                url("/api/v1/lessons/" + lessonId + "/progress"),
                HttpMethod.POST,
                new HttpEntity<>(new RecordLessonProgressRequest(status, null), bearer(token)),
                LessonProgressView.class);
    }

    private CourseProgressView myCourseProgress(String token, UUID courseId) {
        ResponseEntity<CourseProgressView> response =
                rest.exchange(
                        url("/api/v1/courses/" + courseId + "/progress/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(token)),
                        CourseProgressView.class);
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
