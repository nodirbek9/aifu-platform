package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ClassView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.CreateTimetableRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.EntryRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.EntryView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ExceptionRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ExceptionView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.HolidayRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.LoadRow;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.PairSlotView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.RoomRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.RoomView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.TimetableView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.WeekView;
import uz.aifu.lms.scheduling.app.TimetableQueryService;
import uz.aifu.lms.scheduling.app.WeekCalendar;
import uz.aifu.lms.scheduling.domain.ExceptionKind;
import uz.aifu.lms.scheduling.domain.RoomType;
import uz.aifu.lms.scheduling.domain.WeekParity;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.SchedulingFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

class TimetableAddExceptionIT extends AbstractIntegrationTest {

    private static final int MONDAY = 1;

    @Autowired private UserFixtures userFixtures;
    @Autowired private SchedulingFixtures fixtures;
    @Autowired private TimetableQueryService queryService;
    @LocalServerPort private int port;

    private final class World {
        final String admin = login(userFixtures.activeUser(RoleCode.ADMIN));
        final User teacher = userFixtures.activeUser(RoleCode.TEACHER);
        final User student = fixtures.student("G-" + SchedulingFixtures.unique(8));
        final Term term = fixtures.term();
        final Course course = fixtures.course(term, teacher);
        final GroupView group = createGroup(admin, "G-" + SchedulingFixtures.unique(8), 20);
        final RoomView room = createRoom(admin, 40, RoomType.CLASSROOM);
        final List<PairSlotView> slots = pairSlots(admin);
        final TimetableView timetable =
                post(
                                admin,
                                "/api/v1/admin/timetables",
                                new CreateTimetableRequest(term.getId(), "Draft", null),
                                TimetableView.class)
                        .getBody();

        final LocalDate week1 = WeekCalendar.mondayOf(term.getStartsOn()).plusWeeks(1);

