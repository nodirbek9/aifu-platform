package uz.aifu.lms.attendance.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.ClassTopicView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.MyCourseAttendance;
import uz.aifu.lms.attendance.app.AttendanceJournalService;
import uz.aifu.lms.attendance.app.ClassSessionService;
import uz.aifu.lms.common.security.CurrentUserProvider;

/** What a student sees: their own attendance (spec S-10) and the topic of a class (S-02.4). */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Attendance", description = "My attendance; the topic of a class")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceJournalService journal;
    private final ClassSessionService classSessions;
    private final CurrentUserProvider currentUser;
    private final AttendanceWeb web;

    public AttendanceController(
            AttendanceJournalService journal,
            ClassSessionService classSessions,
            CurrentUserProvider currentUser,
            AttendanceWeb web) {
        this.journal = journal;
        this.classSessions = classSessions;
        this.currentUser = currentUser;
        this.web = web;
    }

    @GetMapping("/attendance/me")
    @PreAuthorize("hasAuthority('ATTENDANCE_SELF_READ')")
    @Operation(
            summary = "My attendance, one block per course",
            description =
                    "Only classes where the teacher marked me count. percent = (present + late) /"
                            + " marked classes.")
    public List<MyCourseAttendance> mine(
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return journal.mine(currentUser.require().id(), courseId, web.locale(locale, http));
    }

    @GetMapping("/class-sessions/{entryId}/{date}/topic")
    @PreAuthorize("hasAuthority('TIMETABLE_READ')")
    @Operation(
            summary = "The topic of a class: the lesson its teacher linked, if any",
            description =
                    "As public as the timetable itself. lessonId is set only for a published"
                            + " lesson; opening it still needs enrollment in the course.")
    public ClassTopicView topic(
            @PathVariable UUID entryId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String locale,
            HttpServletRequest http) {
        return classSessions.topic(entryId, date, web.locale(locale, http));
    }
}
