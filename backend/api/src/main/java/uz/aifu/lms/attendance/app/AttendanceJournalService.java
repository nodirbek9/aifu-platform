package uz.aifu.lms.attendance.app;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.CourseDirectory;
import uz.aifu.lms.academics.app.CourseDirectory.CourseLabel;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.JournalRow;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.JournalSession;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.JournalView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.MyCourseAttendance;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.MyRecord;
import uz.aifu.lms.attendance.domain.AttendanceRecord;
import uz.aifu.lms.attendance.domain.AttendanceStatus;
import uz.aifu.lms.attendance.domain.ClassSession;
import uz.aifu.lms.attendance.persistence.AttendanceRecordRepository;
import uz.aifu.lms.attendance.persistence.ClassSessionRepository;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.curriculum.app.LessonDirectory;
import uz.aifu.lms.curriculum.app.LessonDirectory.LessonLabel;
import uz.aifu.lms.iam.app.access.AccessGuard;
import uz.aifu.lms.iam.app.user.StudentDirectory;
import uz.aifu.lms.iam.app.user.StudentDirectory.StudentRef;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupRef;
import uz.aifu.lms.scheduling.app.SchedulingReferenceService;

/**
 * Read side of attendance: the teacher's journal of a course (students x class dates, spec T-10)
 * and a student's own record (S-10). Only classes whose attendance was actually taken appear.
 */
@Service
public class AttendanceJournalService {

    private final ClassSessionRepository sessions;
    private final AttendanceRecordRepository records;
    private final SchedulingReferenceService groups;
    private final StudentDirectory students;
    private final CourseDirectory courses;
    private final LessonDirectory lessons;
    private final AccessGuard access;

    public AttendanceJournalService(
            ClassSessionRepository sessions,
            AttendanceRecordRepository records,
            SchedulingReferenceService groups,
            StudentDirectory students,
            CourseDirectory courses,
            LessonDirectory lessons,
            AccessGuard access) {
        this.sessions = sessions;
        this.records = records;
        this.groups = groups;
        this.students = students;
        this.courses = courses;
        this.lessons = lessons;
        this.access = access;
    }

    /**
     * The journal of a course, optionally of one group and a date range. Rows are the current
     * students of the groups involved, plus anyone marked earlier who has since left.
     */
    @Transactional(readOnly = true)
    public JournalView journal(
            UUID courseId, UUID groupId, LocalDate from, LocalDate to, LocaleCode locale) {
        access.requireCourseEditor(courseId);
        List<ClassSession> taken =
                sessions.findAllOfCourse(courseId).stream()
                        .filter(s -> s.getAttendanceMarkedAt() != null)
                        .filter(s -> groupId == null || s.getGroupIds().contains(groupId))
                        .filter(s -> from == null || !s.getOnDate().isBefore(from))
                        .filter(s -> to == null || !s.getOnDate().isAfter(to))
                        .toList();

        // Every group of the classes shown is named in the header; the rows are the students of
        // the group asked for, or of all of them.
        Map<UUID, String> allCodes = new HashMap<>();
        if (groupId != null) {
            allCodes.put(groupId, groups.requireGroup(groupId).getCode());
        }
        for (ClassSession session : taken) {
            for (UUID id : session.getGroupIds()) {
                allCodes.computeIfAbsent(id, key -> groups.requireGroup(key).getCode());
            }
        }
        List<String> rosterCodes =
                groupId != null
                        ? List.of(allCodes.get(groupId))
                        : allCodes.values().stream().sorted().toList();

        Map<UUID, Map<UUID, AttendanceStatus>> marksByStudent = new HashMap<>();
        if (!taken.isEmpty()) {
            for (AttendanceRecord record :
                    records.findBySessionIdIn(taken.stream().map(ClassSession::getId).toList())) {
                marksByStudent
                        .computeIfAbsent(record.getStudentUserId(), key -> new LinkedHashMap<>())
                        .put(record.getSessionId(), record.getStatus());
            }
        }

        List<JournalRow> rows = new ArrayList<>();
        Set<UUID> listed = new java.util.HashSet<>();
        rosterCodes.forEach(
                code ->
                        students.studentsOfGroup(code).stream()
                                .sorted(Comparator.comparing(StudentRef::fullName))
                                .forEach(
                                        student -> {
                                            if (listed.add(student.userId())) {
                                                rows.add(
                                                        row(
                                                                student.userId(),
                                                                student.fullName(),
                                                                student.studentNumber(),
                                                                code,
                                                                taken,
                                                                marksByStudent));
                                            }
                                        }));
        // Someone marked earlier who has since left stays visible. With a group filter, students of
        // a shared class's other groups are not such leavers: they belong to another journal.
        Set<UUID> elsewhere = new java.util.HashSet<>();
        if (groupId != null) {
            allCodes.forEach(
                    (id, code) -> {
                        if (!id.equals(groupId)) {
                            students.studentsOfGroup(code).forEach(s -> elsewhere.add(s.userId()));
                        }
                    });
        }
        List<UUID> departed =
                marksByStudent.keySet().stream()
                        .filter(id -> !listed.contains(id) && !elsewhere.contains(id))
                        .toList();
        Map<UUID, String> departedNames = students.fullNames(departed);
        for (UUID id : departed) {
            rows.add(row(id, departedNames.get(id), null, null, taken, marksByStudent));
        }

        Map<UUID, LessonLabel> lessonLabels = lessonLabels(taken, locale);
        CourseLabel course = courses.labels(List.of(courseId), locale).get(courseId);
        return new JournalView(
                courseId,
                course == null ? null : course.subjectCode(),
                course == null ? null : course.title(),
                groupId,
                taken.stream()
                        .map(
                                s ->
                                        new JournalSession(
                                                s.getId(),
                                                s.getEntryId(),
                                                s.getOnDate(),
                                                s.getPairNo(),
                                                s.getSessionType(),
                                                s.getSubgroup().isEmpty() ? null : s.getSubgroup(),
                                                s.getGroupIds().stream()
                                                        .map(
                                                                id ->
                                                                        new GroupRef(
                                                                                id,
                                                                                allCodes.get(id)))
                                                        .sorted(
                                                                Comparator.comparing(
                                                                        GroupRef::code))
                                                        .toList(),
                                                title(lessonLabels, s),
                                                s.getTopic()))
                        .toList(),
                rows);
    }

