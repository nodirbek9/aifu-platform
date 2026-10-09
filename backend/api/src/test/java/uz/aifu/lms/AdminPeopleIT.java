package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.aifu.lms.academics.api.dto.CourseDtos.AssignTeacherRequest;
import uz.aifu.lms.academics.api.dto.CourseDtos.CourseDetail;
import uz.aifu.lms.academics.api.dto.CourseDtos.TeacherView;
import uz.aifu.lms.academics.app.CourseService;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.TeacherRole;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentSummary;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.EnrollmentSource;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.api.dto.UserDtos.UserSummary;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/** Phase 7.1-B1: administrative enrollment filters against real HTTP and PostgreSQL. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminPeopleIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures users;
    @Autowired private EnrollmentFixtures fixtures;
    @Autowired private EnrollmentService enrollments;
    @Autowired private CourseService courses;
    @Autowired private JdbcTemplate jdbc;
    @LocalServerPort private int port;

    private User studentA;
    private User studentB;
    private User teacher;
    private Course sharedCourse;
    private Course otherCourse;
    private UUID sharedEnrollmentA;
    private UUID otherEnrollmentA;
    private UUID sharedEnrollmentB;
    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @BeforeAll
    void createPeopleAndEnrollments() {
        User admin = users.activeUser(RoleCode.ADMIN);
        teacher = users.activeUser(RoleCode.TEACHER);
        studentA = users.activeUser(RoleCode.STUDENT);
        studentB = users.activeUser(RoleCode.STUDENT);
        sharedCourse = fixtures.publishedCourse(150);
        otherCourse = fixtures.publishedCourse(150);
        courses.assignTeacher(
                sharedCourse.getId(),
                new AssignTeacherRequest(teacher.getId(), TeacherRole.LEAD),
                admin.getId(),
                null);
        sharedEnrollmentA = enroll(sharedCourse, studentA, admin);
        otherEnrollmentA = enroll(otherCourse, studentA, admin);
        sharedEnrollmentB = enroll(sharedCourse, studentB, admin);
        enrollments.suspend(otherEnrollmentA, admin.getId(), null, "Fixture suspension");

        // Deliberate timestamps prove descending date order and the UUID tie-breaker.
        setEnrolledAt(sharedEnrollmentA, "2026-09-01T00:00:00Z");
        setEnrolledAt(sharedEnrollmentB, "2026-09-01T00:00:00Z");
        setEnrolledAt(otherEnrollmentA, "2026-09-02T00:00:00Z");
        adminToken = login(admin);
        teacherToken = login(teacher);
        studentToken = login(studentA);
    }

    @Test
    void users_teacherRoleFilter_includesTeachersAndExcludesStudentOnlyAccounts() {
        List<UUID> teacherIds = new ArrayList<>();
        int pageNumber = 0;
        PageResponse<UserSummary> page;
        do {
            var response =
                    rest.exchange(
                            url("/api/v1/admin/users?role=TEACHER&size=100&page=" + pageNumber),
                            HttpMethod.GET,
                            new HttpEntity<>(bearer(adminToken)),
                            new ParameterizedTypeReference<PageResponse<UserSummary>>() {});
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            page = response.getBody();
            assertThat(page).isNotNull();
            assertThat(page.content())
                    .allSatisfy(user -> assertThat(user.roles()).contains(RoleCode.TEACHER));
            page.content().stream().map(UserSummary::id).forEach(teacherIds::add);
            pageNumber++;
        } while (page.hasNext());

        assertThat(teacherIds)
                .contains(teacher.getId())
                .doesNotContain(studentA.getId(), studentB.getId());
    }

    @Test
    void courseDetails_unscheduledAssignment_containsTeacherIdentityAndRole() {
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from timetable_entries where course_id = ?",
                                Long.class,
                                sharedCourse.getId()))
                .isZero();
        var response =
                rest.exchange(
                        url("/api/v1/admin/courses/" + sharedCourse.getId()),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        CourseDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().teachers())
                .containsExactly(
                        new TeacherView(teacher.getId(), teacher.getFullName(), TeacherRole.LEAD));
    }

    @Test
    void users_teacherRoleFilter_assignedTeacherIsForbidden() {
        var response =
                rest.exchange(
                        url("/api/v1/admin/users?role=TEACHER"),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(teacherToken)),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("AUTH_FORBIDDEN");
        assertThat(response.getBody().requestId()).isNotBlank();
    }

    @Test
    void list_userOnly_returnsStudentAcrossCoursesWithoutOtherStudents() {
        var page = list("userId=" + studentA.getId());

        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content())
                .extracting(EnrollmentSummary::studentUserId)
                .containsOnly(studentA.getId());
        assertThat(page.content())
                .extracting(EnrollmentSummary::courseId)
                .containsExactly(otherCourse.getId(), sharedCourse.getId());
        assertThat(page.content())
                .extracting(EnrollmentSummary::id)
                .containsExactly(otherEnrollmentA, sharedEnrollmentA);
    }

    @Test
    void list_courseAndUser_returnsOnlyTheirIntersection() {
        var page = list("courseId=" + sharedCourse.getId() + "&userId=" + studentA.getId());

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content())
                .extracting(EnrollmentSummary::id)
                .containsExactly(sharedEnrollmentA);
        assertThat(page.content().getFirst().studentUserId()).isEqualTo(studentA.getId());
        assertThat(page.content().getFirst().courseId()).isEqualTo(sharedCourse.getId());
    }

    @Test
    void list_courseOnly_preservesExistingRosterAndSortsTiedDatesById() {
        var page = list("courseId=" + sharedCourse.getId());
        List<UUID> expected =
                List.of(sharedEnrollmentA, sharedEnrollmentB).stream()
                        .sorted(Comparator.comparing(UUID::toString))
                        .toList();

        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content())
                .extracting(EnrollmentSummary::id)
                .containsExactlyElementsOf(expected);
        assertThat(page.content())
                .extracting(EnrollmentSummary::studentUserId)
                .containsExactlyInAnyOrder(studentA.getId(), studentB.getId());
    }

    @Test
    void list_userAndStatus_filtersBeforePagination() {
        var page = list("userId=" + studentA.getId() + "&status=ACTIVE&size=1");

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page.content())
                .extracting(EnrollmentSummary::id)
                .containsExactly(sharedEnrollmentA);
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void list_courseUserAndStatus_appliesAllThreeFilters() {
        String filters = "courseId=" + otherCourse.getId() + "&userId=" + studentA.getId();

        assertThat(list(filters + "&status=ACTIVE").content()).isEmpty();
        assertThat(list(filters + "&status=SUSPENDED").content())
                .extracting(EnrollmentSummary::id)
                .containsExactly(otherEnrollmentA);
    }

    @Test
    void list_courseAndStatus_preservesExistingStatusFilter() {
        var page = list("courseId=" + otherCourse.getId() + "&status=SUSPENDED");

        assertThat(page.content())
                .extracting(EnrollmentSummary::id)
                .containsExactly(otherEnrollmentA);
        assertThat(list("courseId=" + otherCourse.getId() + "&status=ACTIVE").content()).isEmpty();
    }

    @Test
    void list_unknownUser_returnsEmptyPage() {
        UUID unknownUser = UUID.randomUUID();
        for (String filters :
                List.of(
                        "userId=" + unknownUser,
                        "courseId=" + sharedCourse.getId() + "&userId=" + unknownUser)) {
            var page = list(filters);
            assertThat(page.content()).isEmpty();
            assertThat(page.totalElements()).isZero();
            assertThat(page.totalPages()).isZero();
            assertThat(page.hasNext()).isFalse();
        }
    }

    @Test
    void list_multiplePages_keepsStableOrderAndPageMetadata() {
        String filters = "userId=" + studentA.getId() + "&size=1";
        var first = list(filters + "&page=0");
        var second = list(filters + "&page=1");
        var beyond = list(filters + "&page=2");

        assertThat(first.content())
                .extracting(EnrollmentSummary::id)
                .containsExactly(otherEnrollmentA);
        assertThat(first.page()).isZero();
        assertThat(first.size()).isEqualTo(1);
        assertThat(first.totalElements()).isEqualTo(2);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(first.hasNext()).isTrue();
        assertThat(first.hasPrevious()).isFalse();
        assertThat(second.content())
                .extracting(EnrollmentSummary::id)
                .containsExactly(sharedEnrollmentA);
        assertThat(second.page()).isEqualTo(1);
        assertThat(second.totalElements()).isEqualTo(2);
        assertThat(second.hasNext()).isFalse();
        assertThat(second.hasPrevious()).isTrue();
        assertThat(beyond.content()).isEmpty();
        assertThat(beyond.totalElements()).isEqualTo(2);
    }

    @Test
    void list_oversizedPage_capsSizeAtOneHundred() {
        var page = list("userId=" + studentA.getId() + "&size=1000");

        assertThat(page.size()).isEqualTo(100);
        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(2);
    }

    @Test
    void list_explicitSort_overridesDefaultOrder() {
        var page = list("userId=" + studentA.getId() + "&sort=enrolledAt,asc");

        assertThat(page.content())
                .extracting(EnrollmentSummary::id)
                .containsExactly(sharedEnrollmentA, otherEnrollmentA);
        var roster = list("courseId=" + sharedCourse.getId() + "&sort=student.fullName,asc");
        assertThat(roster.content()).extracting(EnrollmentSummary::studentFullName).isSorted();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "status=ACTIVE", "userId=not-a-uuid", "courseId=not-a-uuid"})
    void list_missingOrInvalidFilters_returnsBadRequest(String filters) {
        var response = get(filters, adminToken, ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code())
                .isEqualTo(
                        filters.contains("not-a-uuid") ? "MALFORMED_REQUEST" : "VALIDATION_FAILED");
        assertThat(response.getBody().requestId()).isNotBlank();
    }

    @Test
    void list_unknownCourse_retainsCourseEditorGuard() {
        var response =
                get(
                        "courseId=" + UUID.randomUUID() + "&userId=" + studentA.getId(),
                        adminToken,
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void list_assignedTeacherAndStudent_areForbiddenForEveryFilterShape() {
        for (String token : List.of(teacherToken, studentToken)) {
            for (String filters :
                    List.of(
                            "userId=" + studentA.getId(),
                            "courseId=" + sharedCourse.getId(),
                            "courseId=" + sharedCourse.getId() + "&userId=" + studentA.getId())) {
                var response = get(filters, token, ApiError.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                assertThat(response.getBody().code()).isEqualTo("AUTH_FORBIDDEN");
            }
        }
    }

    @Test
    void list_anonymousCaller_isUnauthorized() {
        var response = get("userId=" + studentA.getId(), null, ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void list_responseContainsOnlySummaryFieldsAndRequestedStudent() {
        var response = get("userId=" + studentA.getId(), adminToken, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.path("content").size()).isEqualTo(2);
        for (JsonNode row : body.path("content")) {
            List<String> fields = new ArrayList<>();
            row.fieldNames().forEachRemaining(fields::add);
            // Null cohortCode may be omitted by Jackson's existing NON_NULL policy.
            assertThat(fields)
                    .contains(
                            "id",
                            "courseId",
                            "subjectCode",
                            "termName",
                            "studentUserId",
                            "studentFullName",
                            "status",
                            "attemptNo",
                            "enrolledAt");
            assertThat(fields)
                    .containsOnly(
                            "id",
                            "courseId",
                            "subjectCode",
                            "cohortCode",
                            "termName",
                            "studentUserId",
                            "studentFullName",
                            "status",
                            "attemptNo",
                            "enrolledAt");
            assertThat(row.path("studentUserId").asText()).isEqualTo(studentA.getId().toString());
            assertThat(row.path("studentFullName").asText()).isEqualTo(studentA.getFullName());
            assertThat(row.path("attemptNo").asInt()).isEqualTo(1);
        }
        assertThat(body.toString())
                .doesNotContain(
                        studentB.getId().toString(),
                        studentB.getFullName(),
                        studentA.getEmail(),
                        studentA.getUsername(),
                        "password",
                        "accessToken",
                        "refreshToken");
    }

    private UUID enroll(Course course, User student, User admin) {
        return enrollments
                .enroll(
                        course.getId(),
                        student.getId(),
                        EnrollmentSource.ADMIN,
                        admin.getId(),
                        null)
                .getId();
    }

    private void setEnrolledAt(UUID enrollmentId, String timestamp) {
        jdbc.update(
                "update enrollments set enrolled_at = ? where id = ?",
                Timestamp.from(Instant.parse(timestamp)),
                enrollmentId);
    }

    private PageResponse<EnrollmentSummary> list(String filters) {
        ResponseEntity<PageResponse<EnrollmentSummary>> response =
                rest.exchange(
                        url("/api/v1/admin/enrollments?" + filters),
                        HttpMethod.GET,
                        new HttpEntity<>(bearer(adminToken)),
                        new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private <T> ResponseEntity<T> get(String filters, String token, Class<T> responseType) {
        return rest.exchange(
                url("/api/v1/admin/enrollments?" + filters),
                HttpMethod.GET,
                new HttpEntity<>(bearer(token)),
                responseType);
    }

    private String login(User user) {
        var response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().accessToken();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
