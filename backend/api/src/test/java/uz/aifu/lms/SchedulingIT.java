package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.CheckResult;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ClassView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.CreateTimetableRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.DayView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.EntryRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.EntryView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ExceptionRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupStudentView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupStudentsRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.HolidayRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.LoadBulkRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.LoadItemRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.LoadRow;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.PairSlotView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.RoomRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.RoomView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.TimetableDirectory;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.TimetableView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.WeekView;
import uz.aifu.lms.scheduling.app.WeekCalendar;
import uz.aifu.lms.scheduling.domain.ExceptionKind;
import uz.aifu.lms.scheduling.domain.RoomType;
import uz.aifu.lms.scheduling.domain.TimetableStatus;
import uz.aifu.lms.scheduling.domain.WeekParity;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.SchedulingFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * The timetable end to end against real Postgres (product spec S-02, T-02, A-02..A-04): groups and
 * rosters, conflict and warning rules, publishing, the concrete week a student and a teacher see
 * (odd/even weeks, one-off changes, holidays), the load report, and who may call what.
 */
class SchedulingIT extends AbstractIntegrationTest {

    private static final int MONDAY = 1;
    private static final int TUESDAY = 2;

    @Autowired private UserFixtures userFixtures;
    @Autowired private SchedulingFixtures fixtures;
    @LocalServerPort private int port;

    /** One timetable under construction with everything a class needs. */
    private final class World {
        final String admin = login(userFixtures.activeUser(RoleCode.ADMIN));
        final Term term = fixtures.term();
        final User teacher = userFixtures.activeUser(RoleCode.TEACHER);
        final User otherTeacher = userFixtures.activeUser(RoleCode.TEACHER);
        final Course course = fixtures.course(term, teacher);
        final Course otherCourse = fixtures.course(term, otherTeacher);
        final GroupView group = createGroup(admin, "G-" + SchedulingFixtures.unique(8), 20);
        final GroupView otherGroup = createGroup(admin, "G-" + SchedulingFixtures.unique(8), 20);
        final RoomView room = createRoom(admin, 40, RoomType.CLASSROOM);
        final RoomView otherRoom = createRoom(admin, 40, RoomType.CLASSROOM);
        final List<PairSlotView> slots = pairSlots(admin);
        final TimetableView timetable =
                post(
                                admin,
                                "/api/v1/admin/timetables",
                                new CreateTimetableRequest(term.getId(), "Draft", null),
                                TimetableView.class)
                        .getBody();

        /** Monday of the term's first week (week 1 is ODD). */
        final LocalDate week1 = WeekCalendar.mondayOf(term.getStartsOn()).plusWeeks(1);

        EntryRequest lecture(int day, int pairIndex, WeekParity parity) {
            return entry(course, teacher, room, group, day, pairIndex, parity, null, null);
        }

        EntryRequest entry(
                Course c,
                User t,
                RoomView r,
                GroupView g,
                int day,
                int pairIndex,
                WeekParity parity,
                String subgroup,
                Boolean force) {
            return new EntryRequest(
                    c.getId(),
                    SessionType.LECTURE,
                    t.getId(),
                    r == null ? null : r.id(),
                    day,
                    slots.get(pairIndex).id(),
                    parity,
                    null,
                    null,
                    subgroup,
                    null,
                    List.of(g.id()),
                    force,
                    force == null ? null : "agreed with the department");
        }

        ResponseEntity<EntryView> add(EntryRequest request) {
            return post(
                    admin,
                    "/api/v1/admin/timetables/" + timetable.id() + "/entries",
                    request,
                    EntryView.class);
        }

        ResponseEntity<ApiError> addRefused(EntryRequest request) {
            return post(
                    admin,
                    "/api/v1/admin/timetables/" + timetable.id() + "/entries",
                    request,
                    ApiError.class);
        }

