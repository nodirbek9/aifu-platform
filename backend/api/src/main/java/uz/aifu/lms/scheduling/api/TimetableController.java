package uz.aifu.lms.scheduling.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.security.AuthenticatedUser;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.DayView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.PairSlotView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.RoomView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.TimetableDirectory;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.WeekView;
import uz.aifu.lms.scheduling.app.SchedulingReferenceService;
import uz.aifu.lms.scheduling.app.TimetableQueryService;
import uz.aifu.lms.scheduling.app.TimetableQueryService.Filter;

/**
 * The published timetable for everyone (spec S-02, T-02): my own week, today, and any group's,
 * teacher's or room's week. A timetable is public inside the university, so any signed-in user with
 * {@code TIMETABLE_READ} may look up any group, teacher or room.
 */
@RestController
@RequestMapping("/api/v1/timetable")
@Tag(
        name = "Timetable",
        description = "The published timetable: my week, today, by group / teacher / room")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('TIMETABLE_READ')")
public class TimetableController {

    /** Matches no class: a student who has no group yet gets an empty week, not everyone's. */
    private static final UUID NOBODY = new UUID(0L, 0L);

    private final TimetableQueryService queries;
    private final SchedulingReferenceService reference;
    private final CurrentUserProvider currentUser;
    private final SchedulingWeb web;

    public TimetableController(
            TimetableQueryService queries,
            SchedulingReferenceService reference,
            CurrentUserProvider currentUser,
            SchedulingWeb web) {
        this.queries = queries;
        this.reference = reference;
        this.currentUser = currentUser;
        this.web = web;
    }

    @GetMapping("/me")
    @Operation(
            summary = "My week",
            description =
                    "A student gets their group's classes, a teacher the classes they teach. Someone"
                            + " who is both chooses with as=student|teacher (default: teacher). The"
                            + " response already has one-off changes, holidays and the odd/even week"
                            + " applied.")
    public WeekView myWeek(
            @Parameter(description = "Any date of the week; default today")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date,
            @RequestParam(required = false) String as,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return queries.week(web.dateOrToday(date), myFilter(as), web.locale(locale, http));
    }

    @GetMapping("/today")
    @Operation(summary = "My classes today (campus date, Asia/Tashkent)")
    public DayView today(
            @RequestParam(required = false) String as,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        LocalDate today = web.today();
        WeekView week = queries.week(today, myFilter(as), web.locale(locale, http));
        return week.days().stream().filter(d -> d.date().equals(today)).findFirst().orElseThrow();
    }

    @GetMapping("/week")
    @Operation(summary = "A week of one group, teacher or room (exactly one of the three ids)")
    public WeekView week(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date,
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        long given =
                java.util.stream.Stream.of(groupId, teacherId, roomId)
                        .filter(java.util.Objects::nonNull)
                        .count();
        if (given != 1) {
            throw Exceptions.validation("Give exactly one of groupId, teacherId, roomId");
        }
        return queries.week(
                web.dateOrToday(date),
                new Filter(groupId, teacherId, roomId),
                web.locale(locale, http));
    }

    @GetMapping("/directory")
    @Operation(
            summary = "Groups and teachers that have classes, to look up anybody's week",
            description = "From the timetable published for the term containing the date.")
    public TimetableDirectory directory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        return queries.directory(web.dateOrToday(date));
    }

    @GetMapping("/pair-slots")
    @Operation(summary = "The bell schedule")
    public List<PairSlotView> pairSlots() {
        return reference.listPairSlots();
    }

    @GetMapping("/rooms")
    @Operation(summary = "Rooms (to look up a room's week)")
    public List<RoomView> rooms() {
        return reference.listRooms();
    }

    private Filter myFilter(String as) {
        AuthenticatedUser user = currentUser.require();
        boolean asStudent = "student".equalsIgnoreCase(as) || (!user.isTeacher() && as == null);
        if (user.isTeacher() && !asStudent) {
            return Filter.teacher(user.id());
        }
        return Filter.group(reference.groupIdOfStudent(user.id()).orElse(NOBODY));
    }
}
