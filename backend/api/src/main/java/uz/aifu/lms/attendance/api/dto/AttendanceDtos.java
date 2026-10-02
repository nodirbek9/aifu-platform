package uz.aifu.lms.attendance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.attendance.domain.AttendanceStatus;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ClassView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupRef;

/** Request and response shapes of class sessions, attendance and the journal. */
public final class AttendanceDtos {

    private AttendanceDtos() {}

    // ------------------------------------------------------------------ class session

    @Schema(description = "What the teacher records about one class: the lesson taught and notes.")
    public record SessionDetailsRequest(
            @Schema(description = "A lesson of the same course; null unlinks") UUID lessonId,
            @Schema(description = "Free-text topic when no lesson fits") @Size(max = 300)
                    String topic,
            @Schema(description = "Private notes; never shown to students") @Size(max = 4000)
                    String notes) {}

    public record MarkRequest(
            @NotNull UUID studentId,
            @Schema(description = "null removes the mark") AttendanceStatus status,
            @Size(max = 300) String note) {}

    @Schema(
            description =
                    "Marks for some or all students of the class. Students not listed keep their"
                            + " current mark.")
    public record AttendanceRequest(@NotEmpty List<@Valid @NotNull MarkRequest> marks) {}

    public record LessonRef(UUID id, String title, SessionType sessionType, boolean published) {}

    public record RosterRow(
            UUID userId,
            String fullName,
            String studentNumber,
            String groupCode,
            @Schema(description = "null = not marked yet") AttendanceStatus status,
            String note) {}

    @Schema(
            description =
                    "percent = (present + late) / all marked classes; null when nothing is marked.")
    public record Totals(
            int present, int late, int absent, int excused, int unmarked, Integer percent) {}

    @Schema(description = "Everything the teacher's class page shows (spec T-03).")
    public record ClassSessionView(
            @Schema(description = "null until the teacher saves something for this class")
                    UUID sessionId,
            ClassView classInfo,
            @Schema(description = "false for a cancelled class, a holiday or a moved-away slot")
                    boolean held,
            @Schema(description = "true when the class is held and its date is today or earlier")
                    boolean canMarkAttendance,
            LessonRef lesson,
            @Schema(
                            description =
                                    "The next lesson of this session type not yet taught to these"
                                            + " groups; null when a lesson is linked or none is left")
                    LessonRef suggestedLesson,
            String topic,
            String notes,
            Instant attendanceMarkedAt,
            List<RosterRow> students,
            Totals totals) {}

    @Schema(description = "What a student sees about a class: its topic and lesson (spec S-02.4).")
    public record ClassTopicView(
            ClassView classInfo,
            @Schema(description = "null when no lesson is linked or the lesson is not published")
                    UUID lessonId,
            String lessonTitle,
            String topic) {}

    // ------------------------------------------------------------------ calendar-thematic plan

    @Schema(description = "One class of the term with the topic planned or taught (spec T-04.3).")
    public record PlannedClassView(
            int weekNumber,
            ClassView classInfo,
            UUID sessionId,
            LessonRef lesson,
            String topic,
            boolean attendanceMarked) {}

    // ------------------------------------------------------------------ journal

    public record JournalSession(
            UUID sessionId,
            UUID entryId,
            LocalDate date,
            int pairNo,
            SessionType sessionType,
            String subgroup,
            List<GroupRef> groups,
            String lessonTitle,
            String topic) {}

    public record JournalRow(
            UUID userId,
            String fullName,
            String studentNumber,
            String groupCode,
            @Schema(description = "sessionId -> mark; a missing key means not marked")
                    Map<UUID, AttendanceStatus> marks,
            Totals totals) {}

    @Schema(description = "Students x class dates for one course (spec T-10).")
    public record JournalView(
            UUID courseId,
            String subjectCode,
            String courseTitle,
            UUID groupId,
            List<JournalSession> sessions,
            List<JournalRow> students) {}

    // ------------------------------------------------------------------ my attendance

    public record MyRecord(
            LocalDate date,
            int pairNo,
            SessionType sessionType,
            AttendanceStatus status,
            String lessonTitle,
            String topic) {}

    @Schema(description = "A student's own attendance in one course (spec S-10).")
    public record MyCourseAttendance(
            UUID courseId,
            String subjectCode,
            String courseTitle,
            Totals totals,
            List<MyRecord> records) {}
}
