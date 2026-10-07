package uz.aifu.lms.scheduling.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.scheduling.domain.ExceptionKind;
import uz.aifu.lms.scheduling.domain.GroupStatus;
import uz.aifu.lms.scheduling.domain.RoomType;
import uz.aifu.lms.scheduling.domain.Shift;
import uz.aifu.lms.scheduling.domain.TimetableStatus;
import uz.aifu.lms.scheduling.domain.WeekParity;

public final class SchedulingDtos {

    private SchedulingDtos() {}

    // ------------------------------------------------------------------ groups

    @Schema(description = "Create or update a student group (\"guruh\").")
    public record GroupRequest(
            @Schema(example = "BK-DI26/1") @NotBlank @Size(max = 40) String code,
            UUID programmeId,
            @Min(1) @Max(6) Integer courseYear,
            @Min(1990) @Max(2200) Integer admissionYear,
            @Schema(allowableValues = {"FULL_TIME", "PART_TIME", "EVENING", "DISTANCE"})
                    String studyForm,
            @Schema(example = "uz") String instructionLanguage,
            @Min(0) Integer plannedSize,
            UUID curatorUserId,
            @Schema(description = "The shift the group studies in; MORNING when left out")
                    Shift shift) {}

    public record GroupView(
            UUID id,
            String code,
            UUID programmeId,
            Integer courseYear,
            Integer admissionYear,
            String studyForm,
            String instructionLanguage,
            Integer plannedSize,
            long studentCount,
            UUID curatorUserId,
            String curatorName,
            GroupStatus status,
            Shift shift) {}

    public record GroupRef(UUID id, String code) {}

    public record GroupStudentsRequest(@NotEmpty List<UUID> userIds) {}

    public record GroupStudentView(
            UUID userId, String fullName, String username, String studentNumber) {}

    // ------------------------------------------------------------------ rooms, bell schedule,
    // holidays

    public record RoomRequest(
            @Size(max = 40) String building,
            @Schema(example = "204") @NotBlank @Size(max = 40) String number,
            @Size(max = 120) String name,
            @Min(1) int capacity,
            @NotNull RoomType roomType,
            Boolean active) {}

    public record RoomView(
            UUID id,
            String building,
            String number,
            String name,
            String label,
            int capacity,
            RoomType roomType,
            boolean active) {}

    @Schema(
            description =
                    "A pair of the bell schedule: its shift, its number in the shift, its time.")
    public record PairSlotRequest(
            @NotNull Shift shift,
            @Min(1) @Max(6) int shiftPairNo,
            @Schema(example = "09:00") @NotNull LocalTime startsAt,
            @Schema(example = "10:20") @NotNull LocalTime endsAt) {}

    @Schema(
            description =
                    "pairNo is the pair's place in the whole day (a stable reference);"
                            + " shiftPairNo is the number people use (\"1-para\" of its shift).")
    public record PairSlotView(
            UUID id,
            int pairNo,
            Shift shift,
            int shiftPairNo,
            LocalTime startsAt,
            LocalTime endsAt) {}

    public record HolidayRequest(@NotNull LocalDate date, @NotBlank @Size(max = 120) String name) {}

    public record HolidayView(UUID id, LocalDate date, String name) {}

    public record TermParityRequest(
            @Schema(allowableValues = {"ODD", "EVEN"}) @NotBlank String firstWeekParity) {}

    // ------------------------------------------------------------------ timetables

    public record CreateTimetableRequest(
            @NotNull UUID termId,
            @Schema(example = "2026/2027, 1-semestr") @NotBlank @Size(max = 120) String name,
            @Schema(description = "Copy every class of this timetable into the new draft")
                    UUID copyFromId) {}

    public record TimetableView(
            UUID id,
            UUID termId,
            String termName,
            LocalDate termStartsOn,
            LocalDate termEndsOn,
            String firstWeekParity,
            String name,
            TimetableStatus status,
            Instant publishedAt,
            long entryCount) {}

    @Schema(
            description =
                    "One recurring class. The same body creates, edits and moves it. A hard conflict"
                            + " (teacher, room or group already busy) is refused with 409"
                            + " TIMETABLE_CONFLICT; warnings need force=true and a reason.")
    public record EntryRequest(
            @NotNull UUID courseId,
            @NotNull SessionType sessionType,
            @NotNull UUID teacherUserId,
            UUID roomId,
            @Schema(description = "ISO day: 1 = Monday … 7 = Sunday") @Min(1) @Max(7) int dayOfWeek,
            @NotNull UUID pairSlotId,
            WeekParity weekParity,
            LocalDate validFrom,
            LocalDate validTo,
            @Size(max = 10) String subgroup,
            @Size(max = 300) String note,
            @NotEmpty List<UUID> groupIds,
            Boolean force,
            @Size(max = 300) String forceReason) {}

