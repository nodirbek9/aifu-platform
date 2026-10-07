package uz.aifu.lms.attendance.app;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.JournalCourseRef;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.JournalGroupChoice;
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
import uz.aifu.lms.scheduling.app.TimetableQueryService;
import uz.aifu.lms.scheduling.app.TimetableQueryService.Occurrence;
import uz.aifu.lms.scheduling.domain.StudentGroup;
import uz.aifu.lms.scheduling.domain.TimetableEntry;

/**
 * Read side of attendance: the teacher's journal of a course (students x class dates, spec T-10)
 * and a student's own record (S-10).
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
    private final TimetableQueryService timetable;
    private final Clock clock;

    public AttendanceJournalService(
            ClassSessionRepository sessions,
            AttendanceRecordRepository records,
            SchedulingReferenceService groups,
            StudentDirectory students,
            CourseDirectory courses,
            LessonDirectory lessons,
            AccessGuard access,
            TimetableQueryService timetable,
            Clock clock) {
        this.sessions = sessions;
        this.records = records;
        this.groups = groups;
        this.students = students;
        this.courses = courses;
        this.lessons = lessons;
        this.access = access;
        this.timetable = timetable;
        this.clock = clock;
    }

    /**
     * The journal of a course for one group (or every group of the course) and a period: every
     * student of the group by surname, and every class held in the period by the published
     * timetable, with the marks where attendance was taken. A class whose attendance was taken but
     * that the timetable no longer has stays in the journal. Without an end date the period ends
     * today.
     */
    @Transactional(readOnly = true)
    public JournalView journal(
            UUID courseId, UUID groupId, LocalDate from, LocalDate to, LocaleCode locale) {
        access.requireCourseEditor(courseId);
        LocalDate until = to != null ? to : LocalDate.now(clock);
        List<ClassSession> all = sessions.findAllOfCourse(courseId);
        Map<String, ClassSession> byKey = new HashMap<>();
        for (ClassSession session : all) {
            byKey.putIfAbsent(ClassSessionService.key(session), session);
        }

        List<Column> columns = new ArrayList<>();
        Set<UUID> covered = new HashSet<>();
        for (Occurrence occurrence : timetable.occurrencesOfCourse(courseId, groupId)) {
            if (!HELD.contains(occurrence.status()) || outside(occurrence.date(), from, until)) {
                continue;
            }
            ClassSession session = byKey.get(ClassSessionService.key(occurrence));
            if (session != null) {
                covered.add(session.getId());
            }
            columns.add(Column.of(occurrence, session));
        }
        for (ClassSession session : all) {
            if (session.getAttendanceMarkedAt() == null
                    || covered.contains(session.getId())
                    || (groupId != null && !session.getGroupIds().contains(groupId))
                    || outside(session.getOnDate(), from, until)) {
                continue;
            }
            columns.add(Column.of(session));
        }
        columns.sort(Comparator.comparing(Column::date).thenComparingInt(Column::pairNo));
        List<ClassSession> taken =
                columns.stream()
                        .map(Column::session)
                        .filter(s -> s != null && s.getAttendanceMarkedAt() != null)
                        .toList();

        // Every group of the classes shown is named in the header; the rows are the students of
        // the group asked for, or of all of them.
        Map<UUID, String> allCodes = new HashMap<>();
        if (groupId != null) {
            allCodes.put(groupId, groups.requireGroup(groupId).getCode());
        }
        for (Column column : columns) {
            for (UUID id : column.groupIds()) {
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
        Set<UUID> listed = new HashSet<>();
        for (String code : rosterCodes) {
            students.studentsOfGroup(code).stream()
                    .sorted(Comparator.comparing(StudentRef::fullName, BY_NAME))
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
                            });
        }
        // Someone marked earlier who has since left stays visible. With a group filter, students of
        // a shared class's other groups are not such leavers: they belong to another journal.
        Set<UUID> elsewhere = new HashSet<>();
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
        departed.stream()
                .sorted(Comparator.comparing(id -> departedNames.getOrDefault(id, ""), BY_NAME))
                .forEach(
                        id ->
                                rows.add(
                                        row(
                                                id,
                                                departedNames.get(id),
                                                null,
                                                null,
                                                taken,
                                                marksByStudent)));

        Map<UUID, LessonLabel> lessonLabels =
                lessons.labels(
                        columns.stream()
                                .map(Column::lessonId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet()),
                        locale);
        CourseLabel course = courses.labels(List.of(courseId), locale).get(courseId);
        return new JournalView(
                courseId,
                course == null ? null : course.subjectCode(),
                course == null ? null : course.title(),
                groupId,
                columns.stream()
                        .map(
                                c ->
                                        new JournalSession(
                                                c.session() == null ? null : c.session().getId(),
                                                c.entryId(),
                                                c.date(),
                                                c.pairNo(),
                                                c.sessionType(),
                                                c.subgroup(),
                                                c.groupIds().stream()
                                                        .map(
                                                                id ->
                                                                        new GroupRef(
                                                                                id,
                                                                                allCodes.get(id)))
                                                        .sorted(
                                                                Comparator.comparing(
                                                                        GroupRef::code))
                                                        .toList(),
                                                c.lessonId() == null
                                                        ? null
                                                        : labelTitle(lessonLabels, c.lessonId()),
                                                c.session() == null ? null : c.session().getTopic(),
                                                c.session() != null
                                                        && c.session().getAttendanceMarkedAt()
                                                                != null))
                        .toList(),
                rows);
    }

    /**
     * The groups the caller teaches, each with the caller's courses that have classes with it in
     * the published timetable, groups and courses in name order: what the journal is opened by.
     */
    @Transactional(readOnly = true)
    public List<JournalGroupChoice> choices(LocaleCode locale) {
        Set<UUID> mine = courses.courseIdsOfTeacher(access.caller().id());
        if (mine.isEmpty()) {
            return List.of();
        }
        Map<UUID, CourseLabel> labels = courses.labels(mine, locale);
        Map<UUID, StudentGroup> byId = new HashMap<>();
        Map<UUID, List<JournalCourseRef>> coursesByGroup = new HashMap<>();
        timetable
                .groupsOfCourses(mine)
                .forEach(
                        (courseId, found) -> {
                            CourseLabel label = labels.get(courseId);
                            for (StudentGroup group : found) {
                                byId.putIfAbsent(group.getId(), group);
                                coursesByGroup
                                        .computeIfAbsent(group.getId(), key -> new ArrayList<>())
                                        .add(
                                                new JournalCourseRef(
                                                        courseId,
                                                        label == null ? null : label.subjectCode(),
                                                        label == null ? null : label.title()));
                            }
                        });
        return byId.values().stream()
                .sorted(Comparator.comparing(StudentGroup::getCode))
                .map(
                        g ->
                                new JournalGroupChoice(
                                        g.getId(),
                                        g.getCode(),
                                        coursesByGroup.get(g.getId()).stream()
                                                .sorted(
                                                        Comparator.comparing(
                                                                c ->
                                                                        c.title() == null
                                                                                ? ""
                                                                                : c.title()))
                                                .toList()))
                .toList();
    }

    /** Held: not cancelled, not on a holiday, not the old place of a moved class. */
    private static final Set<String> HELD =
            Set.of(
                    TimetableQueryService.SCHEDULED,
                    TimetableQueryService.REPLACED,
                    TimetableQueryService.MOVED_IN);

    /** Surname first, as the names are written; letters compared the Uzbek way, case aside. */
    private static final Comparator<String> BY_NAME =
            Comparator.nullsLast(
                    java.text.Collator.getInstance(java.util.Locale.forLanguageTag("uz"))::compare);

    private static boolean outside(LocalDate date, LocalDate from, LocalDate until) {
        return (from != null && date.isBefore(from)) || date.isAfter(until);
    }

    /**
     * One column of the journal: a class held in the period, with its session once there is one.
     */
    private record Column(
            ClassSession session,
            UUID entryId,
            LocalDate date,
            int pairNo,
            SessionType sessionType,
            String subgroup,
            Set<UUID> groupIds,
            UUID lessonId) {

        static Column of(Occurrence occurrence, ClassSession session) {
            TimetableEntry entry = occurrence.entry();
            return new Column(
                    session,
                    entry.getId(),
                    occurrence.date(),
                    occurrence.slot().getPairNo(),
                    entry.getSessionType(),
                    entry.getSubgroup() == null || entry.getSubgroup().isBlank()
                            ? null
                            : entry.getSubgroup(),
                    entry.getGroups().stream().map(StudentGroup::getId).collect(Collectors.toSet()),
                    session == null ? null : session.getLessonId());
        }

        static Column of(ClassSession session) {
            return new Column(
                    session,
                    session.getEntryId(),
                    session.getOnDate(),
                    session.getPairNo(),
                    session.getSessionType(),
                    session.getSubgroup().isEmpty() ? null : session.getSubgroup(),
                    session.getGroupIds(),
                    session.getLessonId());
        }
    }

    private static String labelTitle(Map<UUID, LessonLabel> labels, UUID lessonId) {
        LessonLabel label = labels.get(lessonId);
        return label == null ? null : label.title();
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