        EntryView addLecture(int day, int pairIndex) {
            return post(
                            admin,
                            "/api/v1/admin/timetables/" + timetable.id() + "/entries",
                            new EntryRequest(
                                    course.getId(),
                                    SessionType.LECTURE,
                                    teacher.getId(),
                                    room.id(),
                                    day,
                                    slots.get(pairIndex).id(),
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
        }

        void publish() {
            post(
                    admin,
                    "/api/v1/admin/timetables/" + timetable.id() + "/publish",
                    null,
                    TimetableView.class);
        }
    }

    private GroupView createGroup(String admin, String code, int plannedSize) {
        return post(
                        admin,
                        "/api/v1/admin/groups",
                        new GroupRequest(code, null, 1, null, null, "uz", plannedSize, null, null),
                        GroupView.class)
                .getBody();
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

    private List<ClassView> classes(WeekView w) {
        return w.days().stream().flatMap(d -> d.classes().stream()).toList();
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
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void addException_createsExtraOccurrence_andChecksConflicts() {
        World w = new World();
        EntryView lecture = w.addLecture(MONDAY, 0);
        w.publish();

        LocalDate addDate = w.week1.plusDays(1); // Tuesday

        // 1. ADD success
        ResponseEntity<ExceptionView> addRes =
                post(
                        w.admin,
                        "/api/v1/admin/timetable-entries/" + lecture.id() + "/exceptions",
                        new ExceptionRequest(
                                addDate,
                                ExceptionKind.ADD,
                                null,
                                w.slots.get(1).id(),
                                null,
                                null,
                                "extra class"),
                        ExceptionView.class);
        assertThat(addRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ExceptionView exceptionView = addRes.getBody();
        assertThat(exceptionView).isNotNull();

        // Added occurrence exists once on the chosen date; original recurrence remains.
        WeekView weekView =
                get(
                                w.admin,
                                "/api/v1/admin/timetables/"
                                        + w.timetable.id()
                                        + "/week?date="
                                        + w.week1,
                                WeekView.class)
                        .getBody();
        List<ClassView> week1Classes = classes(weekView);
        assertThat(week1Classes).hasSize(2);
        assertThat(week1Classes).extracting(ClassView::date).contains(w.week1, addDate);
        assertThat(week1Classes).extracting(ClassView::status).containsOnly("SCHEDULED");

        // Preview, published week, occurrenceOf and load include the extra class.
        WeekView publishedWeek =
                get(
                                w.admin,
                                "/api/v1/timetable/week?date="
                                        + w.week1
                                        + "&groupId="
                                        + w.group.id(),
                                WeekView.class)
                        .getBody();
        assertThat(classes(publishedWeek)).extracting(ClassView::date).contains(addDate);

        var occurrence = queryService.occurrenceOf(lecture.id(), addDate);
        assertThat(occurrence).isPresent();
        assertThat(occurrence.get().status()).isEqualTo("SCHEDULED");
        assertThat(occurrence.get().reason()).isEqualTo("extra class");

        List<LoadRow> loadRows =
                getList(
                        w.admin,
                        "/api/v1/admin/timetables/"
                                + w.timetable.id()
                                + "/load?groupId="
                                + w.group.id(),
                        new ParameterizedTypeReference<>() {});
        assertThat(loadRows).isNotEmpty();
        assertThat(loadRows.get(0).scheduledHours()).isGreaterThan(0);

        // It does not appear the following week.
        WeekView week2View =
                get(
                                w.admin,
                                "/api/v1/admin/timetables/"
                                        + w.timetable.id()
                                        + "/week?date="
                                        + w.week1.plusWeeks(1),
                                WeekView.class)
                        .getBody();
        List<ClassView> week2Classes = classes(week2View);
        assertThat(week2Classes).hasSize(1);
        assertThat(week2Classes.get(0).date()).isEqualTo(w.week1.plusWeeks(1));

        // Duplicate source/date rejected without a 500.
        ResponseEntity<ApiError> dupRes =
                post(
                        w.admin,
                        "/api/v1/admin/timetable-entries/" + lecture.id() + "/exceptions",
                        new ExceptionRequest(
                                addDate,
                                ExceptionKind.ADD,
                                null,
                                w.slots.get(2).id(),
                                null,
                                null,
                                "another extra"),
                        ApiError.class);
        assertThat(dupRes.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(dupRes.getBody().code()).isEqualTo("VALIDATION_FAILED");

        // Conflicts with recurring, moved and added classes:
        // A) Conflicts with added class
        EntryView lecture2 = w.addLecture(3, 1); // Thursday
        ResponseEntity<ApiError> conflictRes =
                post(
                        w.admin,
                        "/api/v1/admin/timetable-entries/" + lecture2.id() + "/exceptions",
                        new ExceptionRequest(
                                addDate,
                                ExceptionKind.ADD,
                                null,
                                w.slots.get(1).id(),
                                null,
                                null,
                                "conflict extra"),
                        ApiError.class);
        assertThat(conflictRes.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(conflictRes.getBody().code()).isEqualTo("TIMETABLE_CONFLICT");

        // B) Conflicts with recurring class (Monday slot 0 is already taken by lecture)
        ResponseEntity<ApiError> recurringConflictRes =
                post(
                        w.admin,
                        "/api/v1/admin/timetable-entries/" + lecture2.id() + "/exceptions",
                        new ExceptionRequest(
                                w.week1,
                                ExceptionKind.ADD,
                                null,
                                w.slots.get(0).id(),
                                null,
                                null,
                                "conflict recurring"),
                        ApiError.class);
        assertThat(recurringConflictRes.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(recurringConflictRes.getBody().code()).isEqualTo("TIMETABLE_CONFLICT");

        // Out-of-term rejected
        ResponseEntity<ApiError> outRes =
                post(
                        w.admin,
                        "/api/v1/admin/timetable-entries/" + lecture.id() + "/exceptions",
                        new ExceptionRequest(
                                w.term.getStartsOn().minusDays(1),
                                ExceptionKind.ADD,
                                null,
                                w.slots.get(0).id(),
                                null,
                                null,
                                "out"),
                        ApiError.class);
        assertThat(outRes.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Invalid teacher rejected
        ResponseEntity<ApiError> badTeacherRes =
                post(
                        w.admin,
                        "/api/v1/admin/timetable-entries/" + lecture.id() + "/exceptions",
                        new ExceptionRequest(
                                w.week1.plusDays(3),
                                ExceptionKind.ADD,
                                null,
                                w.slots.get(0).id(),
                                null,
                                UUID.randomUUID(),
                                "bad teacher"),
                        ApiError.class);
        assertThat(badTeacherRes.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Invalid slot rejected
        ResponseEntity<ApiError> badSlotRes =
                post(
                        w.admin,
                        "/api/v1/admin/timetable-entries/" + lecture.id() + "/exceptions",
                        new ExceptionRequest(
                                w.week1.plusDays(3),
                                ExceptionKind.ADD,
                                null,
                                UUID.randomUUID(),
                                null,
                                null,
                                "bad slot"),
                        ApiError.class);
        assertThat(badSlotRes.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Holiday rejected
        LocalDate holidayDate = w.week1.plusDays(2);
        post(
                w.admin,
                "/api/v1/admin/holidays",
                new HolidayRequest(holidayDate, "Holiday"),
                String.class);
        ResponseEntity<ApiError> holRes =
                post(
                        w.admin,
                        "/api/v1/admin/timetable-entries/" + lecture.id() + "/exceptions",
                        new ExceptionRequest(
                                holidayDate,
                                ExceptionKind.ADD,
                                null,
                                w.slots.get(0).id(),
                                null,
                                null,
                                "holiday class"),
                        ApiError.class);
        assertThat(holRes.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Removing ADD removes only that occurrence (tested while timetable is still editable).
        ResponseEntity<Void> delRes =
                rest.exchange(
                        url("/api/v1/admin/timetable-exceptions/" + exceptionView.id()),
                        HttpMethod.DELETE,
                        new HttpEntity<>(bearer(w.admin)),
                        Void.class);
        assertThat(delRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        WeekView weekAfterDelete =
                get(
                                w.admin,
                                "/api/v1/admin/timetables/"
                                        + w.timetable.id()
                                        + "/week?date="
                                        + w.week1,
                                WeekView.class)
                        .getBody();
        assertThat(classes(weekAfterDelete)).hasSize(1);
        assertThat(queryService.occurrenceOf(lecture.id(), addDate)).isEmpty();

        // Archived timetable rejected
        post(
                w.admin,
                "/api/v1/admin/timetables",
                new CreateTimetableRequest(w.term.getId(), "v2", w.timetable.id()),
                TimetableView.class);
        // Publishing v2 archives v1
        post(
                w.admin,
                "/api/v1/admin/timetables/"
                        + getList(
                                        w.admin,
                                        "/api/v1/admin/timetables?termId=" + w.term.getId(),
                                        new ParameterizedTypeReference<List<TimetableView>>() {})
                                .stream()
                                .filter(t -> t.name().equals("v2"))
                                .findFirst()
                                .get()
                                .id()
                        + "/publish",
                null,
                String.class);
        ResponseEntity<ApiError> archRes =
                post(
                        w.admin,
                        "/api/v1/admin/timetable-entries/" + lecture.id() + "/exceptions",
                        new ExceptionRequest(
                                w.week1.plusDays(4),
                                ExceptionKind.ADD,
                                null,
                                w.slots.get(0).id(),
                                null,
                                null,
                                "archived class"),
                        ApiError.class);
        assertThat(archRes.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void concurrentConflictingAddRequests_cannotBothSucceed() throws Exception {
        World w = new World();
        EntryView lecture1 = w.addLecture(MONDAY, 0);
        EntryView lecture2 = w.addLecture(3, 1);
        w.publish();

        LocalDate addDate = w.week1.plusDays(1);
        UUID slotId = w.slots.get(1).id();

        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var barrier = new java.util.concurrent.CyclicBarrier(2);
            java.util.concurrent.Callable<Integer> task1 =
                    () -> {
                        barrier.await();
                        return post(
                                        w.admin,
                                        "/api/v1/admin/timetable-entries/"
                                                + lecture1.id()
                                                + "/exceptions",
                                        new ExceptionRequest(
                                                addDate,
                                                ExceptionKind.ADD,
                                                null,
                                                slotId,
                                                null,
                                                null,
                                                "concurrent 1"),
                                        Object.class)
                                .getStatusCode()
                                .value();
                    };
            java.util.concurrent.Callable<Integer> task2 =
                    () -> {
                        barrier.await();
                        return post(
                                        w.admin,
                                        "/api/v1/admin/timetable-entries/"
                                                + lecture2.id()
                                                + "/exceptions",
                                        new ExceptionRequest(
                                                addDate,
                                                ExceptionKind.ADD,
                                                null,
                                                slotId,
                                                null,
                                                null,
                                                "concurrent 2"),
                                        Object.class)
                                .getStatusCode()
                                .value();
                    };
            var future1 = executor.submit(task1);
            var future2 = executor.submit(task2);
            int status1 = future1.get();
            int status2 = future2.get();

            List<Integer> statuses = List.of(status1, status2);
            assertThat(statuses).contains(201);
            assertThat(statuses).contains(409);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void addException_authorization() {
        World w = new World();
        EntryView lecture = w.addLecture(MONDAY, 0);
        w.publish();

        // Teacher mutation attempts return 403
        String teacherToken = login(w.teacher);
        ResponseEntity<String> fail1 =
                post(
                        teacherToken,
                        "/api/v1/admin/timetable-entries/" + lecture.id() + "/exceptions",
                        new ExceptionRequest(
                                w.week1.plusDays(1),
                                ExceptionKind.ADD,
                                null,
                                w.slots.get(1).id(),
                                null,
                                null,
                                "extra class"),
                        String.class);
        assertThat(fail1.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Student mutation attempts return 403
        String studentToken = login(w.student);
        ResponseEntity<String> fail2 =
                post(
                        studentToken,
                        "/api/v1/admin/timetable-entries/" + lecture.id() + "/exceptions",
                        new ExceptionRequest(
                                w.week1.plusDays(1),
                                ExceptionKind.ADD,
                                null,
                                w.slots.get(1).id(),
                                null,
                                null,
                                "extra class"),
                        String.class);
        assertThat(fail2.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