    public record EntryView(
            UUID id,
            UUID timetableId,
            UUID courseId,
            String subjectCode,
            String courseTitle,
            SessionType sessionType,
            UUID teacherUserId,
            String teacherName,
            UUID roomId,
            String roomLabel,
            int dayOfWeek,
            UUID pairSlotId,
            int pairNo,
            LocalTime startsAt,
            LocalTime endsAt,
            WeekParity weekParity,
            LocalDate validFrom,
            LocalDate validTo,
            String subgroup,
            String note,
            List<GroupRef> groups,
            List<WarningView> warnings) {}

    @Schema(
            description =
                    "Why a class cannot be placed: who or what is already busy, and with what.")
    public record ConflictView(
            @Schema(allowableValues = {"TEACHER", "ROOM", "GROUP"}) String type,
            UUID entryId,
            String subjectCode,
            String courseTitle,
            String teacherName,
            String roomLabel,
            List<String> groupCodes,
            int dayOfWeek,
            int pairNo,
            WeekParity weekParity) {}

    public record WarningView(
            @Schema(
                            allowableValues = {
                                "ROOM_CAPACITY",
                                "ROOM_TYPE",
                                "ROOM_INACTIVE",
                                "TEACHER_NOT_ASSIGNED",
                                "COURSE_OTHER_TERM",
                                "SHIFT_MISMATCH"
                            })
                    String code,
            String message) {}

    public record CheckResult(List<ConflictView> conflicts, List<WarningView> warnings) {}

    public record ExceptionRequest(
            @NotNull LocalDate onDate,
            @NotNull ExceptionKind kind,
            LocalDate newDate,
            UUID newPairSlotId,
            UUID newRoomId,
            UUID newTeacherUserId,
            @Size(max = 300) String reason) {}

    public record ExceptionView(
            UUID id,
            UUID entryId,
            LocalDate onDate,
            ExceptionKind kind,
            LocalDate newDate,
            Integer newPairNo,
            UUID newRoomId,
            String newRoomLabel,
            UUID newTeacherUserId,
            String newTeacherName,
            String reason) {}

    // ------------------------------------------------------------------ week view (students,
    // teachers)

    @Schema(
            description =
                    "A concrete week of the published timetable, with one-off changes applied.")
    public record WeekView(
            LocalDate weekStart,
            LocalDate weekEnd,
            Integer weekNumber,
            WeekParity parity,
            UUID termId,
            String termName,
            UUID timetableId,
            List<PairSlotView> slots,
            List<DayView> days) {}

    public record TeacherRef(UUID id, String fullName) {}

    @Schema(description = "The groups and teachers that have classes in the published timetable.")
    public record TimetableDirectory(List<GroupRef> groups, List<TeacherRef> teachers) {}

    public record DayView(LocalDate date, int dayOfWeek, String holiday, List<ClassView> classes) {}

    public record ClassView(
            UUID entryId,
            LocalDate date,
            int pairNo,
            LocalTime startsAt,
            LocalTime endsAt,
            UUID courseId,
            String subjectCode,
            String courseTitle,
            SessionType sessionType,
            UUID teacherUserId,
            String teacherName,
            UUID roomId,
            String roomLabel,
            List<GroupRef> groups,
            String subgroup,
            WeekParity weekParity,
            @Schema(
                            description =
                                    "MOVED_OUT marks the original slot of a moved class, MOVED_IN its new"
                                            + " slot. SCHEDULED, MOVED_IN and REPLACED are actually held.",
                            allowableValues = {
                                "SCHEDULED",
                                "CANCELLED",
                                "MOVED_OUT",
                                "MOVED_IN",
                                "REPLACED",
                                "HOLIDAY"
                            })
                    String status,
            String note,
            String changeReason) {}

    // ------------------------------------------------------------------ teaching load

    public record LoadItemRequest(
            @NotNull UUID courseId,
            @NotNull UUID groupId,
            @NotNull SessionType sessionType,
            UUID teacherUserId,
            @Min(0) int plannedHours) {}

    public record LoadBulkRequest(@NotEmpty List<@NotNull LoadItemRequest> items) {}

    @Schema(description = "Planned hours (\"yuklama\") against the hours the timetable schedules.")
    public record LoadRow(
            UUID itemId,
            UUID courseId,
            String subjectCode,
            String courseTitle,
            UUID groupId,
            String groupCode,
            SessionType sessionType,
            UUID teacherUserId,
            String teacherName,
            Integer plannedHours,
            int scheduledHours,
            Integer remainingHours) {}
}
