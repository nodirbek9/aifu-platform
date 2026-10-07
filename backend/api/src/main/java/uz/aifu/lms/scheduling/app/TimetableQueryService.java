package uz.aifu.lms.scheduling.app;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.AcademicCalendarService;
import uz.aifu.lms.academics.app.CourseDirectory;
import uz.aifu.lms.academics.app.CourseDirectory.CourseLabel;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.iam.app.user.StudentDirectory;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ClassView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.DayView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.EntryView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ExceptionView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.GroupRef;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.LoadRow;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.PairSlotView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.TeacherRef;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.TimetableDirectory;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.TimetableView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.WarningView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.WeekView;
import uz.aifu.lms.scheduling.domain.ExceptionKind;
import uz.aifu.lms.scheduling.domain.Holiday;
import uz.aifu.lms.scheduling.domain.PairSlot;
import uz.aifu.lms.scheduling.domain.Room;
import uz.aifu.lms.scheduling.domain.StudentGroup;
import uz.aifu.lms.scheduling.domain.TeachingLoadItem;
import uz.aifu.lms.scheduling.domain.Timetable;
import uz.aifu.lms.scheduling.domain.TimetableEntry;
import uz.aifu.lms.scheduling.domain.TimetableException;
import uz.aifu.lms.scheduling.domain.TimetableStatus;
import uz.aifu.lms.scheduling.domain.WeekParity;
import uz.aifu.lms.scheduling.persistence.HolidayRepository;
import uz.aifu.lms.scheduling.persistence.PairSlotRepository;
import uz.aifu.lms.scheduling.persistence.StudentGroupRepository;
import uz.aifu.lms.scheduling.persistence.TeachingLoadItemRepository;
import uz.aifu.lms.scheduling.persistence.TimetableEntryRepository;
import uz.aifu.lms.scheduling.persistence.TimetableExceptionRepository;
import uz.aifu.lms.scheduling.persistence.TimetableRepository;

/**
 * Read side of the timetable: the builder grid, concrete weeks with one-off changes applied, and
 * the planned-vs-scheduled load report. Names of courses and people come from their owning modules
 * in one batch per request.
 */
@Service
public class TimetableQueryService {

    /** One "juftlik" counts as two academic hours in the university's load sheets. */
    public static final int HOURS_PER_PAIR = 2;

    public static final String SCHEDULED = "SCHEDULED";
    public static final String CANCELLED = "CANCELLED";
    public static final String MOVED_OUT = "MOVED_OUT";
    public static final String MOVED_IN = "MOVED_IN";
    public static final String REPLACED = "REPLACED";
    public static final String HOLIDAY = "HOLIDAY";

    /** Which classes a view is about. Exactly one of the ids is normally set; none = everything. */
    public record Filter(UUID groupId, UUID teacherUserId, UUID roomId) {
        public static Filter all() {
            return new Filter(null, null, null);
        }

        public static Filter group(UUID id) {
            return new Filter(id, null, null);
        }

        public static Filter teacher(UUID id) {
            return new Filter(null, id, null);
        }
    }

    /** A class on a concrete date, after exceptions and holidays. */
    public record Occurrence(
            TimetableEntry entry,
            LocalDate date,
            PairSlot slot,
            Room room,
            UUID teacherUserId,
            String status,
            String reason) {

        /** Whether the class actually takes place (counts as taught hours, blocks the slot). */
        public boolean held() {
            return SCHEDULED.equals(status) || MOVED_IN.equals(status) || REPLACED.equals(status);
        }
    }

    private final TimetableRepository timetables;
    private final TimetableEntryRepository entries;
    private final TimetableExceptionRepository exceptions;
    private final HolidayRepository holidays;
    private final PairSlotRepository pairSlots;
    private final StudentGroupRepository groups;
    private final TeachingLoadItemRepository loadItems;
    private final AcademicCalendarService calendar;
    private final CourseDirectory courses;
    private final StudentDirectory people;

