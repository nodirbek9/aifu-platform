package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
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
import uz.aifu.lms.announcement.api.dto.AnnouncementDtos.AnnouncementView;
import uz.aifu.lms.announcement.api.dto.AnnouncementDtos.CreateAnnouncementRequest;
import uz.aifu.lms.announcement.domain.AnnouncementAudience;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.ActivityDetail;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.ChangeActivityStatusRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.CreateActivityRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.UpdateActivityRequest;
import uz.aifu.lms.curriculum.api.dto.ActivityDtos.UpsertActivityTranslationRequest;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.ContentBlockView;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.CreateContentBlockRequest;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.ReorderContentBlocksRequest;
import uz.aifu.lms.curriculum.api.dto.ContentBlockDtos.UpsertContentBlockTranslationRequest;
import uz.aifu.lms.curriculum.api.dto.LessonDtos.LessonDetail;
import uz.aifu.lms.curriculum.domain.ActivityType;
import uz.aifu.lms.curriculum.domain.BlockVisibility;
import uz.aifu.lms.curriculum.domain.ContentBlockType;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.BulkEnrollResultView;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.GroupEnrollRequest;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.iam.api.ReferenceController.DepartmentView;
import uz.aifu.lms.iam.api.dto.AuthDtos.CurrentUserResponse;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.api.dto.AuthDtos.UpdateMyProfileRequest;
import uz.aifu.lms.iam.api.dto.UserDtos.ImportStudentResult;
import uz.aifu.lms.iam.api.dto.UserDtos.ImportStudentRow;
import uz.aifu.lms.iam.api.dto.UserDtos.ImportStudentsRequest;
import uz.aifu.lms.iam.api.dto.UserDtos.UserSummary;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.notification.api.dto.NotificationDtos.UnreadCountView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.CoursePlanView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.MyActivityView;
import uz.aifu.lms.progress.api.dto.ProgressDtos.PlanLesson;
import uz.aifu.lms.progress.api.dto.ProgressDtos.RecordLessonProgressRequest;
import uz.aifu.lms.progress.domain.ProgressStatus;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupView;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.CurriculumFixtures;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.SchedulingFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * The smaller additions the new frontend needs (product spec S-01, S-03.2, S-07, S-09, T-03.4,
 * T-11, A-05, A-06): what a student may see of a lesson, the course plan with statuses, the work
 * list, own profile settings, announcements, and the admin's import and group tools.
 */
class PlatformAdditionsIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @Autowired private CurriculumFixtures curriculumFixtures;
    @Autowired private SchedulingFixtures schedulingFixtures;
    @Autowired private EnrollmentService enrollmentService;
    @LocalServerPort private int port;
    private final ObjectMapper json = new ObjectMapper();

    private final class World {
        final User admin = userFixtures.activeUser(RoleCode.ADMIN);
        final String adminToken = login(admin);
        final Course course = enrollmentFixtures.publishedCourse(150);

        User enrolledStudent(String groupCode) {
            User user = schedulingFixtures.student(groupCode);
            enrollmentService.enroll(
                    course.getId(), user.getId(), EnrollmentSource.ADMIN, admin.getId(), null);
            return user;
        }
    }

    // ------------------------------------------------------------------ block positions

    @Test
    void aBlockAddedOrReorderedAfterADeletion_takesAFreePosition() throws Exception {
        World w = new World();
        Lesson lesson = curriculumFixtures.publishedLesson(w.course);
        String blocks = "/api/v1/teacher/lessons/" + lesson.getId() + "/blocks";
        String text = "{\"html\":\"<p>x</p>\"}";
        ContentBlockView first = addBlock(w.adminToken, blocks, ContentBlockType.TEXT, null, text);
        ContentBlockView second = addBlock(w.adminToken, blocks, ContentBlockType.TEXT, null, text);
        ContentBlockView third = addBlock(w.adminToken, blocks, ContentBlockType.TEXT, null, text);

        // the middle one goes: the next block must not reuse the last live position
        assertThat(
                        rest.exchange(
                                        url("/api/v1/teacher/blocks/" + second.id()),
                                        HttpMethod.DELETE,
                                        new HttpEntity<>(bearer(w.adminToken)),
                                        Void.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        ContentBlockView fourth = addBlock(w.adminToken, blocks, ContentBlockType.TEXT, null, text);
        assertThat(fourth.position()).isGreaterThan(third.position());

        // and the live blocks reorder freely over the deleted block's old position
        assertThat(
                        rest.exchange(
                                        url(blocks + "/reorder"),
                                        HttpMethod.PUT,
                                        new HttpEntity<>(
                                                new ReorderContentBlocksRequest(
                                                        java.util.List.of(
                                                                fourth.id(),
                                                                third.id(),
                                                                first.id())),
                                                bearer(w.adminToken)),
                                        Void.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    // ------------------------------------------------------------------ lesson visibility

    @Test
    void lesson_studentsNeverReceiveTeacherOnlyBlocks_andALessonPlanIsAlwaysOne() throws Exception {
        World w = new World();
        Lesson lesson = curriculumFixtures.publishedLesson(w.course);
        String blocks = "/api/v1/teacher/lessons/" + lesson.getId() + "/blocks";

        addBlock(
                w.adminToken,
                blocks,
                ContentBlockType.TEXT,
                null,
                "{\"html\":\"<p>Nazariya</p>\"}");
        addBlock(
                w.adminToken,
                blocks,
                ContentBlockType.TEXT,
                BlockVisibility.TEACHER_ONLY,
                "{\"html\":\"<p>Faqat o'qituvchi uchun eslatma</p>\"}");
        // asked to be visible to everyone; a lesson plan is teacher-only regardless
        ContentBlockView plan =
                addBlock(
                        w.adminToken,
                        blocks,
                        ContentBlockType.LESSON_PLAN,
                        BlockVisibility.ALL,
                        "{\"objectives\":\"<p>Sikllarni tushuntirish</p>\","
                                + "\"timeline\":[{\"minutes\":10,\"title\":\"Takrorlash\"},"
                                + "{\"minutes\":40,\"title\":\"Yangi mavzu\"}],"
                                + "\"homework\":\"<p>3 ta masala</p>\"}");
        assertThat(plan.visibility()).isEqualTo(BlockVisibility.TEACHER_ONLY);
        assertThat(plan.payload().get("timeline")).hasSize(2);

        // an empty plan, or a step without minutes, is refused
        UUID empty =
                post(
                                w.adminToken,
                                blocks,
                                new CreateContentBlockRequest(
                                        ContentBlockType.LESSON_PLAN, null, null, false, null),
                                ContentBlockView.class)
                        .getBody()
                        .id();
        for (String bad : List.of("{}", "{\"timeline\":[{\"title\":\"x\"}]}")) {
            assertThat(
                            rest.exchange(
                                            url(
                                                    "/api/v1/teacher/blocks/"
                                                            + empty
                                                            + "/translations/uz"),
                                            HttpMethod.PUT,
                                            new HttpEntity<>(
                                                    new UpsertContentBlockTranslationRequest(
                                                            json.readTree(bad)),
                                                    bearer(w.adminToken)),
                                            ApiError.class)
                                    .getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        User student = w.enrolledStudent(null);
        LessonDetail asStudent =
                get(login(student), "/api/v1/lessons/" + lesson.getId(), LessonDetail.class)
                        .getBody();
        assertThat(asStudent.blocks()).hasSize(1);
        assertThat(asStudent.blocks().get(0).payload().get("html").asText()).contains("Nazariya");

        // the same student-facing rendering for a teacher's preview and the projector
        assertThat(
                        get(w.adminToken, "/api/v1/lessons/" + lesson.getId(), LessonDetail.class)
                                .getBody()
                                .blocks())
                .hasSize(1);
        // the editor gets everything
        assertThat(
                        get(
                                        w.adminToken,
                                        "/api/v1/teacher/lessons/" + lesson.getId(),
                                        LessonDetail.class)
                                .getBody()
                                .blocks())
                .extracting(ContentBlockView::blockType)
                .contains(ContentBlockType.TEXT, ContentBlockType.LESSON_PLAN)
                .hasSize(4);
    }

    // ------------------------------------------------------------------ course plan and work list

    @Test
    void coursePlan_showsStatusLockAndWhereToContinue_andTheWorkListShowsDeadlines() {
        World w = new World();
        Lesson first = curriculumFixtures.publishedLesson(w.course);
        Lesson second = curriculumFixtures.publishedLesson(w.course);
        assertThat(
                        post(
                                        w.adminToken,
                                        "/api/v1/teacher/lessons/"
                                                + second.getId()
                                                + "/prerequisites/"
                                                + first.getId(),
                                        null,
                                        String.class)
                                .getStatusCode()
                                .is2xxSuccessful())
                .isTrue();
        UUID overdue =
                publishedActivity(
                        w, first.getId(), "Uy vazifasi", Instant.now().minus(1, ChronoUnit.DAYS));
        publishedActivity(w, second.getId(), "Kurs ishi", Instant.now().plus(7, ChronoUnit.DAYS));

        User student = w.enrolledStudent(null);
        String token = login(student);
        String planPath = "/api/v1/courses/" + w.course.getId() + "/plan/me";

        CoursePlanView before = get(token, planPath, CoursePlanView.class).getBody();
        assertThat(before.lessonsTotal()).isEqualTo(2);
        assertThat(before.lessonsCompleted()).isZero();
        assertThat(before.continueLessonId()).isEqualTo(first.getId());
        assertThat(before.lastLessonId()).isNull();
        PlanLesson firstRow = lesson(before, first.getId());
        PlanLesson secondRow = lesson(before, second.getId());
        assertThat(firstRow.title()).isEqualTo("Fixture lesson");
        assertThat(firstRow.status()).isEqualTo(ProgressStatus.NOT_STARTED);
        assertThat(firstRow.locked()).isFalse();
        assertThat(firstRow.activitiesTotal()).isEqualTo(1);
        assertThat(secondRow.locked()).isTrue();
        assertThat(secondRow.prerequisiteLessonIds()).containsExactly(first.getId());

        assertThat(
                        post(
                                        token,
                                        "/api/v1/lessons/" + first.getId() + "/progress",
                                        new RecordLessonProgressRequest(
                                                ProgressStatus.COMPLETED, 60L),
                                        String.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.OK);
        CoursePlanView after = get(token, planPath, CoursePlanView.class).getBody();
        assertThat(after.lessonsCompleted()).isEqualTo(1);
        assertThat(lesson(after, second.getId()).locked()).isFalse();
        assertThat(after.continueLessonId()).isEqualTo(second.getId());
        assertThat(after.lastLessonId()).isEqualTo(first.getId());

        // a teacher previews the same tree, without statuses
        CoursePlanView preview = get(w.adminToken, planPath, CoursePlanView.class).getBody();
        assertThat(preview.enrollmentId()).isNull();
        assertThat(lesson(preview, first.getId()).status()).isNull();
        // someone who is not in the course does not see it
        assertThat(
                        get(login(schedulingFixtures.student(null)), planPath, ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        // the work list: the missed deadline first, then the one still open
        List<MyActivityView> work =
                getList(token, "/api/v1/activities/me", new ParameterizedTypeReference<>() {});
        assertThat(work)
                .extracting(MyActivityView::title)
                .containsExactly("Uy vazifasi", "Kurs ishi");
        assertThat(work).extracting(MyActivityView::state).containsExactly("OVERDUE", "TODO");
        assertThat(work.get(0).activityId()).isEqualTo(overdue);
        assertThat(work.get(0).courseTitle()).isEqualTo("Fixture course");
        assertThat(work.get(0).lessonId()).isEqualTo(first.getId());
        List<MyActivityView> onlyOverdue =
                getList(
                        token,
                        "/api/v1/activities/me?state=overdue&courseId=" + w.course.getId(),
                        new ParameterizedTypeReference<>() {});
        assertThat(onlyOverdue).hasSize(1);
    }

    // ------------------------------------------------------------------ own profile, notifications

    @Test
    void me_showsTheStudentsGroup_andAUserChangesOnlyTheirOwnLanguage() {
        String groupCode = "PA-" + SchedulingFixtures.unique(8);
        User student = schedulingFixtures.student(groupCode);
        String token = login(student);

        CurrentUserResponse me = get(token, "/api/v1/auth/me", CurrentUserResponse.class).getBody();
        assertThat(me.groupCode()).isEqualTo(groupCode);
        assertThat(me.studentNumber()).isNotBlank();
        assertThat(me.preferredLocale()).isEqualTo("uz");

        CurrentUserResponse changed =
                rest.exchange(
                                url("/api/v1/auth/me"),
                                HttpMethod.PATCH,
                                new HttpEntity<>(
                                        new UpdateMyProfileRequest("ru", "+998901234567"),
                                        bearer(token)),
                                CurrentUserResponse.class)
                        .getBody();
        assertThat(changed.preferredLocale()).isEqualTo("ru");
        assertThat(changed.phone()).isEqualTo("+998901234567");
        assertThat(changed.fullName()).isEqualTo(student.getFullName());

        assertThat(
                        rest.exchange(
                                        url("/api/v1/auth/me"),
                                        HttpMethod.PATCH,
                                        new HttpEntity<>(
                                                new UpdateMyProfileRequest("de", null),
                                                bearer(token)),
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ------------------------------------------------------------------ announcements

    @Test
    void wideAnnouncements_reachEveryStudentOrTeacher_andOnlyTheStudyOfficeWritesThem() {
        World w = new World();
        User student = schedulingFixtures.student(null);
        User teacherUser = userFixtures.activeUser(RoleCode.TEACHER);
        String teacher = login(teacherUser);
        String title = "Imtihonlar haftasi " + SchedulingFixtures.unique(6);

        // a teacher may not write to the whole university; a wide one names no course
        assertThat(
                        post(
                                        teacher,
                                        "/api/v1/teacher/announcements",
                                        new CreateAnnouncementRequest(
                                                null,
                                                null,
                                                title,
                                                "<p>x</p>",
                                                AnnouncementAudience.EVERYONE),
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(
                        post(
                                        w.adminToken,
                                        "/api/v1/teacher/announcements",
                                        new CreateAnnouncementRequest(
                                                w.course.getId(),
                                                null,
                                                title,
                                                "<p>x</p>",
                                                AnnouncementAudience.STUDENTS),
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        AnnouncementView posted =
                post(
                                w.adminToken,
                                "/api/v1/teacher/announcements",
                                new CreateAnnouncementRequest(
                                        null,
                                        null,
                                        title,
                                        "<p>Imtihonlar 20-dekabrdan boshlanadi.</p>",
                                        AnnouncementAudience.STUDENTS),
                                AnnouncementView.class)
                        .getBody();
        assertThat(posted.audience()).isEqualTo(AnnouncementAudience.STUDENTS);
        assertThat(titles(login(student))).contains(title);
        assertThat(
                        get(
                                        login(student),
                                        "/api/v1/notifications/me/unread-count",
                                        UnreadCountView.class)
                                .getBody()
                                .count())
                .isGreaterThanOrEqualTo(1);
        assertThat(titles(teacher)).doesNotContain(title);

        // taken back, so that no other test's student sees it
        assertThat(
                        rest.exchange(
                                        url("/api/v1/teacher/announcements/" + posted.id()),
                                        HttpMethod.DELETE,
                                        new HttpEntity<>(bearer(w.adminToken)),
                                        Void.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(titles(login(student))).doesNotContain(title);
    }

    @Test
    void announcements_reachTheCourseOrTheGroup_andNotifyTheirStudents() {
        World w = new World();
        GroupView group =
                post(
                                w.adminToken,
                                "/api/v1/admin/groups",
                                new GroupRequest(
                                        "AN-" + SchedulingFixtures.unique(8),
                                        null,
                                        1,
                                        null,
                                        null,
                                        "uz",
                                        20,
                                        null,
                                        null),
                                GroupView.class)
                        .getBody();
        User inCourse = w.enrolledStudent(null);
        User inGroup = schedulingFixtures.student(group.code());
        User outsider = schedulingFixtures.student(null);

        // hostile markup is refused outright rather than half-kept
        assertThat(
                        post(
                                        w.adminToken,
                                        "/api/v1/teacher/announcements",
                                        new CreateAnnouncementRequest(
                                                w.course.getId(),
                                                null,
                                                "Ertangi dars",
                                                "<p>Dars</p><img src=x onerror=alert(1)>"),
                                        ApiError.class)
                                .getBody()
                                .code())
                .isEqualTo("UNSAFE_CONTENT_REJECTED");

        ResponseEntity<AnnouncementView> posted =
                post(
                        w.adminToken,
                        "/api/v1/teacher/announcements",
                        new CreateAnnouncementRequest(
                                w.course.getId(),
                                null,
                                "Ertangi dars",
                                "<p>Dars 204-xonada <b>bo'ladi</b>.</p>"),
                        AnnouncementView.class);
        assertThat(posted.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(posted.getBody().courseTitle()).isEqualTo("Fixture course");
        assertThat(posted.getBody().authorName()).isEqualTo(w.admin.getFullName());
        assertThat(
                        post(
                                        w.adminToken,
                                        "/api/v1/teacher/announcements",
                                        new CreateAnnouncementRequest(
                                                null,
                                                group.id(),
                                                "Guruh yig'ilishi",
                                                "<p>Soat 14:00</p>"),
                                        AnnouncementView.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(titles(login(inCourse))).containsExactly("Ertangi dars");
        assertThat(titles(login(inGroup))).containsExactly("Guruh yig'ilishi");
        assertThat(titles(login(outsider))).isEmpty();
        assertThat(
                        get(
                                        login(inCourse),
                                        "/api/v1/notifications/me/unread-count",
                                        UnreadCountView.class)
                                .getBody()
                                .count())
                .isGreaterThanOrEqualTo(1);
        assertThat(
                        post(
                                        login(inCourse),
                                        "/api/v1/notifications/me/read-all",
                                        null,
                                        UnreadCountView.class)
                                .getBody()
                                .count())
                .isZero();
        assertThat(
                        get(
                                        login(inCourse),
                                        "/api/v1/notifications/me/unread-count",
                                        UnreadCountView.class)
                                .getBody()
                                .count())
                .isZero();

        // neither audience, both audiences, a student, a teacher who is not the group's curator
        assertThat(
                        post(
                                        w.adminToken,
                                        "/api/v1/teacher/announcements",
                                        new CreateAnnouncementRequest(null, null, "x", "<p>x</p>"),
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(
                        post(
                                        login(inCourse),
                                        "/api/v1/teacher/announcements",
                                        new CreateAnnouncementRequest(
                                                w.course.getId(), null, "x", "<p>x</p>"),
                                        String.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        String teacher = login(userFixtures.activeUser(RoleCode.TEACHER));
        assertThat(
                        post(
                                        teacher,
                                        "/api/v1/teacher/announcements",
                                        new CreateAnnouncementRequest(
                                                null, group.id(), "x", "<p>x</p>"),
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        // its author removes it
        assertThat(
                        rest.exchange(
                                        url(
                                                "/api/v1/teacher/announcements/"
                                                        + posted.getBody().id()),
                                        HttpMethod.DELETE,
                                        new HttpEntity<>(bearer(w.adminToken)),
                                        Void.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(titles(login(inCourse))).isEmpty();
        // and the notification about it is withdrawn: it would lead nowhere
        assertThat(get(login(inCourse), "/api/v1/notifications/me", String.class).getBody())
                .doesNotContain(posted.getBody().id().toString());
    }

    // ------------------------------------------------------------------ admin tools

    @Test
    void admin_importsStudentsWithGroups_enrollsAGroup_andFindsTeachers() {
        World w = new World();
        String group = "IM-" + SchedulingFixtures.unique(8);
        String otherGroup = "IM-" + SchedulingFixtures.unique(8);
        String n1 = "U" + SchedulingFixtures.unique(9);
        String n2 = "U" + SchedulingFixtures.unique(9);
        List<ImportStudentRow> rows =
                List.of(
                        new ImportStudentRow(
                                "Aliyev Vali", n1, group, null, null, null, null, 2026),
                        new ImportStudentRow(
                                "Karimova Nodira", n2, group, null, null, null, null, 2026),
                        new ImportStudentRow("Takror", n1, group, null, null, null, null, null),
                        new ImportStudentRow(null, "U-x", group, null, null, null, null, null));

        // a dry run says what would happen and writes nothing
        List<ImportStudentResult> preview =
                importStudents(
                        w.adminToken,
                        new ImportStudentsRequest(
                                rows, "Test!Pass123", "student.aifu.uz", "uz", true));
        assertThat(preview)
                .extracting(ImportStudentResult::outcome)
                .containsExactly("CREATED", "CREATED", "FAILED", "FAILED");
        assertThat(preview.get(0).userId()).isNull();
        assertThat(preview.get(0).username()).isEqualTo(n1.toLowerCase());

        List<ImportStudentResult> done =
                importStudents(
                        w.adminToken,
                        new ImportStudentsRequest(
                                rows, "Test!Pass123", "student.aifu.uz", "uz", false));
        assertThat(done)
                .extracting(ImportStudentResult::outcome)
                .containsExactly("CREATED", "CREATED", "FAILED", "FAILED");
        assertThat(done.get(0).userId()).isNotNull();

        // the new student logs in with the given password and belongs to the group
        TokenResponse token =
                rest.postForEntity(
                                url("/api/v1/auth/login"),
                                new LoginRequest(n1.toLowerCase(), "Test!Pass123"),
                                TokenResponse.class)
                        .getBody();
        assertThat(token.accessToken()).isNotBlank();

        // importing the same list with another group moves the students instead of failing
        List<ImportStudentResult> again =
                importStudents(
                        w.adminToken,
                        new ImportStudentsRequest(
                                List.of(
                                        new ImportStudentRow(
                                                "Aliyev Vali",
                                                n1,
                                                otherGroup,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null),
                                        new ImportStudentRow(
                                                "Karimova Nodira",
                                                n2,
                                                group,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null)),
                                null,
                                null,
                                null,
                                false));
        assertThat(again)
                .extracting(ImportStudentResult::outcome)
                .containsExactly("MOVED", "UNCHANGED");

        // enroll what is left of the group into the course
        List<BulkEnrollResultView> enrolled =
                rest.exchange(
                                url("/api/v1/admin/enrollments/by-group"),
                                HttpMethod.POST,
                                new HttpEntity<>(
                                        new GroupEnrollRequest(w.course.getId(), group, null),
                                        bearer(w.adminToken)),
                                new ParameterizedTypeReference<List<BulkEnrollResultView>>() {})
                        .getBody();
        assertThat(enrolled).hasSize(1);
        assertThat(enrolled.get(0).success()).isTrue();
        assertThat(enrolled.get(0).studentUserId()).isEqualTo(done.get(1).userId());

        // pickers: teachers only, departments
        User teacher = userFixtures.activeUser(RoleCode.TEACHER);
        PageResponse<UserSummary> teachers =
                rest.exchange(
                                url(
                                        "/api/v1/admin/users?role=teacher&query="
                                                + teacher.getUsername()),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(w.adminToken)),
                                new ParameterizedTypeReference<PageResponse<UserSummary>>() {})
                        .getBody();
        assertThat(teachers.content()).extracting(UserSummary::id).containsExactly(teacher.getId());
        PageResponse<UserSummary> noStudents =
                rest.exchange(
                                url(
                                        "/api/v1/admin/users?role=STUDENT&query="
                                                + teacher.getUsername()),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(w.adminToken)),
                                new ParameterizedTypeReference<PageResponse<UserSummary>>() {})
                        .getBody();
        assertThat(noStudents.content()).isEmpty();
        List<DepartmentView> departments =
                getList(
                        w.adminToken,
                        "/api/v1/reference/departments",
                        new ParameterizedTypeReference<>() {});
        assertThat(departments).isNotEmpty();
    }

    // ------------------------------------------------------------------ helpers

    private ContentBlockView addBlock(
            String token,
            String blocksPath,
            ContentBlockType type,
            BlockVisibility visibility,
            String payload)
            throws Exception {
        ContentBlockView created =
                post(
                                token,
                                blocksPath,
                                new CreateContentBlockRequest(type, null, null, false, visibility),
                                ContentBlockView.class)
                        .getBody();
        ResponseEntity<ContentBlockView> translated =
                rest.exchange(
                        url("/api/v1/teacher/blocks/" + created.id() + "/translations/uz"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new UpsertContentBlockTranslationRequest(json.readTree(payload)),
                                bearer(token)),
                        ContentBlockView.class);
        assertThat(translated.getStatusCode()).isEqualTo(HttpStatus.OK);
        return translated.getBody();
    }

    private UUID publishedActivity(World w, UUID lessonId, String title, Instant dueAt) {
        UUID id =
                post(
                                w.adminToken,
                                "/api/v1/teacher/courses/" + w.course.getId() + "/activities",
                                new CreateActivityRequest(
                                        null,
                                        lessonId,
                                        ActivityType.MANUAL_TASK,
                                        false,
                                        null,
                                        null),
                                ActivityDetail.class)
                        .getBody()
                        .id();
        String path = "/api/v1/teacher/activities/" + id;
        for (Map.Entry<HttpMethod, Object[]> step :
                List.of(
                        Map.entry(
                                HttpMethod.PUT,
                                new Object[] {
                                    path + "/translations/uz",
                                    new UpsertActivityTranslationRequest(title, null)
                                }),
                        Map.entry(
                                HttpMethod.PATCH,
                                new Object[] {
                                    path,
                                    new UpdateActivityRequest(
                                            null, null, null, null, null, null, dueAt)
                                }),
                        Map.entry(
                                HttpMethod.PUT,
                                new Object[] {
                                    path + "/status",
                                    new ChangeActivityStatusRequest(PublicationStatus.PUBLISHED)
                                }))) {
            ResponseEntity<String> response =
                    rest.exchange(
                            url((String) step.getValue()[0]),
                            step.getKey(),
                            new HttpEntity<>(step.getValue()[1], bearer(w.adminToken)),
                            String.class);
            assertThat(response.getStatusCode().is2xxSuccessful())
                    .as("%s %s -> %s", step.getKey(), step.getValue()[0], response.getBody())
                    .isTrue();
        }
        return id;
    }

    private static PlanLesson lesson(CoursePlanView plan, UUID lessonId) {
        return plan.modules().stream()
                .flatMap(m -> m.lessons().stream())
                .filter(l -> l.id().equals(lessonId))
                .findFirst()
                .orElseThrow();
    }

    private List<String> titles(String token) {
        return rest
                .exchange(
                        url("/api/v1/announcements/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(token)),
                        new ParameterizedTypeReference<PageResponse<AnnouncementView>>() {})
                .getBody()
                .content()
                .stream()
                .map(AnnouncementView::title)
                .toList();
    }

    private List<ImportStudentResult> importStudents(String token, ImportStudentsRequest request) {
        ResponseEntity<List<ImportStudentResult>> response =
                rest.exchange(
                        url("/api/v1/admin/users/import"),
                        HttpMethod.POST,
                        new HttpEntity<>(request, bearer(token)),
                        new ParameterizedTypeReference<List<ImportStudentResult>>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private <T> ResponseEntity<T> get(String token, String path, Class<T> type) {
        return rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(bearer(token)), type);
    }

    private <T> List<T> getList(
            String token, String path, ParameterizedTypeReference<List<T>> type) {
        return rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(bearer(token)), type)
                .getBody();
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