    private static JournalRow row(
            UUID userId,
            String fullName,
            String studentNumber,
            String groupCode,
            List<ClassSession> taken,
            Map<UUID, Map<UUID, AttendanceStatus>> marksByStudent) {
        Map<UUID, AttendanceStatus> marks = marksByStudent.getOrDefault(userId, Map.of());
        // Not marked in a class does not count against a student: they may be in the other half
        // of a split group, or have joined later.
        return new JournalRow(
                userId,
                fullName,
                studentNumber,
                groupCode,
                marks,
                AttendanceMath.totals(
                        taken.stream()
                                .map(s -> marks.get(s.getId()))
                                .collect(Collectors.toList())));
    }

    /** A student's own attendance, one block per course, newest class first. */
    @Transactional(readOnly = true)
    public List<MyCourseAttendance> mine(UUID studentUserId, UUID courseId, LocaleCode locale) {
        List<AttendanceRecord> mine = records.findByStudentUserId(studentUserId);
        if (mine.isEmpty()) {
            return List.of();
        }
        Map<UUID, AttendanceStatus> statusBySession =
                mine.stream()
                        .collect(
                                Collectors.toMap(
                                        AttendanceRecord::getSessionId,
                                        AttendanceRecord::getStatus));
        List<ClassSession> attended =
                sessions.findByIdIn(statusBySession.keySet()).stream()
                        .filter(s -> courseId == null || s.getCourseId().equals(courseId))
                        .sorted(
                                Comparator.comparing(ClassSession::getOnDate)
                                        .thenComparingInt(ClassSession::getPairNo)
                                        .reversed())
                        .toList();
        Map<UUID, LessonLabel> lessonLabels = lessonLabels(attended, locale);
        Map<UUID, List<ClassSession>> byCourse =
                attended.stream()
                        .collect(
                                Collectors.groupingBy(
                                        ClassSession::getCourseId,
                                        LinkedHashMap::new,
                                        Collectors.toList()));
        Map<UUID, CourseLabel> courseLabels = courses.labels(byCourse.keySet(), locale);
        return byCourse.entrySet().stream()
                .map(
                        e -> {
                            CourseLabel course = courseLabels.get(e.getKey());
                            return new MyCourseAttendance(
                                    e.getKey(),
                                    course == null ? null : course.subjectCode(),
                                    course == null ? null : course.title(),
                                    AttendanceMath.totals(
                                            e.getValue().stream()
                                                    .map(s -> statusBySession.get(s.getId()))
                                                    .collect(Collectors.toList())),
                                    e.getValue().stream()
                                            .map(
                                                    s ->
                                                            new MyRecord(
                                                                    s.getOnDate(),
                                                                    s.getPairNo(),
                                                                    s.getSessionType(),
                                                                    statusBySession.get(s.getId()),
                                                                    title(lessonLabels, s),
                                                                    s.getTopic()))
                                            .toList());
                        })
                .sorted(Comparator.comparing(c -> c.courseTitle() == null ? "" : c.courseTitle()))
                .toList();
    }

    private Map<UUID, LessonLabel> lessonLabels(List<ClassSession> found, LocaleCode locale) {
        return lessons.labels(
                found.stream()
                        .map(ClassSession::getLessonId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()),
                locale);
    }

    private static String title(Map<UUID, LessonLabel> labels, ClassSession session) {
        if (session.getLessonId() == null) {
            return null;
        }
        LessonLabel label = labels.get(session.getLessonId());
        return label == null ? null : label.title();
    }
}
