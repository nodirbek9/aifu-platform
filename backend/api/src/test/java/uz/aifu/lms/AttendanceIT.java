package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
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
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.AttendanceRequest;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.ClassSessionView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.ClassTopicView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.JournalGroupChoice;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.JournalView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.MarkRequest;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.MyCourseAttendance;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.PlannedClassView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.RosterRow;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.SessionDetailsRequest;
import uz.aifu.lms.attendance.domain.AttendanceStatus;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.curriculum.domain.Lesson;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ClassView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.CreateTimetableRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.DayView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.EntryRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.EntryView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ExceptionRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.PairSlotView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.RoomRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.RoomView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.TimetableView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.WeekView;
import uz.aifu.lms.scheduling.app.WeekCalendar;
import uz.aifu.lms.scheduling.domain.ExceptionKind;
import uz.aifu.lms.scheduling.domain.RoomType;
import uz.aifu.lms.scheduling.domain.WeekParity;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.CurriculumFixtures;
import uz.aifu.lms.support.fixtures.SchedulingFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Class sessions and attendance end to end against real Postgres (product spec T-03, T-04.3, T-10,
 * S-02.4, S-10): the teacher's class page, linking the lesson taught, marking attendance, the
 * journal and the thematic plan, a student's own view, and who may do what.
 */
class AttendanceIT extends AbstractIntegrationTest {

    private static final int MONDAY = 1;

    @Autowired private UserFixtures userFixtures;
    @Autowired private SchedulingFixtures fixtures;
    @Autowired private CurriculumFixtures curriculumFixtures;
    @LocalServerPort private int port;

    /** A published timetable with one weekly Monday lecture for a group of three students. */
    private final class World {
        final String admin = login(userFixtures.activeUser(RoleCode.ADMIN));
        final Term term;
        final User teacher = userFixtures.activeUser(RoleCode.TEACHER);
        final String teacherToken = login(teacher);
        final Course course;
        final GroupView group;
        final User anna;
        final User bek;
        final User chori;
        final TimetableView timetable;
        final EntryView lecture;

        /** The Monday of the term's second week: a date the lecture is held on. */
        final LocalDate monday;

