package uz.aifu.lms.attendance.app;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.AcademicCalendarService;
import uz.aifu.lms.academics.app.CourseDirectory;
import uz.aifu.lms.academics.app.CourseDirectory.CourseLabel;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.AttendanceRequest;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.ClassSessionView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.ClassTopicView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.LessonRef;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.MarkRequest;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.PlannedClassView;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.RosterRow;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.SessionDetailsRequest;
import uz.aifu.lms.attendance.domain.AttendanceRecord;
import uz.aifu.lms.attendance.domain.ClassSession;
import uz.aifu.lms.attendance.persistence.AttendanceRecordRepository;
import uz.aifu.lms.attendance.persistence.ClassSessionRepository;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.security.AuthenticatedUser;
import uz.aifu.lms.curriculum.app.LessonDirectory;
import uz.aifu.lms.curriculum.app.LessonDirectory.LessonLabel;
import uz.aifu.lms.iam.app.access.AccessGuard;
import uz.aifu.lms.iam.app.user.StudentDirectory;
import uz.aifu.lms.iam.app.user.StudentDirectory.StudentRef;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ClassView;
import uz.aifu.lms.scheduling.app.TimetableQueryService;
import uz.aifu.lms.scheduling.app.TimetableQueryService.Occurrence;
import uz.aifu.lms.scheduling.app.WeekCalendar;
import uz.aifu.lms.scheduling.domain.StudentGroup;
import uz.aifu.lms.scheduling.domain.TimetableEntry;

/**
 * The page a teacher opens during a class (spec T-03): which lesson is taught, private notes, and
 * the attendance of the class's students. Also the calendar-thematic plan of a course (T-04.3),
 * which is the same data laid out over the whole term.
 *
 * <p>A class is addressed as (timetable entry, date). Nothing is stored until the teacher saves
 * something; reading a class that has no session yet returns the roster with no marks.
 */
@Service
public class ClassSessionService {

    private final ClassSessionRepository sessions;
    private final AttendanceRecordRepository records;
    private final TimetableQueryService timetable;
    private final StudentDirectory students;
    private final CourseDirectory courses;
    private final AcademicCalendarService calendar;
    private final LessonDirectory lessons;
    private final AccessGuard access;
    private final AuditService audit;
    private final Clock clock;

    public ClassSessionService(
            ClassSessionRepository sessions,
            AttendanceRecordRepository records,
            TimetableQueryService timetable,
            StudentDirectory students,
            CourseDirectory courses,
            AcademicCalendarService calendar,
            LessonDirectory lessons,
            AccessGuard access,
            AuditService audit,
            Clock clock) {
        this.sessions = sessions;
        this.records = records;
        this.timetable = timetable;
        this.students = students;
        this.courses = courses;
        this.calendar = calendar;
        this.lessons = lessons;
        this.access = access;
        this.audit = audit;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ the teacher's class page

    @Transactional(readOnly = true)
    public ClassSessionView view(UUID entryId, LocalDate date, LocaleCode locale) {
        Occurrence occurrence = requireOccurrence(entryId, date);
        requireTeacherOf(occurrence);
        return toView(occurrence, find(occurrence).orElse(null), locale);
    }

    @Transactional
    public ClassSessionView describe(
            UUID entryId, LocalDate date, SessionDetailsRequest request, LocaleCode locale) {
        Occurrence occurrence = requireOccurrence(entryId, date);
        requireTeacherOf(occurrence);
        if (request.lessonId() != null) {
            LessonLabel lesson =
                    lessons.labels(List.of(request.lessonId()), locale).get(request.lessonId());
            if (lesson == null || !lesson.courseId().equals(occurrence.entry().getCourseId())) {
                throw Exceptions.validation("The lesson does not belong to this class's course");
            }
        }
        ClassSession session = ensure(occurrence);
        session.describe(request.lessonId(), request.topic(), request.notes());
        return toView(occurrence, session, locale);
    }

    @Transactional
    public ClassSessionView markAttendance(
            UUID entryId, LocalDate date, AttendanceRequest request, LocaleCode locale, String ip) {
        Occurrence occurrence = requireOccurrence(entryId, date);
        AuthenticatedUser actor = requireTeacherOf(occurrence);
        if (!occurrence.held()) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION,
                    "This class is not held on this date, so there is no attendance to take");
        }
        if (occurrence.date().isAfter(WeekCalendar.today(clock))) {
            throw Exceptions.validation("Attendance cannot be marked before the class takes place");
        }
        Set<UUID> roster =
                roster(occurrence.entry()).values().stream()
                        .flatMap(List::stream)
                        .map(StudentRef::userId)
                        .collect(Collectors.toSet());
        List<String> strangers =
                request.marks().stream()
                        .map(MarkRequest::studentId)
                        .filter(id -> !roster.contains(id))
                        .map(UUID::toString)
                        .toList();
        if (!strangers.isEmpty()) {
            throw Exceptions.validation("Some students are not in this class's groups")
                    .with("studentIds", strangers);
        }