        void publish() {
            assertThat(
                            post(
                                            admin,
                                            "/api/v1/admin/timetables/"
                                                    + timetable.id()
                                                    + "/publish",
                                            null,
                                            TimetableView.class)
                                    .getBody()
                                    .status())
                    .isEqualTo(TimetableStatus.PUBLISHED);
        }
    }

    // ------------------------------------------------------------------ groups

    @Test
    void groups_rosterMovesStudents_andOnlyStaffMayRead() {
        String admin = login(userFixtures.activeUser(RoleCode.ADMIN));
        String code = "BK-" + SchedulingFixtures.unique(8);
        GroupView group = createGroup(admin, code, 25);
        assertThat(group.studentCount()).isZero();

        ResponseEntity<ApiError> duplicate =
                post(
                        admin,
                        "/api/v1/admin/groups",
                        new GroupRequest(
                                code.toLowerCase(), null, null, null, null, null, null, null),
                        ApiError.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // A student created with an unknown group code gets that group created (V019 trigger).
        String legacyCode = "OLD-" + SchedulingFixtures.unique(8);
        User student = fixtures.student(legacyCode);
        List<GroupView> all =
                getList(admin, "/api/v1/groups", new ParameterizedTypeReference<>() {});
        assertThat(all).extracting(GroupView::code).contains(code, legacyCode);

        List<GroupStudentView> roster =
                rest.exchange(
                                url("/api/v1/admin/groups/" + group.id() + "/students"),
                                HttpMethod.POST,
                                new HttpEntity<>(
                                        new GroupStudentsRequest(List.of(student.getId())),
                                        bearer(admin)),
                                new ParameterizedTypeReference<List<GroupStudentView>>() {})
                        .getBody();
        assertThat(roster).extracting(GroupStudentView::userId).containsExactly(student.getId());
        assertThat(
                        get(admin, "/api/v1/groups/" + group.id(), GroupView.class)
                                .getBody()
                                .studentCount())
                .isEqualTo(1);

        ResponseEntity<ApiError> deleteUsed =
                rest.exchange(
                        url("/api/v1/admin/groups/" + group.id()),
                        HttpMethod.DELETE,
                        new HttpEntity<>(bearer(admin)),
                        ApiError.class);
        assertThat(deleteUsed.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        String teacher = login(userFixtures.activeUser(RoleCode.TEACHER));
        assertThat(
                        get(teacher, "/api/v1/groups/" + group.id() + "/students", String.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(get(login(student), "/api/v1/groups", String.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(
                        post(
                                        teacher,
                                        "/api/v1/admin/groups",
                                        new GroupRequest(
                                                "X-" + SchedulingFixtures.unique(6),
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null),
                                        String.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ------------------------------------------------------------------ conflicts and warnings

    @Test
    @SuppressWarnings("unchecked")
    void conflicts_teacherRoomAndGroupAreRefused_parityAndSubgroupsAreNot() {
        World w = new World();
        EntryView first = w.add(w.lecture(MONDAY, 0, WeekParity.EVERY)).getBody();
        assertThat(first.groups()).extracting(g -> g.code()).containsExactly(w.group.code());
        assertThat(first.courseTitle()).startsWith("Dasturlash");

        // the same teacher, elsewhere, with another group
        ResponseEntity<ApiError> teacherBusy =
                w.addRefused(
                        w.entry(
                                w.course,
                                w.teacher,
                                w.otherRoom,
                                w.otherGroup,
                                MONDAY,
                                0,
                                WeekParity.EVERY,
                                null,
                                null));
        assertThat(teacherBusy.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(teacherBusy.getBody().code()).isEqualTo("TIMETABLE_CONFLICT");
        List<Map<String, Object>> conflicts =
                (List<Map<String, Object>>) teacherBusy.getBody().details().get("conflicts");
        assertThat(conflicts).extracting(c -> c.get("type")).containsExactly("TEACHER");
        assertThat(conflicts.get(0).get("entryId")).isEqualTo(first.id().toString());

        // the same room / the same group with another teacher
        assertThat(
                        conflictTypes(
                                w.addRefused(
                                        w.entry(
                                                w.otherCourse,
                                                w.otherTeacher,
                                                w.room,
                                                w.otherGroup,
                                                MONDAY,
                                                0,
                                                WeekParity.EVERY,
                                                null,
                                                null))))
                .containsExactly("ROOM");
        assertThat(
                        conflictTypes(
                                w.addRefused(
                                        w.entry(
                                                w.otherCourse,
                                                w.otherTeacher,
                                                w.otherRoom,
                                                w.group,
                                                MONDAY,
                                                0,
                                                WeekParity.EVERY,
                                                null,
                                                null))))
                .containsExactly("GROUP");

        // odd and even weeks share a slot
        assertThat(w.add(w.lecture(TUESDAY, 0, WeekParity.ODD)).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(w.add(w.lecture(TUESDAY, 0, WeekParity.EVEN)).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(conflictTypes(w.addRefused(w.lecture(TUESDAY, 0, WeekParity.EVERY))))
                .contains("TEACHER", "ROOM", "GROUP");

        // two subgroups of one group at the same time, different teachers and rooms
        assertThat(
                        w.add(
                                        w.entry(
                                                w.course,
                                                w.teacher,
                                                w.room,
                                                w.group,
                                                3,
                                                0,
                                                WeekParity.EVERY,
                                                "A",
                                                null))
                                .getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(
                        w.add(
                                        w.entry(
                                                w.otherCourse,
                                                w.otherTeacher,
                                                w.otherRoom,
                                                w.group,
                                                3,
                                                0,
                                                WeekParity.EVERY,
                                                "B",
                                                null))
                                .getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        // moving a class onto itself is not a conflict; moving it onto a taken slot is
        assertThat(
                        put(
                                        w.admin,
                                        "/api/v1/admin/timetable-entries/" + first.id(),
                                        w.lecture(MONDAY, 1, WeekParity.EVERY),
                                        EntryView.class)
                                .getBody()
                                .pairNo())
                .isEqualTo(2);
        assertThat(
                        put(
                                        w.admin,
                                        "/api/v1/admin/timetable-entries/" + first.id(),
                                        w.lecture(TUESDAY, 0, WeekParity.EVERY),
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        // dry run for the drag preview: nothing is saved
        CheckResult check =
                post(
                                w.admin,
                                "/api/v1/admin/timetables/" + w.timetable.id() + "/entries/check",
                                w.lecture(TUESDAY, 0, WeekParity.ODD),
                                CheckResult.class)
                        .getBody();
        assertThat(check.conflicts()).isNotEmpty();
        assertThat(check.conflicts().get(0).teacherName()).isEqualTo(w.teacher.getFullName());
        assertThat(
                        get(
                                        w.admin,
                                        "/api/v1/admin/timetables/" + w.timetable.id(),
                                        TimetableView.class)
                                .getBody()
                                .entryCount())
                .isEqualTo(5);
    }

    @Test
    @SuppressWarnings("unchecked")
    void warnings_blockUntilForcedWithAReason() {
        World w = new World();
        RoomView tiny = createRoom(w.admin, 5, RoomType.CLASSROOM);
        EntryRequest unassignedTeacherInTinyRoom =
                w.entry(
                        w.course,
                        w.otherTeacher,
                        tiny,
                        w.group,
                        MONDAY,
                        0,
                        WeekParity.EVERY,
                        null,
                        null);

        ResponseEntity<ApiError> refused = w.addRefused(unassignedTeacherInTinyRoom);
        assertThat(refused.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat((List<?>) refused.getBody().details().get("conflicts")).isEmpty();
        List<Map<String, Object>> warnings =
                (List<Map<String, Object>>) refused.getBody().details().get("warnings");
        assertThat(warnings)
                .extracting(x -> x.get("code"))
                .containsExactlyInAnyOrder("ROOM_CAPACITY", "TEACHER_NOT_ASSIGNED");

        ResponseEntity<EntryView> forced =
                w.add(
                        w.entry(
                                w.course,
                                w.otherTeacher,
                                tiny,
                                w.group,
                                MONDAY,
                                0,
                                WeekParity.EVERY,
                                null,
                                true));
        assertThat(forced.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(forced.getBody().warnings()).hasSize(2);
    }

    // ------------------------------------------------------------------ what people see

    @Test
    void publishedWeek_studentAndTeacherSeeTheirClasses_withOddEvenWeeks() {
        World w = new World();
        User student = fixtures.student(w.group.code());
        User outsider = fixtures.student(null);
        w.add(w.lecture(MONDAY, 0, WeekParity.EVERY));
        w.add(w.lecture(TUESDAY, 1, WeekParity.ODD));
        w.add(
                w.entry(
                        w.otherCourse,
                        w.otherTeacher,
                        w.otherRoom,
                        w.otherGroup,
                        MONDAY,
                        0,
                        WeekParity.EVERY,
                        null,
                        null));

        // nothing is visible before publishing
        assertThat(classes(week(login(student), "/api/v1/timetable/me?date=" + w.week1))).isEmpty();
        w.publish();

        // whose week can be looked up: the groups and teachers that have classes
        TimetableDirectory directory =
                get(
                                login(outsider),
                                "/api/v1/timetable/directory?date=" + w.week1,
                                TimetableDirectory.class)
                        .getBody();
        assertThat(directory.groups())
                .extracting(g -> g.code())
                .contains(w.group.code(), w.otherGroup.code());
        assertThat(directory.teachers())
                .extracting(t -> t.id())
                .contains(w.teacher.getId(), w.otherTeacher.getId());

        WeekView oddWeek = week(login(student), "/api/v1/timetable/me?date=" + w.week1.plusDays(3));
        assertThat(oddWeek.weekStart()).isEqualTo(w.week1);
        assertThat(oddWeek.parity())
                .isEqualTo(WeekParity.EVEN); // week 2 of a term whose week 1 is ODD
        assertThat(oddWeek.slots()).hasSizeGreaterThanOrEqualTo(6);
        assertThat(classes(oddWeek)).extracting(ClassView::date).containsExactly(w.week1);

        WeekView nextWeek =
                week(login(student), "/api/v1/timetable/me?date=" + w.week1.plusWeeks(1));
        assertThat(nextWeek.parity()).isEqualTo(WeekParity.ODD);
        assertThat(classes(nextWeek))
                .extracting(ClassView::date)
                .containsExactly(w.week1.plusWeeks(1), w.week1.plusWeeks(1).plusDays(1));
        ClassView monday = classes(nextWeek).get(0);
        assertThat(monday.status()).isEqualTo("SCHEDULED");
        assertThat(monday.teacherName()).isEqualTo(w.teacher.getFullName());
        assertThat(monday.roomLabel()).isEqualTo(w.room.label());
        assertThat(monday.groups()).extracting(g -> g.code()).containsExactly(w.group.code());

        // the teacher sees only their own classes; a student without a group sees none
        assertThat(classes(week(login(w.otherTeacher), "/api/v1/timetable/me?date=" + w.week1)))
                .extracting(ClassView::teacherUserId)
                .containsExactly(w.otherTeacher.getId());
        assertThat(classes(week(login(outsider), "/api/v1/timetable/me?date=" + w.week1)))
                .isEmpty();
        assertThat(
                        classes(
                                week(
                                        login(outsider),
                                        "/api/v1/timetable/week?date="
                                                + w.week1
                                                + "&groupId="
                                                + w.group.id())))
                .hasSize(1);
        assertThat(
                        get(
                                        login(outsider),
                                        "/api/v1/timetable/week?date=" + w.week1,
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        // publishing a newer timetable archives this one
        TimetableView copy =
                post(
                                w.admin,
                                "/api/v1/admin/timetables",
                                new CreateTimetableRequest(w.term.getId(), "v2", w.timetable.id()),
                                TimetableView.class)
                        .getBody();
        assertThat(copy.entryCount()).isEqualTo(3);
        post(
                w.admin,
                "/api/v1/admin/timetables/" + copy.id() + "/publish",
                null,
                TimetableView.class);
        assertThat(
                        get(
                                        w.admin,
                                        "/api/v1/admin/timetables/" + w.timetable.id(),
                                        TimetableView.class)
                                .getBody()
                                .status())
                .isEqualTo(TimetableStatus.ARCHIVED);
        assertThat(classes(week(login(student), "/api/v1/timetable/me?date=" + w.week1)))
                .hasSize(1);

        // the builder is the admin's alone
        assertThat(
                        post(
                                        login(student),
                                        "/api/v1/admin/timetables",
                                        new CreateTimetableRequest(w.term.getId(), "x", null),
                                        String.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(get(login(w.teacher), "/api/v1/admin/timetables", String.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void oneOffChanges_cancelMoveAndHolidays_showInTheWeek_andNotify() {
        World w = new World();
        User student = fixtures.student(w.group.code());
        EntryView lecture = w.add(w.lecture(MONDAY, 0, WeekParity.EVERY)).getBody();
        w.add(
                w.entry(
                        w.otherCourse,
                        w.otherTeacher,
                        w.otherRoom,
                        w.otherGroup,
                        3,
                        2,
                        WeekParity.EVERY,
                        null,
                        null));
        w.publish();
        String studentToken = login(student);
        String exceptions = "/api/v1/admin/timetable-entries/" + lecture.id() + "/exceptions";

        // not a Monday: the class is not held then
        assertThat(
                        post(
                                        w.admin,
                                        exceptions,
                                        new ExceptionRequest(
                                                w.week1.plusDays(1),
                                                ExceptionKind.CANCELLED,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null),
                                        ApiError.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(
                        post(
                                        w.admin,
                                        exceptions,
                                        new ExceptionRequest(
                                                w.week1,
                                                ExceptionKind.CANCELLED,
                                                null,
                                                null,
                                                null,
                                                null,
                                                "teacher is ill"),
                                        String.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        ClassView cancelled =
                classes(week(studentToken, "/api/v1/timetable/me?date=" + w.week1)).get(0);
        assertThat(cancelled.status()).isEqualTo("CANCELLED");
        assertThat(cancelled.changeReason()).isEqualTo("teacher is ill");

        // moving next Monday's class to Thursday, pair 4, clashes with nothing: allowed
        LocalDate monday2 = w.week1.plusWeeks(1);
        assertThat(
                        post(
                                        w.admin,
                                        exceptions,
                                        new ExceptionRequest(
                                                monday2,
                                                ExceptionKind.MOVED,
                                                monday2.plusDays(3),
                                                w.slots.get(3).id(),
                                                null,
                                                null,
                                                "room repair"),
                                        String.class)
                                .getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        List<ClassView> week2 = classes(week(studentToken, "/api/v1/timetable/me?date=" + monday2));
        assertThat(week2).extracting(ClassView::status).containsExactly("MOVED_OUT", "MOVED_IN");
        assertThat(week2.get(1).date()).isEqualTo(monday2.plusDays(3));
        assertThat(week2.get(1).pairNo()).isEqualTo(4);

        // a move into a slot where the room is taken is refused: Wednesday pair 3 holds the other
        // class
        LocalDate monday3 = w.week1.plusWeeks(2);
        ResponseEntity<ApiError> busy =
                post(
                        w.admin,
                        exceptions,
                        new ExceptionRequest(
                                monday3,
                                ExceptionKind.MOVED,
                                monday3.plusDays(2),
                                w.slots.get(2).id(),
                                w.otherRoom.id(),
                                null,
                                null),
                        ApiError.class);
        assertThat(busy.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(conflictTypes(busy)).containsExactly("ROOM");

        // a holiday: the class of that day is shown as not held
        LocalDate monday4 = w.week1.plusWeeks(3);
        post(
                w.admin,
                "/api/v1/admin/holidays",
                new HolidayRequest(monday4, "Bayram " + SchedulingFixtures.unique(6)),
                String.class);
        WeekView holidayWeek = week(studentToken, "/api/v1/timetable/me?date=" + monday4);
        assertThat(holidayWeek.days().get(0).holiday()).startsWith("Bayram");
        assertThat(classes(holidayWeek).get(0).status()).isEqualTo("HOLIDAY");

        // the student was told about the cancellation and the move
        Map<?, ?> unread =
                get(studentToken, "/api/v1/notifications/me/unread-count", Map.class).getBody();
        assertThat(((Number) unread.values().iterator().next()).intValue())
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void load_comparesPlannedHoursWithWhatTheTimetableDelivers() {
        World w = new World();
        w.add(w.lecture(MONDAY, 0, WeekParity.EVERY));
        String loadUrl = "/api/v1/admin/teaching-load?termId=" + w.term.getId();
        put(
                w.admin,
                loadUrl,
                new LoadBulkRequest(
                        List.of(
                                new LoadItemRequest(
                                        w.course.getId(),
                                        w.group.id(),
                                        SessionType.LECTURE,
                                        w.teacher.getId(),
                                        30))),
                String.class);
        // sending the same sheet again changes nothing
        put(
                w.admin,
                loadUrl,
                new LoadBulkRequest(
                        List.of(
                                new LoadItemRequest(
                                        w.course.getId(),
                                        w.group.id(),
                                        SessionType.LECTURE,
                                        w.teacher.getId(),
                                        30))),
                String.class);

        int mondays = 0;
        for (LocalDate d = w.term.getStartsOn();
                !d.isAfter(w.term.getEndsOn());
                d = d.plusDays(1)) {
            if (d.getDayOfWeek().getValue() == MONDAY) {
                mondays++;
            }
        }
        List<LoadRow> planned = getList(w.admin, loadUrl, new ParameterizedTypeReference<>() {});
        assertThat(planned).hasSize(1);
        assertThat(planned.get(0).remainingHours()).isEqualTo(30);

        List<LoadRow> rows =
                getList(
                        w.admin,
                        "/api/v1/admin/timetables/"
                                + w.timetable.id()
                                + "/load?groupId="
                                + w.group.id(),
                        new ParameterizedTypeReference<>() {});
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).plannedHours()).isEqualTo(30);
        assertThat(rows.get(0).scheduledHours()).isEqualTo(mondays * 2);
        assertThat(rows.get(0).remainingHours()).isEqualTo(30 - mondays * 2);
        assertThat(rows.get(0).teacherName()).isEqualTo(w.teacher.getFullName());
    }

    // ------------------------------------------------------------------ helpers

    private GroupView createGroup(String admin, String code, int plannedSize) {
        ResponseEntity<GroupView> response =
                post(
                        admin,
                        "/api/v1/admin/groups",
                        new GroupRequest(code, null, 1, null, null, "uz", plannedSize, null),
                        GroupView.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private RoomView createRoom(String admin, int capacity, RoomType type) {
        return post(
                        admin,
                        "/api/v1/admin/rooms",
                        new RoomRequest(
                                "T", SchedulingFixtures.unique(8), null, capacity, type, true),
                        RoomView.class)
                .getBody();
    }

    private List<PairSlotView> pairSlots(String token) {
        return getList(
                token, "/api/v1/timetable/pair-slots", new ParameterizedTypeReference<>() {});
    }

    private WeekView week(String token, String path) {
        ResponseEntity<WeekView> response = get(token, path, WeekView.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private static List<ClassView> classes(WeekView week) {
        return week.days().stream().map(DayView::classes).flatMap(List::stream).toList();
    }

    @SuppressWarnings("unchecked")
    private static List<Object> conflictTypes(ResponseEntity<ApiError> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        return ((List<Map<String, Object>>) response.getBody().details().get("conflicts"))
                .stream().map(c -> c.get("type")).distinct().toList();
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