        World(Term term) {
            this.term = term;
            this.course = fixtures.course(term, teacher);
            this.group =
                    post(
                                    admin,
                                    "/api/v1/admin/groups",
                                    new GroupRequest(
                                            "AT-" + SchedulingFixtures.unique(8),
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
            this.anna = fixtures.student(group.code());
            this.bek = fixtures.student(group.code());
            this.chori = fixtures.student(group.code());
            RoomView room =
                    post(
                                    admin,
                                    "/api/v1/admin/rooms",
                                    new RoomRequest(
                                            "A",
                                            SchedulingFixtures.unique(8),
                                            null,
                                            40,
                                            RoomType.CLASSROOM,
                                            true),
                                    RoomView.class)
                            .getBody();
            List<PairSlotView> slots =
                    rest.exchange(
                                    url("/api/v1/timetable/pair-slots"),
                                    HttpMethod.GET,
                                    new HttpEntity<>(bearer(admin)),
                                    new ParameterizedTypeReference<List<PairSlotView>>() {})
                            .getBody();
            this.timetable =
                    post(
                                    admin,
                                    "/api/v1/admin/timetables",
                                    new CreateTimetableRequest(term.getId(), "Draft", null),
                                    TimetableView.class)
                            .getBody();
            this.lecture =
                    post(
                                    admin,
                                    "/api/v1/admin/timetables/" + timetable.id() + "/entries",
                                    new EntryRequest(
                                            course.getId(),
                                            SessionType.LECTURE,
                                            teacher.getId(),
                                            room.id(),
                                            MONDAY,
                                            slots.get(0).id(),
                                            WeekParity.EVERY,
                                            null,
                                            null,
                                            null,
                                            null,
                                            List.of(group.id()),
                                            null,
                                            null),
                                    EntryView.class)
                            .getBody();
            publish(timetable);
            this.monday = WeekCalendar.mondayOf(term.getStartsOn()).plusWeeks(1);
        }

        void publish(TimetableView draft) {
            assertThat(
                            post(
                                            admin,
                                            "/api/v1/admin/timetables/" + draft.id() + "/publish",
                                            null,
                                            TimetableView.class)
                                    .getStatusCode())
                    .isEqualTo(HttpStatus.OK);
        }

        String classPath(LocalDate date) {
            return "/api/v1/teacher/class-sessions/" + lecture.id() + "/" + date;
        }

        ResponseEntity<ClassSessionView> mark(LocalDate date, MarkRequest... marks) {
            return put(
                    teacherToken,
                    classPath(date) + "/attendance",
                    new AttendanceRequest(List.of(marks)),
                    ClassSessionView.class);
        }
    }

    @Test
    void classPage_linksTheLesson_marksAttendance_andFeedsJournalPlanAndStudentView() {
        World w = new World(fixtures.pastTerm());
        Lesson lesson = curriculumFixtures.publishedLesson(w.course);

        // nothing saved yet: the roster with no marks, and the next lecture suggested as the topic
        ClassSessionView fresh =
                get(w.teacherToken, w.classPath(w.monday), ClassSessionView.class).getBody();
        assertThat(fresh.sessionId()).isNull();
        assertThat(fresh.held()).isTrue();
        assertThat(fresh.canMarkAttendance()).isTrue();
        assertThat(fresh.classInfo().courseId()).isEqualTo(w.course.getId());
        assertThat(fresh.students())
                .extracting(RosterRow::userId)
                .containsExactlyInAnyOrder(w.anna.getId(), w.bek.getId(), w.chori.getId());
        assertThat(fresh.students()).extracting(RosterRow::status).containsOnlyNulls();
        assertThat(fresh.totals().unmarked()).isEqualTo(3);
        assertThat(fresh.totals().percent()).isNull();
        assertThat(fresh.lesson()).isNull();
        assertThat(fresh.suggestedLesson().id()).isEqualTo(lesson.getId());

        // a Tuesday has no such class
        assertThat(
                        get(w.teacherToken, w.classPath(w.monday.plusDays(1)), ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        // link the lesson and write notes
        ClassSessionView described =
                put(
                                w.teacherToken,
                                w.classPath(w.monday),
                                new SessionDetailsRequest(
                                        lesson.getId(), null, "Start with a recap of week 1"),
                                ClassSessionView.class)
                        .getBody();
        assertThat(described.sessionId()).isNotNull();
        assertThat(described.lesson().title()).isEqualTo("Fixture lesson");
        assertThat(described.suggestedLesson()).isNull();
        assertThat(described.notes()).isEqualTo("Start with a recap of week 1");

        // a lesson of another course is refused
        Lesson foreign = curriculumFixtures.publishedLesson(fixtures.course(w.term, null));
        assertThat(
                        put(
                                        w.teacherToken,
                                        w.classPath(w.monday),
                                        new SessionDetailsRequest(foreign.getId(), null, null),
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        // mark two of three; the third stays unmarked and does not count
        ClassSessionView marked =
                w.mark(
                                w.monday,
                                new MarkRequest(w.anna.getId(), AttendanceStatus.PRESENT, null),
                                new MarkRequest(
                                        w.bek.getId(), AttendanceStatus.ABSENT, "no reason"))
                        .getBody();
        assertThat(marked.sessionId()).isEqualTo(described.sessionId());
        assertThat(marked.attendanceMarkedAt()).isNotNull();
        assertThat(marked.totals().present()).isEqualTo(1);
        assertThat(marked.totals().absent()).isEqualTo(1);
        assertThat(marked.totals().unmarked()).isEqualTo(1);
        assertThat(marked.totals().percent()).isEqualTo(50);

        // correct one mark, add the third: students not listed keep theirs
        ClassSessionView corrected =
                w.mark(
                                w.monday,
                                new MarkRequest(w.bek.getId(), AttendanceStatus.LATE, null),
                                new MarkRequest(w.chori.getId(), AttendanceStatus.EXCUSED, null))
                        .getBody();
        assertThat(corrected.totals().present()).isEqualTo(1);
        assertThat(corrected.totals().late()).isEqualTo(1);
        assertThat(corrected.totals().excused()).isEqualTo(1);
        assertThat(corrected.totals().percent()).isEqualTo(67);

        // someone who is not in the class's group cannot be marked
        User stranger = fixtures.student(null);
        ResponseEntity<ApiError> refused =
                put(
                        w.teacherToken,
                        w.classPath(w.monday) + "/attendance",
                        new AttendanceRequest(
                                List.of(
                                        new MarkRequest(
                                                stranger.getId(), AttendanceStatus.PRESENT, null))),
                        ApiError.class);
        assertThat(refused.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // the journal: every Monday of the term held so far, the one marked carrying its lesson;
        // the other classes are there too, waiting for their attendance; all three students
        JournalView journal =
                get(
                                w.teacherToken,
                                "/api/v1/teacher/courses/"
                                        + w.course.getId()
                                        + "/attendance?groupId="
                                        + w.group.id(),
                                JournalView.class)
                        .getBody();
        assertThat(journal.sessions()).hasSizeGreaterThan(1);
        assertThat(journal.sessions().get(0).date()).isEqualTo(w.monday);
        assertThat(journal.sessions())
                .filteredOn(s -> s.attendanceTaken())
                .singleElement()
                .satisfies(
                        s -> {
                            assertThat(s.sessionId()).isEqualTo(described.sessionId());
                            assertThat(s.lessonTitle()).isEqualTo("Fixture lesson");
                        });
        assertThat(journal.sessions().subList(1, journal.sessions().size()))
                .allSatisfy(s -> assertThat(s.sessionId()).isNull());
        assertThat(journal.students()).hasSize(3);

        // a period without any class still lists the group's students
        JournalView empty =
                get(
                                w.teacherToken,
                                "/api/v1/teacher/courses/"
                                        + w.course.getId()
                                        + "/attendance?groupId="
                                        + w.group.id()
                                        + "&from="
                                        + w.monday.plusDays(1)
                                        + "&to="
                                        + w.monday.plusDays(2),
                                JournalView.class)
                        .getBody();
        assertThat(empty.sessions()).isEmpty();
        assertThat(empty.students()).hasSize(3);

        // the journal is opened by group: the teacher's groups, each with the course it meets
        List<JournalGroupChoice> choices =
                rest.exchange(
                                url("/api/v1/teacher/journal/groups"),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(w.teacherToken)),
                                new ParameterizedTypeReference<List<JournalGroupChoice>>() {})
                        .getBody();
        assertThat(choices)
                .filteredOn(c -> c.groupId().equals(w.group.id()))
                .singleElement()
                .satisfies(
                        c ->
                                assertThat(c.courses())
                                        .extracting(course -> course.courseId())
                                        .containsExactly(w.course.getId()));
        assertThat(
                        journal.students().stream()
                                .filter(r -> r.userId().equals(w.bek.getId()))
                                .findFirst()
                                .orElseThrow()
                                .marks())
                .containsEntry(described.sessionId(), AttendanceStatus.LATE);

        // the thematic plan: every Monday of the term, the marked one carrying its lesson
        List<PlannedClassView> plan =
                rest.exchange(
                                url("/api/v1/teacher/courses/" + w.course.getId() + "/class-plan"),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(w.teacherToken)),
                                new ParameterizedTypeReference<List<PlannedClassView>>() {})
                        .getBody();
        assertThat(plan).hasSizeGreaterThan(10);
        PlannedClassView taught =
                plan.stream()
                        .filter(p -> p.classInfo().date().equals(w.monday))
                        .findFirst()
                        .orElseThrow();
        assertThat(taught.weekNumber()).isEqualTo(2);
        assertThat(taught.lesson().id()).isEqualTo(lesson.getId());
        assertThat(taught.attendanceMarked()).isTrue();
        assertThat(plan.stream().filter(p -> p.sessionId() != null)).hasSize(1);

        // the student sees their own record and the topic of the class, nothing more
        String annaToken = login(w.anna);
        List<MyCourseAttendance> mine =
                rest.exchange(
                                url("/api/v1/attendance/me"),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(annaToken)),
                                new ParameterizedTypeReference<List<MyCourseAttendance>>() {})
                        .getBody();
        assertThat(mine).hasSize(1);
        assertThat(mine.get(0).courseId()).isEqualTo(w.course.getId());
        assertThat(mine.get(0).totals().percent()).isEqualTo(100);
        assertThat(mine.get(0).records()).hasSize(1);
        assertThat(mine.get(0).records().get(0).status()).isEqualTo(AttendanceStatus.PRESENT);

        ClassTopicView topic =
                get(
                                annaToken,
                                "/api/v1/class-sessions/"
                                        + w.lecture.id()
                                        + "/"
                                        + w.monday
                                        + "/topic",
                                ClassTopicView.class)
                        .getBody();
        assertThat(topic.lessonId()).isEqualTo(lesson.getId());
        assertThat(topic.lessonTitle()).isEqualTo("Fixture lesson");

        assertThat(get(annaToken, w.classPath(w.monday), String.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(
                        get(
                                        annaToken,
                                        "/api/v1/teacher/courses/"
                                                + w.course.getId()
                                                + "/attendance",
                                        String.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        // a teacher who has nothing to do with the course is refused; an admin is not
        String otherTeacher = login(userFixtures.activeUser(RoleCode.TEACHER));
        assertThat(get(otherTeacher, w.classPath(w.monday), ApiError.class).getBody().code())
                .isEqualTo("NOT_COURSE_TEACHER");
        assertThat(get(w.admin, w.classPath(w.monday), ClassSessionView.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void attendance_isRefusedForFutureAndCancelledClasses() {
        World future = new World(fixtures.term());
        ResponseEntity<ApiError> tooEarly =
                put(
                        future.teacherToken,
                        future.classPath(future.monday) + "/attendance",
                        new AttendanceRequest(
                                List.of(
                                        new MarkRequest(
                                                future.anna.getId(),
                                                AttendanceStatus.PRESENT,
                                                null))),
                        ApiError.class);
        assertThat(tooEarly.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // ...but its topic can already be planned
        assertThat(
                        put(
                                        future.teacherToken,
                                        future.classPath(future.monday),
                                        new SessionDetailsRequest(null, "Kirish", null),
                                        ClassSessionView.class)
                                .getBody()
                                .topic())
                .isEqualTo("Kirish");

        World past = new World(fixtures.pastTerm());
        assertThat(
                        post(
                                        past.admin,
                                        "/api/v1/admin/timetable-entries/"
                                                + past.lecture.id()
                                                + "/exceptions",
                                        new ExceptionRequest(
                                                past.monday,
                                                ExceptionKind.CANCELLED,
                                                null,
                                                null,
                                                null,
                                                null,
                                                "teacher is ill"),
                                        String.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        ClassSessionView cancelled =
                get(past.teacherToken, past.classPath(past.monday), ClassSessionView.class)
                        .getBody();
        assertThat(cancelled.held()).isFalse();
        assertThat(cancelled.canMarkAttendance()).isFalse();
        assertThat(
                        put(
                                        past.teacherToken,
                                        past.classPath(past.monday) + "/attendance",
                                        new AttendanceRequest(
                                                List.of(
                                                        new MarkRequest(
                                                                past.anna.getId(),
                                                                AttendanceStatus.PRESENT,
                                                                null))),
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void journal_survivesPublishingACorrectedTimetable() {
        World w = new World(fixtures.pastTerm());
        ClassSessionView marked =
                w.mark(w.monday, new MarkRequest(w.anna.getId(), AttendanceStatus.PRESENT, null))
                        .getBody();

        // the admin publishes a corrected copy: every class gets a new entry id
        TimetableView copy =
                post(
                                w.admin,
                                "/api/v1/admin/timetables",
                                new CreateTimetableRequest(w.term.getId(), "v2", w.timetable.id()),
                                TimetableView.class)
                        .getBody();
        w.publish(copy);
        WeekView week =
                get(w.teacherToken, "/api/v1/timetable/me?date=" + w.monday, WeekView.class)
                        .getBody();
        ClassView sameClass =
                week.days().stream()
                        .map(DayView::classes)
                        .flatMap(List::stream)
                        .findFirst()
                        .orElseThrow();
        assertThat(sameClass.entryId()).isNotEqualTo(w.lecture.id());

        ClassSessionView reopened =
                get(
                                w.teacherToken,
                                "/api/v1/teacher/class-sessions/"
                                        + sameClass.entryId()
                                        + "/"
                                        + w.monday,
                                ClassSessionView.class)
                        .getBody();
        assertThat(reopened.sessionId()).isEqualTo(marked.sessionId());
        assertThat(reopened.totals().present()).isEqualTo(1);
    }

    // ------------------------------------------------------------------ helpers

    private <T> ResponseEntity<T> get(String token, String path, Class<T> type) {
        return rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(bearer(token)), type);
    }

    private <T> ResponseEntity<T> post(String token, String path, Object body, Class<T> type) {
        return rest.exchange(
                url(path), HttpMethod.POST, new HttpEntity<>(body, bearer(token)), type);
    }

    private <T> ResponseEntity<T> put(String token, String path, Object body, Class<T> type) {
        return rest.exchange(
                url(path), HttpMethod.PUT, new HttpEntity<>(body, bearer(token)), type);
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