    public TimetableQueryService(
            TimetableRepository timetables,
            TimetableEntryRepository entries,
            TimetableExceptionRepository exceptions,
            HolidayRepository holidays,
            PairSlotRepository pairSlots,
            StudentGroupRepository groups,
            TeachingLoadItemRepository loadItems,
            AcademicCalendarService calendar,
            CourseDirectory courses,
            StudentDirectory people) {
        this.timetables = timetables;
        this.entries = entries;
        this.exceptions = exceptions;
        this.holidays = holidays;
        this.pairSlots = pairSlots;
        this.groups = groups;
        this.loadItems = loadItems;
        this.calendar = calendar;
        this.courses = courses;
        this.people = people;
    }

    // ------------------------------------------------------------------ timetables

    @Transactional(readOnly = true)
    public Timetable requireTimetable(UUID id) {
        return timetables.findById(id).orElseThrow(() -> Exceptions.notFound("Timetable", id));
    }

    @Transactional(readOnly = true)
    public List<TimetableView> listTimetables(UUID termId) {
        List<Timetable> found =
                termId == null
                        ? timetables.findAllByOrderByCreatedAtDesc()
                        : timetables.findByTermIdOrderByCreatedAtDesc(termId);
        return found.stream().map(this::toTimetableView).toList();
    }

    @Transactional(readOnly = true)
    public TimetableView toTimetableView(Timetable timetable) {
        Term term = calendar.requireTerm(timetable.getTermId());
        return new TimetableView(
                timetable.getId(),
                term.getId(),
                term.getName(),
                term.getStartsOn(),
                term.getEndsOn(),
                term.getFirstWeekParity(),
                timetable.getName(),
                timetable.getStatus(),
                timetable.getPublishedAt(),
                entries.countByTimetable_Id(timetable.getId()));
    }

    // ------------------------------------------------------------------ builder grid

    @Transactional(readOnly = true)
    public List<EntryView> listEntries(UUID timetableId, Filter filter, LocaleCode locale) {
        requireTimetable(timetableId);
        List<TimetableEntry> found =
                entries.findAllOf(timetableId).stream().filter(matches(filter)).toList();
        return toEntryViews(found, locale, Map.of());
    }

    @Transactional(readOnly = true)
    public EntryView entryView(
            TimetableEntry entry, LocaleCode locale, List<WarningView> warnings) {
        return toEntryViews(List.of(entry), locale, Map.of(entry.getId(), warnings)).get(0);
    }

    List<EntryView> toEntryViews(
            List<TimetableEntry> found, LocaleCode locale, Map<UUID, List<WarningView>> warnings) {
        Map<UUID, CourseLabel> labels =
                courses.labels(
                        found.stream().map(TimetableEntry::getCourseId).collect(Collectors.toSet()),
                        locale);
        Map<UUID, String> teachers =
                people.fullNames(
                        found.stream()
                                .map(TimetableEntry::getTeacherUserId)
                                .collect(Collectors.toSet()));
        return found.stream()
                .sorted(
                        Comparator.comparingInt(TimetableEntry::getDayOfWeek)
                                .thenComparingInt(e -> e.getPairSlot().getPairNo()))
                .map(
                        e -> {
                            CourseLabel label = labels.get(e.getCourseId());
                            return new EntryView(
                                    e.getId(),
                                    e.getTimetable().getId(),
                                    e.getCourseId(),
                                    label == null ? null : label.subjectCode(),
                                    label == null ? null : label.title(),
                                    e.getSessionType(),
                                    e.getTeacherUserId(),
                                    teachers.get(e.getTeacherUserId()),
                                    e.getRoom() == null ? null : e.getRoom().getId(),
                                    e.getRoom() == null ? null : e.getRoom().label(),
                                    e.getDayOfWeek(),
                                    e.getPairSlot().getId(),
                                    e.getPairSlot().getPairNo(),
                                    e.getPairSlot().getStartsAt(),
                                    e.getPairSlot().getEndsAt(),
                                    e.getWeekParity(),
                                    e.getValidFrom(),
                                    e.getValidTo(),
                                    e.getSubgroup(),
                                    e.getNote(),
                                    groupRefs(e),
                                    warnings.getOrDefault(e.getId(), List.of()));
                        })
                .toList();
    }

    static List<GroupRef> groupRefs(TimetableEntry entry) {
        return entry.getGroups().stream()
                .sorted(Comparator.comparing(StudentGroup::getCode))
                .map(g -> new GroupRef(g.getId(), g.getCode()))
                .toList();
    }

