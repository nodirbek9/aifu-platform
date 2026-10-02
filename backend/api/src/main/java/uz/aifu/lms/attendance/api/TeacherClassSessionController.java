package uz.aifu.lms.attendance.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.AttendanceRequest;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.ClassSessionView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.JournalView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.PlannedClassView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.SessionDetailsRequest;
import uz.aifu.lms.attendance.app.AttendanceJournalService;
import uz.aifu.lms.attendance.app.ClassSessionService;

/**
 * The teacher's side of a class (spec T-03, T-04.3, T-10): the class page with its lesson, notes
 * and attendance; the calendar-thematic plan; the attendance journal. A class is addressed by its
 * timetable entry and date, exactly as the week view returns it.
 */
@RestController
@RequestMapping("/api/v1/teacher")
@Tag(
        name = "Teacher: class sessions and attendance",
        description = "Class page, lesson of the class, attendance, journal, thematic plan")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ATTENDANCE_MANAGE')")
public class TeacherClassSessionController {

    private final ClassSessionService classSessions;
    private final AttendanceJournalService journal;
    private final AttendanceWeb web;

    public TeacherClassSessionController(
            ClassSessionService classSessions,
            AttendanceJournalService journal,
            AttendanceWeb web) {
        this.classSessions = classSessions;
        this.journal = journal;
        this.web = web;
    }

    @GetMapping("/class-sessions/{entryId}/{date}")
    @Operation(
            summary = "The class page: class details, linked lesson, notes, roster with marks",
            description =
                    "Open to the class's teacher (or the one replacing them that day), any teacher"
                            + " of the course, and admins. Reading stores nothing.")
    public ClassSessionView get(
            @PathVariable UUID entryId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return classSessions.view(entryId, date, web.locale(locale, http));
    }

    @PutMapping("/class-sessions/{entryId}/{date}")
    @Operation(
            summary = "Link the lesson taught, set a free-text topic and private notes",
            description = "Works for future classes too: that is how the thematic plan is edited.")
    public ClassSessionView describe(
            @PathVariable UUID entryId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody SessionDetailsRequest request,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return classSessions.describe(entryId, date, request, web.locale(locale, http));
    }

    @PutMapping("/class-sessions/{entryId}/{date}/attendance")
    @Operation(
            summary = "Mark attendance",
            description =
                    "Send any subset of the roster; the rest keep their marks. Refused for a class"
                            + " that is not held that day (422) or lies in the future (400).")
    public ClassSessionView markAttendance(
            @PathVariable UUID entryId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody AttendanceRequest request,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return classSessions.markAttendance(
                entryId, date, request, web.locale(locale, http), AttendanceWeb.clientIp(http));
    }

    @GetMapping("/courses/{courseId}/class-plan")
    @Operation(
            summary = "Calendar-thematic plan: every class of the term with its topic",
            description = "From the published timetable; empty when there is none yet.")
    public List<PlannedClassView> plan(
            @PathVariable UUID courseId,
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return classSessions.plan(courseId, groupId, web.locale(locale, http));
    }

    @GetMapping("/courses/{courseId}/attendance")
    @Operation(summary = "Attendance journal: students x classes whose attendance was taken")
    public JournalView journal(
            @PathVariable UUID courseId,
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return journal.journal(courseId, groupId, from, to, web.locale(locale, http));
    }
}