        ClassSession session = ensure(occurrence);
        Map<UUID, AttendanceRecord> existing =
                records.findBySessionId(session.getId()).stream()
                        .collect(Collectors.toMap(AttendanceRecord::getStudentUserId, r -> r));
        Instant now = clock.instant();
        for (MarkRequest mark : request.marks()) {
            AttendanceRecord record = existing.get(mark.studentId());
            if (mark.status() == null) {
                if (record != null) {
                    records.delete(record);
                }
                continue;
            }
            if (record == null) {
                record = new AttendanceRecord(session.getId(), mark.studentId());
                existing.put(mark.studentId(), record);
            }
            record.mark(mark.status(), mark.note(), actor.id(), now);
            records.save(record);
        }
        session.attendanceMarked(actor.id(), now);
        records.flush();
        audit.record(
                AuditAction.ATTENDANCE_MARKED,
                actor.id(),
                "ClassSession",
                session.getId(),
                ip,
                null,
                Map.of("date", occurrence.date().toString(), "marks", request.marks().size()));
        return toView(occurrence, session, locale);
    }

    // ------------------------------------------------------------------ what a student sees

    @Transactional(readOnly = true)
    public ClassTopicView topic(UUID entryId, LocalDate date, LocaleCode locale) {
        Occurrence occurrence = requireOccurrence(entryId, date);
        ClassView classInfo = timetable.toClassViews(List.of(occurrence), locale).get(0);
        ClassSession session = find(occurrence).orElse(null);
        if (session == null) {
            return new ClassTopicView(classInfo, null, null, null);
        }
        LessonLabel lesson =
                session.getLessonId() == null
                        ? null
                        : lessons.labels(List.of(session.getLessonId()), locale)
                                .get(session.getLessonId());
        return new ClassTopicView(
                classInfo,
                lesson != null && lesson.published() ? lesson.lessonId() : null,
                lesson == null ? null : lesson.title(),
                session.getTopic());
    }

    // ------------------------------------------------------------------ calendar-thematic plan

    /** Every class of a course over its term with the topic planned or already taught. */
    @Transactional(readOnly = true)
    public List<PlannedClassView> plan(UUID courseId, UUID groupId, LocaleCode locale) {
        access.requireCourseEditor(courseId);
        List<Occurrence> found = timetable.occurrencesOfCourse(courseId, groupId);
        if (found.isEmpty()) {
            return List.of();
        }
        CourseLabel course = courses.labels(List.of(courseId), locale).get(courseId);
        Term term = calendar.requireTerm(course.termId());
        Map<String, ClassSession> byKey = new HashMap<>();
        for (ClassSession session : sessions.findAllOfCourse(courseId)) {
            byKey.put(key(session), session);
        }
        Map<UUID, LessonLabel> lessonLabels =
                lessons.labels(
                        byKey.values().stream()
                                .map(ClassSession::getLessonId)
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toSet()),
                        locale);
        List<ClassView> classes = timetable.toClassViews(found, locale);
        List<PlannedClassView> plan = new ArrayList<>();
        for (int i = 0; i < found.size(); i++) {
            Occurrence occurrence = found.get(i);
            ClassSession session = byKey.get(key(occurrence));
            plan.add(
                    new PlannedClassView(
                            WeekCalendar.weekNumber(term.getStartsOn(), occurrence.date()),
                            classes.get(i),
                            session == null ? null : session.getId(),
                            session == null || session.getLessonId() == null
                                    ? null
                                    : toRef(lessonLabels.get(session.getLessonId())),
                            session == null ? null : session.getTopic(),
                            session != null && session.getAttendanceMarkedAt() != null));
        }
        return plan;
    }

    // ------------------------------------------------------------------ helpers

    private Occurrence requireOccurrence(UUID entryId, LocalDate date) {
        return timetable
                .occurrenceOf(entryId, date)
                .orElseThrow(() -> Exceptions.notFound("Class", entryId + "@" + date));
    }

    /**
     * The class's own teacher (or the one replacing them that day) may always work with it; so may
     * any teacher assigned to the course, and an admin.
     */
    private AuthenticatedUser requireTeacherOf(Occurrence occurrence) {
        AuthenticatedUser user = access.caller();
        if (!user.id().equals(occurrence.teacherUserId())
                && !user.id().equals(occurrence.entry().getTeacherUserId())) {
            access.requireCourseEditor(occurrence.entry().getCourseId());
        }
        return user;
    }

    private Optional<ClassSession> find(Occurrence occurrence) {
        TimetableEntry entry = occurrence.entry();
        return sessions.findByCourseIdAndOnDateAndPairNoAndGroupsKeyAndSubgroup(
                entry.getCourseId(),
                occurrence.date(),
                occurrence.slot().getPairNo(),
                ClassSession.groupsKey(groupIds(entry)),
                ClassSession.subgroupKey(entry.getSubgroup()));
    }

    /** The class's session, created now if this is the first thing saved for it. */
    private ClassSession ensure(Occurrence occurrence) {
        sessions.lockKey(key(occurrence));
        TimetableEntry entry = occurrence.entry();
        ClassSession session =
                find(occurrence)
                        .orElseGet(
                                () ->
                                        new ClassSession(
                                                entry.getCourseId(),
                                                occurrence.date(),
                                                occurrence.slot().getPairNo(),
                                                entry.getSessionType(),
                                                groupIds(entry),
                                                entry.getSubgroup()));
        session.pointTo(entry.getId(), occurrence.teacherUserId());
        return sessions.save(session);
    }

    private static List<UUID> groupIds(TimetableEntry entry) {
        return entry.getGroups().stream().map(StudentGroup::getId).toList();
    }

    private static String key(Occurrence occurrence) {
        TimetableEntry entry = occurrence.entry();
        return key(
                entry.getCourseId(),
                occurrence.date(),
                occurrence.slot().getPairNo(),
                ClassSession.groupsKey(groupIds(entry)),
                ClassSession.subgroupKey(entry.getSubgroup()));
    }

    private static String key(ClassSession session) {
        return key(
                session.getCourseId(),
                session.getOnDate(),
                session.getPairNo(),
                session.getGroupsKey(),
                session.getSubgroup());
    }

    private static String key(
            UUID courseId, LocalDate date, int pairNo, String groupsKey, String subgroup) {
        return courseId + "|" + date + "|" + pairNo + "|" + groupsKey + "|" + subgroup;
    }

    /** Group code -> its students by name, groups in code order. */
    private Map<String, List<StudentRef>> roster(TimetableEntry entry) {
        Map<String, List<StudentRef>> roster = new LinkedHashMap<>();
        entry.getGroups().stream()
                .map(StudentGroup::getCode)
                .sorted()
                .forEach(
                        code ->
                                roster.put(
                                        code,
                                        students.studentsOfGroup(code).stream()
                                                .sorted(Comparator.comparing(StudentRef::fullName))
                                                .toList()));
        return roster;
    }

    private ClassSessionView toView(
            Occurrence occurrence, ClassSession session, LocaleCode locale) {
        TimetableEntry entry = occurrence.entry();
        Map<UUID, AttendanceRecord> marks =
                session == null || session.getId() == null
                        ? Map.of()
                        : records.findBySessionId(session.getId()).stream()
                                .collect(
                                        Collectors.toMap(
                                                AttendanceRecord::getStudentUserId, r -> r));
        List<RosterRow> rows = new ArrayList<>();
        Set<UUID> listed = new java.util.HashSet<>();
        roster(entry)
                .forEach(
                        (code, members) -> {
                            for (StudentRef student : members) {
                                AttendanceRecord mark = marks.get(student.userId());
                                listed.add(student.userId());
                                rows.add(
                                        new RosterRow(
                                                student.userId(),
                                                student.fullName(),
                                                student.studentNumber(),
                                                code,
                                                mark == null ? null : mark.getStatus(),
                                                mark == null ? null : mark.getNote()));
                            }
                        });
        // A student marked earlier who has since left the group stays visible, with no group.
        List<UUID> departed = marks.keySet().stream().filter(id -> !listed.contains(id)).toList();
        Map<UUID, String> departedNames = students.fullNames(departed);
        for (UUID id : departed) {
            AttendanceRecord mark = marks.get(id);
            rows.add(
                    new RosterRow(
                            id,
                            departedNames.get(id),
                            null,
                            null,
                            mark.getStatus(),
                            mark.getNote()));
        }

        LessonLabel lesson =
                session == null || session.getLessonId() == null
                        ? null
                        : lessons.labels(List.of(session.getLessonId()), locale)
                                .get(session.getLessonId());
        boolean canMark =
                occurrence.held() && !occurrence.date().isAfter(WeekCalendar.today(clock));
        return new ClassSessionView(
                session == null ? null : session.getId(),
                timetable.toClassViews(List.of(occurrence), locale).get(0),
                occurrence.held(),
                canMark,
                toRef(lesson),
                lesson == null ? toRef(suggest(occurrence, locale)) : null,
                session == null ? null : session.getTopic(),
                session == null ? null : session.getNotes(),
                session == null ? null : session.getAttendanceMarkedAt(),
                rows,
                AttendanceMath.totals(
                        rows.stream().map(RosterRow::status).collect(Collectors.toList())));
    }

    /**
     * The next lesson of the class's session type that these groups have not been taught yet, in
     * the course's teaching order.
     */
    private LessonLabel suggest(Occurrence occurrence, LocaleCode locale) {
        TimetableEntry entry = occurrence.entry();
        String groupsKey = ClassSession.groupsKey(groupIds(entry));
        String subgroup = ClassSession.subgroupKey(entry.getSubgroup());
        Set<UUID> taught =
                sessions.findAllOfCourse(entry.getCourseId()).stream()
                        .filter(s -> s.getLessonId() != null)
                        .filter(s -> s.getGroupsKey().equals(groupsKey))
                        .filter(s -> s.getSubgroup().equals(subgroup))
                        .map(ClassSession::getLessonId)
                        .collect(Collectors.toSet());
        return lessons.lessonsOfCourse(entry.getCourseId(), locale).stream()
                .filter(l -> l.sessionType() == entry.getSessionType())
                .filter(l -> !taught.contains(l.lessonId()))
                .findFirst()
                .orElse(null);
    }

    static LessonRef toRef(LessonLabel label) {
        return label == null
                ? null
                : new LessonRef(
                        label.lessonId(), label.title(), label.sessionType(), label.published());
    }
}