    static Predicate<TimetableEntry> matches(Filter filter) {
        return e ->
                (filter.groupId() == null
                                || e.getGroups().stream()
                                        .anyMatch(g -> g.getId().equals(filter.groupId())))
                        && (filter.teacherUserId() == null
                                || filter.teacherUserId().equals(e.getTeacherUserId()))
                        && (filter.roomId() == null
                                || (e.getRoom() != null
                                        && filter.roomId().equals(e.getRoom().getId())));
    }

    // ------------------------------------------------------------------ concrete dates

    /**
     * Every class of the timetable between two dates (inclusive), with one-off changes and holidays
     * applied. Dates outside the term produce nothing.
     */
    @Transactional(readOnly = true)
    public List<Occurrence> occurrences(
            Timetable timetable, Term term, LocalDate from, LocalDate to) {
        LocalDate start = from.isBefore(term.getStartsOn()) ? term.getStartsOn() : from;
        LocalDate end = to.isAfter(term.getEndsOn()) ? term.getEndsOn() : to;
        if (end.isBefore(start)) {
            return List.of();
        }
        List<TimetableEntry> all = entries.findAllOf(timetable.getId());
        Map<UUID, TimetableEntry> byId =
                all.stream().collect(Collectors.toMap(TimetableEntry::getId, e -> e));
        List<TimetableException> touching = exceptions.findTouching(timetable.getId(), start, end);
        Map<String, TimetableException> onDate = new HashMap<>();
        for (TimetableException x : touching) {
            onDate.put(x.getEntry().getId() + "@" + x.getOnDate(), x);
        }
        Set<LocalDate> holidayDates =
                holidays.findByOnDateBetweenOrderByOnDate(start, end).stream()
                        .map(Holiday::getOnDate)
                        .collect(Collectors.toSet());
        WeekParity first = WeekParity.valueOf(term.getFirstWeekParity());

        List<Occurrence> result = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            int dayOfWeek = day.getDayOfWeek().getValue();
            WeekParity parity =
                    WeekCalendar.parityOf(first, WeekCalendar.weekNumber(term.getStartsOn(), day));
            for (TimetableEntry entry : all) {
                if (entry.getDayOfWeek() != dayOfWeek
                        || !entry.getWeekParity().includes(parity)
                        || !entry.isValidOn(day)) {
                    continue;
                }
                TimetableException x = onDate.get(entry.getId() + "@" + day);
                if (x == null) {
                    String status = holidayDates.contains(day) ? HOLIDAY : SCHEDULED;
                    result.add(
                            occurrence(entry, day, entry.getPairSlot(), null, null, status, null));
                } else if (x.getKind() == ExceptionKind.CANCELLED) {
                    result.add(
                            occurrence(
                                    entry,
                                    day,
                                    entry.getPairSlot(),
                                    null,
                                    null,
                                    CANCELLED,
                                    x.getReason()));
                } else if (x.getKind() == ExceptionKind.MOVED) {
                    result.add(
                            occurrence(
                                    entry,
                                    day,
                                    entry.getPairSlot(),
                                    null,
                                    null,
                                    MOVED_OUT,
                                    x.getReason()));
                } else {
                    String status = holidayDates.contains(day) ? HOLIDAY : REPLACED;
                    result.add(
                            occurrence(
                                    entry,
                                    day,
                                    entry.getPairSlot(),
                                    x.getNewRoom(),
                                    x.getNewTeacherUserId(),
                                    status,
                                    x.getReason()));
                }
            }
        }
        for (TimetableException x : touching) {
            if (x.getKind() == ExceptionKind.MOVED
                    && !x.getNewDate().isBefore(start)
                    && !x.getNewDate().isAfter(end)) {
                TimetableEntry entry = byId.get(x.getEntry().getId());
                if (entry != null) {
                    result.add(
                            occurrence(
                                    entry,
                                    x.getNewDate(),
                                    x.getNewPairSlot(),
                                    x.getNewRoom(),
                                    x.getNewTeacherUserId(),
                                    MOVED_IN,
                                    x.getReason()));
                }
            }
        }
        result.sort(
                Comparator.comparing(Occurrence::date).thenComparingInt(o -> o.slot().getPairNo()));
        return result;
    }

    /**
     * The class of a timetable entry on one date, as it is actually held: a moved class is found on
     * its new date. Empty when the entry has no class that day or its timetable was never published
     * (a draft is not something anyone attends).
     */
    @Transactional(readOnly = true)
    public Optional<Occurrence> occurrenceOf(UUID entryId, LocalDate date) {
        Optional<TimetableEntry> entry = entries.findDetailedById(entryId);
        if (entry.isEmpty() || entry.get().getTimetable().getPublishedAt() == null) {
            return Optional.empty();
        }
        Timetable timetable = entry.get().getTimetable();
        Term term = calendar.requireTerm(timetable.getTermId());
        return occurrences(timetable, term, date, date).stream()
                .filter(o -> o.entry().getId().equals(entryId))
                .min(Comparator.comparing((Occurrence o) -> !o.held()));
    }

    /** For each course, the groups it has classes with in its term's published timetable. */
    @Transactional(readOnly = true)
    public Map<UUID, Set<StudentGroup>> groupsOfCourses(java.util.Collection<UUID> courseIds) {
        Map<UUID, Set<StudentGroup>> found = new HashMap<>();
        Set<UUID> terms =
                courses.labels(courseIds, LocaleCode.DEFAULT).values().stream()
                        .map(CourseLabel::termId)
                        .collect(Collectors.toSet());
        for (UUID termId : terms) {
            timetables
                    .findByTermIdAndStatus(termId, TimetableStatus.PUBLISHED)
                    .ifPresent(
                            published -> {
                                for (TimetableEntry entry : entries.findAllOf(published.getId())) {
                                    if (courseIds.contains(entry.getCourseId())) {
                                        found.computeIfAbsent(
                                                        entry.getCourseId(), key -> new HashSet<>())
                                                .addAll(entry.getGroups());
                                    }
                                }
                            });
        }
        return found;
    }

    /**
     * Every class of a course over its term, from the published timetable; optionally only those of
     * one group. Empty when the term has no published timetable.
     */
    @Transactional(readOnly = true)
    public List<Occurrence> occurrencesOfCourse(UUID courseId, UUID groupId) {
        CourseLabel label = courses.labels(List.of(courseId), LocaleCode.DEFAULT).get(courseId);
        if (label == null) {
            return List.of();
        }
        Optional<Timetable> published =
                timetables.findByTermIdAndStatus(label.termId(), TimetableStatus.PUBLISHED);
        if (published.isEmpty()) {
            return List.of();
        }
        Term term = calendar.requireTerm(label.termId());
        return occurrences(published.get(), term, term.getStartsOn(), term.getEndsOn()).stream()
                .filter(o -> o.entry().getCourseId().equals(courseId))
                .filter(o -> occurrenceMatches(o, Filter.group(groupId)))
                .toList();
    }

    private static Occurrence occurrence(
            TimetableEntry entry,
            LocalDate date,
            PairSlot slot,
            Room newRoom,
            UUID newTeacher,
            String status,
            String reason) {
        return new Occurrence(
                entry,
                date,
                slot,
                newRoom != null ? newRoom : entry.getRoom(),
                newTeacher != null ? newTeacher : entry.getTeacherUserId(),
                status,
                reason);
    }

    // ------------------------------------------------------------------ week view

    /**
     * The week containing {@code date} from the published timetable. Outside any term, or before a
     * timetable is published, the week comes back empty rather than as an error: "no classes" is a
     * normal answer for a student. Should two terms overlap on the date, the one that has a
     * published timetable wins.
     */
    @Transactional(readOnly = true)
    public WeekView week(LocalDate date, Filter filter, LocaleCode locale) {
        List<Term> candidates = calendar.findTermsContaining(date);
        for (Term term : candidates) {
            Optional<Timetable> published =
                    timetables.findByTermIdAndStatus(term.getId(), TimetableStatus.PUBLISHED);
            if (published.isPresent()) {
                return week(date, filter, locale, term, published.get());
            }
        }
        return week(date, filter, locale, candidates.isEmpty() ? null : candidates.get(0), null);
    }

    /**
     * Whose week can be looked up: the groups and teachers with classes in the timetable published
     * for the term containing {@code date}. Empty outside a term or before publishing.
     */
    @Transactional(readOnly = true)
    public TimetableDirectory directory(LocalDate date) {
        for (Term term : calendar.findTermsContaining(date)) {
            Optional<Timetable> published =
                    timetables.findByTermIdAndStatus(term.getId(), TimetableStatus.PUBLISHED);
            if (published.isPresent()) {
                List<TimetableEntry> all = entries.findAllOf(published.get().getId());
                Map<UUID, String> groupCodes = new HashMap<>();
                Set<UUID> teacherIds = new HashSet<>();
                for (TimetableEntry entry : all) {
                    entry.getGroups().forEach(g -> groupCodes.put(g.getId(), g.getCode()));
                    teacherIds.add(entry.getTeacherUserId());
                }
                return new TimetableDirectory(
                        groupCodes.entrySet().stream()
                                .map(e -> new GroupRef(e.getKey(), e.getValue()))
                                .sorted(Comparator.comparing(GroupRef::code))
                                .toList(),
                        people.fullNames(teacherIds).entrySet().stream()
                                .map(e -> new TeacherRef(e.getKey(), e.getValue()))
                                .sorted(Comparator.comparing(TeacherRef::fullName))
                                .toList());
            }
        }
        return new TimetableDirectory(List.of(), List.of());
    }

    /** The same week view over any timetable (the admin previews a draft). */
    @Transactional(readOnly = true)
    public WeekView weekOf(UUID timetableId, LocalDate date, Filter filter, LocaleCode locale) {
        Timetable timetable = requireTimetable(timetableId);
        return week(date, filter, locale, calendar.requireTerm(timetable.getTermId()), timetable);
    }

    private WeekView week(
            LocalDate date, Filter filter, LocaleCode locale, Term term, Timetable timetable) {
        LocalDate monday = WeekCalendar.mondayOf(date);
        LocalDate sunday = monday.plusDays(6);
        List<PairSlotView> slots =
                pairSlots.findAllByOrderByPairNo().stream()
                        .map(SchedulingReferenceService::toSlotView)
                        .toList();
        Map<LocalDate, String> holidayNames =
                holidays.findByOnDateBetweenOrderByOnDate(monday, sunday).stream()
                        .collect(Collectors.toMap(Holiday::getOnDate, Holiday::getName));

        List<Occurrence> found =
                timetable == null
                        ? List.of()
                        : occurrences(timetable, term, monday, sunday).stream()
                                .filter(o -> occurrenceMatches(o, filter))
                                .toList();
        Map<LocalDate, List<ClassView>> byDate =
                toClassViews(found, locale).stream()
                        .collect(
                                Collectors.groupingBy(
                                        ClassView::date, LinkedHashMap::new, Collectors.toList()));

        List<DayView> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            days.add(
                    new DayView(
                            day,
                            day.getDayOfWeek().getValue(),
                            holidayNames.get(day),
                            byDate.getOrDefault(day, List.of())));
        }
        Integer weekNumber = null;
        WeekParity parity = null;
        if (term != null) {
            weekNumber = WeekCalendar.weekNumber(term.getStartsOn(), monday);
            parity =
                    WeekCalendar.parityOf(
                            WeekParity.valueOf(term.getFirstWeekParity()), weekNumber);
        }
        return new WeekView(
                monday,
                sunday,
                weekNumber,
                parity,
                term == null ? null : term.getId(),
                term == null ? null : term.getName(),
                timetable == null ? null : timetable.getId(),
                slots,
                days);
    }

    /**
     * A replaced teacher sees the class they took over; the original teacher sees it as replaced.
     */
    private static boolean occurrenceMatches(Occurrence o, Filter filter) {
        TimetableEntry e = o.entry();
        boolean group =
                filter.groupId() == null
                        || e.getGroups().stream().anyMatch(g -> g.getId().equals(filter.groupId()));
        boolean teacher =
                filter.teacherUserId() == null
                        || filter.teacherUserId().equals(o.teacherUserId())
                        || filter.teacherUserId().equals(e.getTeacherUserId());
        boolean room =
                filter.roomId() == null
                        || (o.room() != null && filter.roomId().equals(o.room().getId()))
                        || (e.getRoom() != null && filter.roomId().equals(e.getRoom().getId()));
        return group && teacher && room;
    }

    @Transactional(readOnly = true)
    public List<ClassView> toClassViews(List<Occurrence> found, LocaleCode locale) {
        Map<UUID, CourseLabel> labels =
                courses.labels(
                        found.stream()
                                .map(o -> o.entry().getCourseId())
                                .collect(Collectors.toSet()),
                        locale);
        Map<UUID, String> teachers =
                people.fullNames(
                        found.stream().map(Occurrence::teacherUserId).collect(Collectors.toSet()));
        return found.stream()
                .map(
                        o -> {
                            TimetableEntry e = o.entry();
                            CourseLabel label = labels.get(e.getCourseId());
                            return new ClassView(
                                    e.getId(),
                                    o.date(),
                                    o.slot().getPairNo(),
                                    o.slot().getStartsAt(),
                                    o.slot().getEndsAt(),
                                    e.getCourseId(),
                                    label == null ? null : label.subjectCode(),
                                    label == null ? null : label.title(),
                                    e.getSessionType(),
                                    o.teacherUserId(),
                                    teachers.get(o.teacherUserId()),
                                    o.room() == null ? null : o.room().getId(),
                                    o.room() == null ? null : o.room().label(),
                                    groupRefs(e),
                                    e.getSubgroup(),
                                    e.getWeekParity(),
                                    o.status(),
                                    e.getNote(),
                                    o.reason());
                        })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExceptionView> listExceptions(UUID entryId) {
        List<TimetableException> found = exceptions.findByEntry_IdOrderByOnDate(entryId);
        Map<UUID, String> teachers =
                people.fullNames(
                        found.stream()
                                .map(TimetableException::getNewTeacherUserId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet()));
        return found.stream().map(x -> toExceptionView(x, teachers)).toList();
    }

    ExceptionView toExceptionView(TimetableException x, Map<UUID, String> teachers) {
        return new ExceptionView(
                x.getId(),
                x.getEntry().getId(),
                x.getOnDate(),
                x.getKind(),
                x.getNewDate(),
                x.getNewPairSlot() == null ? null : x.getNewPairSlot().getPairNo(),
                x.getNewRoom() == null ? null : x.getNewRoom().getId(),
                x.getNewRoom() == null ? null : x.getNewRoom().label(),
                x.getNewTeacherUserId(),
                x.getNewTeacherUserId() == null ? null : teachers.get(x.getNewTeacherUserId()),
                x.getReason());
    }

    // ------------------------------------------------------------------ load: planned vs scheduled

    /**
     * For a timetable: every planned load line with the hours the timetable actually delivers over
     * the term, plus scheduled classes that have no load line (planned = null). Optionally limited
     * to one group or one teacher.
     */
    @Transactional(readOnly = true)
    public List<LoadRow> load(UUID timetableId, Filter filter, LocaleCode locale) {
        Timetable timetable = requireTimetable(timetableId);
        return loadRows(calendar.requireTerm(timetable.getTermId()), timetable, filter, locale);
    }

    /** The planned load of a term on its own (nothing scheduled yet: every hour is remaining). */
    @Transactional(readOnly = true)
    public List<LoadRow> plannedLoad(UUID termId, LocaleCode locale) {
        return loadRows(calendar.requireTerm(termId), null, Filter.all(), locale);
    }

    private List<LoadRow> loadRows(
            Term term, Timetable timetable, Filter filter, LocaleCode locale) {
        // (course, group, session type) -> subgroup ("" = whole group) -> pairs held in the term
        Map<String, Map<String, Integer>> pairs = new HashMap<>();
        Map<String, UUID> teacherOfKey = new HashMap<>();
        List<Occurrence> held =
                timetable == null
                        ? List.of()
                        : occurrences(timetable, term, term.getStartsOn(), term.getEndsOn());
        for (Occurrence o : held) {
            if (!o.held()) {
                continue;
            }
            TimetableEntry e = o.entry();
            for (StudentGroup g : e.getGroups()) {
                String key = key(e.getCourseId(), g.getId(), e.getSessionType());
                pairs.computeIfAbsent(key, k -> new HashMap<>())
                        .merge(e.getSubgroup() == null ? "" : e.getSubgroup(), 1, Integer::sum);
                teacherOfKey.putIfAbsent(key, e.getTeacherUserId());
            }
        }

        record Line(
                UUID itemId,
                UUID courseId,
                StudentGroup group,
                SessionType type,
                UUID teacher,
                Integer planned) {}
        List<Line> lines = new ArrayList<>();
        Set<String> planned = new HashSet<>();
        for (TeachingLoadItem item : loadItems.findByTermId(term.getId())) {
            planned.add(key(item.getCourseId(), item.getGroup().getId(), item.getSessionType()));
            lines.add(
                    new Line(
                            item.getId(),
                            item.getCourseId(),
                            item.getGroup(),
                            item.getSessionType(),
                            item.getTeacherUserId(),
                            item.getPlannedHours()));
        }
        Map<UUID, StudentGroup> groupsById = new HashMap<>();
        Set<UUID> unplannedGroupIds = new HashSet<>();
        for (String key : pairs.keySet()) {
            if (!planned.contains(key)) {
                unplannedGroupIds.add(UUID.fromString(key.split("\\|")[1]));
            }
        }
        groups.findByIdIn(unplannedGroupIds).forEach(g -> groupsById.put(g.getId(), g));
        for (String key : pairs.keySet()) {
            if (!planned.contains(key)) {
                String[] parts = key.split("\\|");
                StudentGroup group = groupsById.get(UUID.fromString(parts[1]));
                if (group != null) {
                    lines.add(
                            new Line(
                                    null,
                                    UUID.fromString(parts[0]),
                                    group,
                                    SessionType.valueOf(parts[2]),
                                    teacherOfKey.get(key),
                                    null));
                }
            }
        }

        List<Line> shown =
                lines.stream()
                        .filter(
                                l ->
                                        filter.groupId() == null
                                                || filter.groupId().equals(l.group().getId()))
                        .filter(
                                l ->
                                        filter.teacherUserId() == null
                                                || filter.teacherUserId().equals(l.teacher())
                                                || filter.teacherUserId()
                                                        .equals(
                                                                teacherOfKey.get(
                                                                        key(
                                                                                l.courseId(),
                                                                                l.group().getId(),
                                                                                l.type()))))
                        .toList();
        Map<UUID, CourseLabel> labels =
                courses.labels(
                        shown.stream().map(Line::courseId).collect(Collectors.toSet()), locale);
        Map<UUID, String> teachers =
                people.fullNames(
                        shown.stream()
                                .map(Line::teacher)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet()));
        return shown.stream()
                .map(
                        l -> {
                            int scheduled =
                                    scheduledHours(
                                            pairs.get(
                                                    key(
                                                            l.courseId(),
                                                            l.group().getId(),
                                                            l.type())));
                            CourseLabel label = labels.get(l.courseId());
                            return new LoadRow(
                                    l.itemId(),
                                    l.courseId(),
                                    label == null ? null : label.subjectCode(),
                                    label == null ? null : label.title(),
                                    l.group().getId(),
                                    l.group().getCode(),
                                    l.type(),
                                    l.teacher(),
                                    l.teacher() == null ? null : teachers.get(l.teacher()),
                                    l.planned(),
                                    scheduled,
                                    l.planned() == null ? null : l.planned() - scheduled);
                        })
                .sorted(
                        Comparator.comparing(LoadRow::groupCode)
                                .thenComparing(r -> r.subjectCode() == null ? "" : r.subjectCode())
                                .thenComparing(LoadRow::sessionType))
                .toList();
    }

    /**
     * A split class (subgroups A and B) gives each student the hours of ONE subgroup, so the
     * group's hours are the whole-group pairs plus the largest subgroup's pairs.
     */
    static int scheduledHours(Map<String, Integer> pairsBySubgroup) {
        if (pairsBySubgroup == null) {
            return 0;
        }
        int whole = pairsBySubgroup.getOrDefault("", 0);
        int split =
                pairsBySubgroup.entrySet().stream()
                        .filter(e -> !e.getKey().isEmpty())
                        .mapToInt(Map.Entry::getValue)
                        .max()
                        .orElse(0);
        return (whole + split) * HOURS_PER_PAIR;
    }

    private static String key(UUID courseId, UUID groupId, SessionType type) {
        return courseId + "|" + groupId + "|" + type;
    }
}
