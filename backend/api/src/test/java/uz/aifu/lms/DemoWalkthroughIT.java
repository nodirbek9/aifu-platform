package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * The acceptance demo of the product spec (docs/01-PRODUCT-SPEC.md §9) against the SEEDED
 * development database, through the real HTTP API: what the admin, a teacher and a student see when
 * they log in to a fresh demo installation. It is the one test that runs with {@code db/seed}
 * loaded, in a database of its own — so it also proves the seed and the API agree with each other
 * (a frontend built against this database must find every screen populated).
 *
 * <p>All dates are fixed dates inside the seeded term; nothing here depends on today's date.
 */
@Tag("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class DemoWalkthroughIT {

    private static final String PASSWORD = "Dev!Pass123";

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("aifu_lms")
                    .withUsername("aifu_app")
                    .withPassword("it_only_password_not_used_anywhere_else");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void seededDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration,classpath:db/seed");
    }

    private final TestRestTemplate rest =
            new TestRestTemplate(
                    new RestTemplateBuilder()
                            .requestFactory(() -> new HttpComponentsClientHttpRequestFactory()));
    private final ObjectMapper json = new ObjectMapper();
    @LocalServerPort private int port;

    // ------------------------------------------------------------------ 1. the administrator

    @Test
    void admin_seesThePublishedTimetable_itsLoad_andIsWarnedAboutAClash() {
        String admin = login("admin");
        JsonNode timetables = get(admin, "/api/v1/admin/timetables");
        assertThat(timetables).hasSize(1);
        JsonNode timetable = timetables.get(0);
        assertThat(timetable.get("status").asText()).isEqualTo("PUBLISHED");
        assertThat(timetable.get("entryCount").asInt()).isEqualTo(10);
        String id = timetable.get("id").asText();

        JsonNode entries = get(admin, "/api/v1/admin/timetables/" + id + "/entries");
        JsonNode practical =
                only(
                        entries,
                        e ->
                                e.get("subjectCode").asText().equals("DAS1110")
                                        && e.get("sessionType").asText().equals("PRACTICAL"));
        assertThat(practical.get("teacherName").asText()).startsWith("Ayitbayev");
        assertThat(practical.get("roomLabel").asText()).contains("301");
        assertThat(practical.get("groups").get(0).get("code").asText()).isEqualTo("BK-DI26/1");

        // putting the same teacher into the same pair with another group is refused
        JsonNode groups = get(admin, "/api/v1/groups");
        JsonNode otherGroup = only(groups, g -> g.get("code").asText().equals("B-IQTR26/1"));
        assertThat(otherGroup.get("studentCount").asInt()).isEqualTo(10);
        JsonNode rooms = get(admin, "/api/v1/timetable/rooms");
        JsonNode otherRoom = only(rooms, r -> r.get("number").asText().equals("206"));
        ObjectNode clash = json.createObjectNode();
        clash.put("courseId", practical.get("courseId").asText());
        clash.put("sessionType", "PRACTICAL");
        clash.put("teacherUserId", practical.get("teacherUserId").asText());
        clash.put("roomId", otherRoom.get("id").asText());
        clash.put("dayOfWeek", practical.get("dayOfWeek").asInt());
        clash.put("pairSlotId", practical.get("pairSlotId").asText());
        clash.put("weekParity", "EVERY");
        clash.putArray("groupIds").add(otherGroup.get("id").asText());
        JsonNode check =
                exchange(
                                admin,
                                HttpMethod.POST,
                                "/api/v1/admin/timetables/" + id + "/entries/check",
                                clash)
                        .getBody();
        assertThat(texts(check.get("conflicts"), "type")).containsExactly("TEACHER");

        // planned against scheduled hours: the split network practical is short of its 30 hours
        JsonNode load = get(admin, "/api/v1/admin/timetables/" + id + "/load");
        assertThat(load).hasSize(8);
        JsonNode networks =
                only(
                        load,
                        r ->
                                r.get("subjectCode").asText().equals("KOT1306")
                                        && r.get("sessionType").asText().equals("PRACTICAL"));
        assertThat(networks.get("plannedHours").asInt()).isEqualTo(30);
        assertThat(networks.get("remainingHours").asInt()).isPositive();
    }

    // ------------------------------------------------------------------ 2. the teacher

    @Test
    void teacher_seesTheirWeek_opensAClass_andHasItsJournalAndPlan() {
        String teacher = login("n.ayitbayev");

        // week 3 of the term is an odd week: the network practical is subgroup A's
        JsonNode week = get(teacher, "/api/v1/timetable/me?date=2026-09-14&as=teacher");
        assertThat(week.get("parity").asText()).isEqualTo("ODD");
        List<JsonNode> classes = classes(week);
        assertThat(classes).hasSize(5);
        assertThat(classes).allMatch(c -> c.get("teacherName").asText().startsWith("Ayitbayev"));
        // a time of day is a plain string, as the API contract says
        assertThat(classes.get(0).get("startsAt").asText())
                .matches("[0-9]{2}:[0-9]{2}(:[0-9]{2})?");
        assertThat(
                        classes.stream()
                                .filter(c -> c.get("subjectCode").asText().equals("KOT1306"))
                                .map(c -> c.get("subgroup").asText()))
                .containsExactly("A");

        // Friday's Dasturlash practical, already held: its topic and attendance are there
        JsonNode friday =
                classes.stream()
                        .filter(c -> c.get("subjectCode").asText().equals("DAS1110"))
                        .findFirst()
                        .orElseThrow();
        assertThat(friday.get("date").asText()).isEqualTo("2026-09-18");
        String classPath =
                "/api/v1/teacher/class-sessions/" + friday.get("entryId").asText() + "/2026-09-18";
        JsonNode session = get(teacher, classPath);
        assertThat(session.hasNonNull("sessionId")).isTrue();
        assertThat(session.get("lesson").get("title").asText()).isNotBlank();
        assertThat(session.get("students").size()).isGreaterThanOrEqualTo(12);
        JsonNode totals = session.get("totals");
        assertThat(
                        totals.get("present").asInt()
                                + totals.get("late").asInt()
                                + totals.get("absent").asInt()
                                + totals.get("excused").asInt())
                .isEqualTo(12);

        String courseId = friday.get("courseId").asText();
        JsonNode journal = get(teacher, "/api/v1/teacher/courses/" + courseId + "/attendance");
        assertThat(journal.get("sessions")).hasSize(4);
        assertThat(journal.get("students").size()).isGreaterThanOrEqualTo(12);

        // the thematic plan knows the cancelled class
        JsonNode plan = get(teacher, "/api/v1/teacher/courses/" + courseId + "/class-plan");
        assertThat(plan.size()).isGreaterThan(20);
        JsonNode cancelled =
                only(plan, p -> p.get("classInfo").get("date").asText().equals("2026-10-16"));
        assertThat(cancelled.get("classInfo").get("status").asText()).isEqualTo("CANCELLED");

        // the first practical's lesson carries a plan that only its teachers receive
        JsonNode first =
                get(
                        teacher,
                        "/api/v1/teacher/class-sessions/"
                                + friday.get("entryId").asText()
                                + "/2026-09-04");
        String lessonId = first.get("lesson").get("id").asText();
        JsonNode forEditors = get(teacher, "/api/v1/teacher/lessons/" + lessonId);
        JsonNode forStudents = get(teacher, "/api/v1/lessons/" + lessonId);
        assertThat(forStudents.get("blocks").size()).isPositive();
        assertThat(texts(forEditors.get("blocks"), "blockType")).contains("LESSON_PLAN");
        assertThat(texts(forStudents.get("blocks"), "blockType")).doesNotContain("LESSON_PLAN");
    }

    // ------------------------------------------------------------------ 3. the student

    @Test
    void student_seesTimetableLessonsWorkAndAttendance_andRunsCodeInThreeLanguages() {
        String student = login("student3");
        JsonNode me = get(student, "/api/v1/auth/me");
        assertThat(me.get("groupCode").asText()).isEqualTo("BK-DI26/1");

        // the week of the moved lecture: gone from Monday, held on Tuesday
        List<JsonNode> moved = classes(get(student, "/api/v1/timetable/me?date=2026-10-19"));
        assertThat(moved.stream().map(c -> c.get("status").asText()))
                .containsExactly("MOVED_OUT", "MOVED_IN", "SCHEDULED");
        assertThat(moved.get(1).get("date").asText()).isEqualTo("2026-10-20");
        assertThat(moved.get(1).get("roomLabel").asText()).contains("204");
        // the week before: the practical is cancelled
        List<JsonNode> before = classes(get(student, "/api/v1/timetable/me?date=2026-10-12"));
        assertThat(before.stream().map(c -> c.get("status").asText()))
                .containsExactly("SCHEDULED", "CANCELLED");

        // this semester's Dasturlash course, with a plan full of lessons
        JsonNode enrollments = get(student, "/api/v1/enrollments/me").get("content");
        String courseId =
                only(enrollments, e -> e.get("cohortCode").asText().equals("DI-26-01-S1"))
                        .get("courseId")
                        .asText();
        JsonNode plan = get(student, "/api/v1/courses/" + courseId + "/plan/me");
        assertThat(plan.get("lessonsTotal").asInt()).isGreaterThan(20);
        assertThat(plan.hasNonNull("continueLessonId")).isTrue();
        List<JsonNode> lessons = new ArrayList<>();
        plan.get("modules").forEach(m -> m.get("lessons").forEach(lessons::add));
        assertThat(lessons).anyMatch(l -> l.get("activitiesTotal").asInt() > 0);
        assertThat(lessons).allMatch(l -> !l.get("title").asText().isBlank());

        // a lesson is a chapter, not a stub
        JsonNode lesson = get(student, "/api/v1/lessons/" + plan.get("continueLessonId").asText());
        assertThat(lesson.get("blocks").size()).isGreaterThanOrEqualTo(3);

        // work across courses, the missed deadline first
        JsonNode work = get(student, "/api/v1/activities/me");
        assertThat(work.size()).isGreaterThan(10);
        assertThat(work.get(0).get("state").asText()).isEqualTo("OVERDUE");
        assertThat(texts(work, "type")).contains("CODING_TASK", "QUIZ");

        // the first task offers the three teaching languages, each with starter code
        JsonNode firstTask = work.get(0);
        assertThat(firstTask.get("type").asText()).isEqualTo("CODING_TASK");
        String taskId = firstTask.get("referenceId").asText();
        JsonNode task = get(student, "/api/v1/courses/" + courseId + "/coding-tasks/" + taskId);
        assertThat(texts(task.get("languages"), "code"))
                .containsExactlyInAnyOrder("python", "java", "cpp");
        task.get("languages").forEach(l -> assertThat(l.get("starterCode").asText()).isNotBlank());
        assertThat(task.get("exampleTestCases").size()).isPositive();

        // "Run" in C++ is accepted (the worker is not part of this test)
        ObjectNode run = json.createObjectNode();
        run.put("codingTaskId", taskId);
        run.put("languageCode", "cpp");
        run.put("sourceCode", "#include <iostream>\nint main() { std::cout << 1; }\n");
        run.put("stdin", "Ali");
        ResponseEntity<JsonNode> queued = exchange(student, HttpMethod.POST, "/api/v1/runs", run);
        assertThat(queued.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(queued.getBody().get("status").asText()).isEqualTo("QUEUED");

        // attendance of the four practicals held so far; both announcements
        JsonNode attendance = get(student, "/api/v1/attendance/me");
        assertThat(attendance).hasSize(1);
        assertThat(attendance.get(0).get("records")).hasSize(4);
        assertThat(texts(get(student, "/api/v1/announcements/me").get("content"), "title"))
                .containsExactly("Guruh yig'ilishi", "Amaliy mashg'ulotlar tartibi");
    }

    @Test
    void secondYearStudent_seesOnlyTheirHalfOfASplitClass_andHintsOpenInOrder() {
        String student = login("xolmatov.u");
        // an odd week: five classes, the network practical is subgroup A's
        List<JsonNode> odd = classes(get(student, "/api/v1/timetable/me?date=2026-09-14"));
        assertThat(odd).hasSize(5);
        // the directory lists everybody who has classes
        JsonNode directory = get(student, "/api/v1/timetable/directory?date=2026-09-14");
        assertThat(texts(directory.get("groups"), "code"))
                .containsExactly("B-IQTR26/1", "BK-DI25/1", "BK-DI26/1");
        assertThat(directory.get("teachers")).hasSize(3);

        // hints of a first-year task are not this student's to open
        String firstYear = login("student3");
        JsonNode work = get(firstYear, "/api/v1/activities/me");
        JsonNode prime = only(work, w -> w.get("title").asText().startsWith("Tub son"));
        String hints = "/api/v1/coding-tasks/" + prime.get("referenceId").asText() + "/hints";
        assertThat(exchange(student, HttpMethod.GET, hints, null).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode closed = get(firstYear, hints);
        assertThat(closed).hasSize(3);
        assertThat(closed.get(0).get("revealed").asBoolean()).isFalse();
        JsonNode opened =
                exchange(
                                firstYear,
                                HttpMethod.POST,
                                hints + "/" + closed.get(0).get("id").asText() + "/reveal",
                                null)
                        .getBody();
        assertThat(opened.get(0).get("body").asText()).contains("1 tub son emas");
        assertThat(opened.get(1).hasNonNull("body")).isFalse();
    }

    // ------------------------------------------------------------------ 4. the rest of the
    // student's side

    @Test
    void student_findsLessonsBySearch_readsGradesByComponent_andIsToldWhatHappened() {
        String student = login("student3");
        String courseId =
                only(
                                get(student, "/api/v1/enrollments/me").get("content"),
                                e -> e.get("cohortCode").asText().equals("DI-26-01-S1"))
                        .get("courseId")
                        .asText();
        String enrollmentId =
                only(
                                get(student, "/api/v1/enrollments/me").get("content"),
                                e -> e.get("cohortCode").asText().equals("DI-26-01-S1"))
                        .get("id")
                        .asText();

        // search: the beginning of a word finds the student's lessons, each with its course
        JsonNode found = get(student, "/api/v1/search?q=massiv&locale=uz");
        assertThat(found).isNotEmpty();
        assertThat(texts(found, "resultType")).contains("LESSON").doesNotContain("SUBJECT");
        found.forEach(hit -> assertThat(hit.hasNonNull("courseId")).isTrue());
        assertThat(texts(get(student, "/api/v1/search?q=dastur&locale=ru"), "resultType"))
                .as("Uzbek titles are found by someone reading in Russian")
                .contains("COURSE", "LESSON");
        // the ICT student is in another course: none of this is theirs to find
        assertThat(get(login("student1"), "/api/v1/search?q=massiv&locale=uz")).isEmpty();

        // grades: the university's scheme with what is earned so far
        JsonNode grades = get(student, "/api/v1/enrollments/" + enrollmentId + "/grades?locale=uz");
        assertThat(grades.get("courseId").asText()).isEqualTo(courseId);
        assertThat(texts(grades.get("components"), "code"))
                .containsExactly(
                        "JN", "JN_ATTENDANCE", "JN_PRACTICAL", "JN_INDEPENDENT", "ON", "YaN");
        JsonNode jn = only(grades.get("components"), c -> c.get("code").asText().equals("JN"));
        assertThat(jn.get("earned").decimalValue()).isEqualByComparingTo("24");
        assertThat(jn.get("gatePassed").asBoolean()).isTrue();
        JsonNode yan = only(grades.get("components"), c -> c.get("code").asText().equals("YaN"));
        assertThat(yan.get("graded").asBoolean()).isFalse();
        assertThat(yan.hasNonNull("gatePassed")).isFalse();
        assertThat(grades.get("complete").asBoolean()).isFalse();
        assertThat(grades.get("finalScore").decimalValue()).isEqualByComparingTo("48");
        assertThat(grades.get("admittedToFinal").asBoolean()).isTrue();
        assertThat(grades.get("entries")).hasSize(4);
        // the corrected practical mark is still in the history
        assertThat(texts(grades.get("history"), "status"))
                .containsExactlyInAnyOrder(
                        "PUBLISHED", "PUBLISHED", "PUBLISHED", "PUBLISHED", "REVISED");

        // notifications: each says what it is about
        JsonNode notifications = get(student, "/api/v1/notifications/me").get("content");
        assertThat(texts(notifications, "type"))
                .contains("GRADE_PUBLISHED", "ANNOUNCEMENT", "TIMETABLE_CHANGED");
        JsonNode moved =
                only(
                        notifications,
                        n ->
                                n.get("type").asText().equals("TIMETABLE_CHANGED")
                                        && n.get("context").get("kind").asText().equals("MOVED"));
        assertThat(moved.get("context").get("date").asText()).isEqualTo("2026-10-19");
        assertThat(moved.get("context").get("newDate").asText()).isEqualTo("2026-10-20");
        assertThat(moved.get("context").get("courseId").asText()).isEqualTo(courseId);
        assertThat(get(student, "/api/v1/notifications/me/unread-count").get("count").asInt())
                .isGreaterThan(0);

        // the next class: one that is held, not before today
        ResponseEntity<JsonNode> next =
                exchange(student, HttpMethod.GET, "/api/v1/timetable/next", null);
        assertThat(next.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);
        if (next.getStatusCode() == HttpStatus.OK) {
            assertThat(next.getBody().get("status").asText())
                    .isIn("SCHEDULED", "MOVED_IN", "REPLACED");
            assertThat(next.getBody().get("date").asText())
                    .isGreaterThanOrEqualTo(java.time.LocalDate.now().minusDays(1).toString());
        }
    }

    // ------------------------------------------------------------------ helpers

    private String login(String username) {
        ResponseEntity<JsonNode> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        Map.of("identifier", username, "password", PASSWORD),
                        JsonNode.class);
        assertThat(response.getStatusCode()).as("login of %s", username).isEqualTo(HttpStatus.OK);
        return response.getBody().get("accessToken").asText();
    }

    private JsonNode get(String token, String path) {
        ResponseEntity<JsonNode> response = exchange(token, HttpMethod.GET, path, null);
        assertThat(response.getStatusCode())
                .as("GET %s -> %s", path, response.getBody())
                .isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private ResponseEntity<JsonNode> exchange(
            String token, HttpMethod method, String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return rest.exchange(url(path), method, new HttpEntity<>(body, headers), JsonNode.class);
    }

    private static List<JsonNode> classes(JsonNode week) {
        List<JsonNode> all = new ArrayList<>();
        week.get("days").forEach(d -> d.get("classes").forEach(all::add));
        return all;
    }

    private static List<String> texts(JsonNode array, String field) {
        List<String> values = new ArrayList<>();
        array.forEach(n -> values.add(n.get(field).asText()));
        return values;
    }

    private static JsonNode only(JsonNode array, java.util.function.Predicate<JsonNode> matches) {
        List<JsonNode> found = new ArrayList<>();
        array.forEach(
                n -> {
                    if (matches.test(n)) {
                        found.add(n);
                    }
                });
        assertThat(found).as("exactly one match in %s", array).hasSize(1);
        return found.get(0);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
