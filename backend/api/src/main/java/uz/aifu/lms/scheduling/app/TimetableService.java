package uz.aifu.lms.scheduling.app;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.AcademicCalendarService;
import uz.aifu.lms.academics.app.CourseDirectory;
import uz.aifu.lms.academics.app.CourseDirectory.CourseLabel;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.academics.domain.Term;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.error.DomainException;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.events.NotificationRequestedEvent;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.iam.app.user.StudentDirectory;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.CheckResult;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ConflictView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.CreateTimetableRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.EntryRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.EntryView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ExceptionRequest;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ExceptionView;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.WarningView;
import uz.aifu.lms.scheduling.app.TimetableConflictDetector.Conflict;
import uz.aifu.lms.scheduling.app.TimetableConflictDetector.PairTime;
import uz.aifu.lms.scheduling.app.TimetableConflictDetector.Slot;
import uz.aifu.lms.scheduling.app.TimetableQueryService.Occurrence;
import uz.aifu.lms.scheduling.domain.ExceptionKind;
import uz.aifu.lms.scheduling.domain.PairSlot;
import uz.aifu.lms.scheduling.domain.Room;
import uz.aifu.lms.scheduling.domain.RoomType;
import uz.aifu.lms.scheduling.domain.StudentGroup;
import uz.aifu.lms.scheduling.domain.Timetable;
import uz.aifu.lms.scheduling.domain.TimetableEntry;
import uz.aifu.lms.scheduling.domain.TimetableException;
import uz.aifu.lms.scheduling.domain.TimetableStatus;
import uz.aifu.lms.scheduling.domain.WeekParity;
import uz.aifu.lms.scheduling.persistence.HolidayRepository;
import uz.aifu.lms.scheduling.persistence.PairSlotRepository;
import uz.aifu.lms.scheduling.persistence.RoomRepository;
import uz.aifu.lms.scheduling.persistence.StudentGroupRepository;
import uz.aifu.lms.scheduling.persistence.TimetableEntryRepository;
import uz.aifu.lms.scheduling.persistence.TimetableExceptionRepository;
import uz.aifu.lms.scheduling.persistence.TimetableRepository;

/**
 * Building the timetable: its lifecycle (draft → published → archived), classes with conflict
 * checks, and one-off changes on a date.
 *
 * <p>Every write takes a row lock on the timetable first ({@link TimetableRepository#lockById}), so
 * "check for clashes, then insert" cannot interleave between two admins.
 */
@Service
public class TimetableService {

    private final TimetableRepository timetables;
    private final TimetableEntryRepository entries;
    private final TimetableExceptionRepository exceptions;
    private final StudentGroupRepository groups;
    private final RoomRepository rooms;
    private final PairSlotRepository pairSlots;
    private final TimetableQueryService queries;
    private final AcademicCalendarService calendar;
    private final HolidayRepository holidays;
    private final CourseDirectory courses;
    private final StudentDirectory people;
    private final AuditService audit;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public TimetableService(
            TimetableRepository timetables,
            TimetableEntryRepository entries,
            TimetableExceptionRepository exceptions,
            StudentGroupRepository groups,
            RoomRepository rooms,
            PairSlotRepository pairSlots,
            TimetableQueryService queries,
            AcademicCalendarService calendar,
            HolidayRepository holidays,
            CourseDirectory courses,
            StudentDirectory people,
            AuditService audit,
            ApplicationEventPublisher events,
            Clock clock) {
        this.timetables = timetables;
        this.entries = entries;
        this.exceptions = exceptions;
        this.groups = groups;
        this.rooms = rooms;
        this.pairSlots = pairSlots;
        this.queries = queries;
        this.calendar = calendar;
        this.holidays = holidays;
        this.courses = courses;
        this.people = people;
        this.audit = audit;
        this.events = events;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ lifecycle

    @Transactional
    public Timetable create(CreateTimetableRequest request) {
        calendar.requireTerm(request.termId());
        Timetable created = timetables.save(new Timetable(request.termId(), request.name()));
        if (request.copyFromId() != null) {
            queries.requireTimetable(request.copyFromId());
            for (TimetableEntry source : entries.findAllOf(request.copyFromId())) {
                TimetableEntry copy = new TimetableEntry(created);
                copy.define(
                        source.getCourseId(),
                        source.getSessionType(),
                        source.getTeacherUserId(),
                        source.getRoom(),
                        source.getDayOfWeek(),
                        source.getPairSlot(),
                        source.getWeekParity(),
                        null, // date limits belonged to the source term
                        null,
                        source.getSubgroup(),
                        source.getNote(),
                        new LinkedHashSet<>(source.getGroups()));
                entries.save(copy);
            }
        }
        return created;
    }

    @Transactional
    public Timetable rename(UUID id, String name) {
        Timetable timetable = lock(id);
        timetable.rename(name);
        return timetable;
    }

    /** Publishes this timetable; the one published before it for the same term is archived. */
    @Transactional
    public Timetable publish(UUID id, UUID actorId, String ip) {
        Timetable timetable = lock(id);
        if (timetable.getStatus() == TimetableStatus.PUBLISHED) {
            return timetable;
        }
        timetables
                .findByTermIdAndStatus(timetable.getTermId(), TimetableStatus.PUBLISHED)
                .ifPresent(
                        previous -> {
                            previous.archive();
                            timetables.flush(); // free uq_timetable_published before the new row
                            // takes it
                        });
        timetable.publish(actorId, Instant.now(clock));
        audit.record(AuditAction.TIMETABLE_PUBLISHED, actorId, "Timetable", id, ip, null);
        return timetable;
    }

    @Transactional
    public Timetable archive(UUID id) {
        Timetable timetable = lock(id);
        timetable.archive();
        return timetable;
    }

    /** Only a draft can be deleted; a published or archived timetable is history. */
    @Transactional
    public void delete(UUID id) {
        Timetable timetable = lock(id);
        if (timetable.getStatus() != TimetableStatus.DRAFT) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION, "Only a draft timetable can be deleted");
        }
        timetables.delete(timetable);
    }

    private Timetable lock(UUID id) {
        return timetables.lockById(id).orElseThrow(() -> Exceptions.notFound("Timetable", id));
    }

    // ------------------------------------------------------------------ classes

    /** Dry run for the builder: what would clash, and what deserves a warning. */
    @Transactional(readOnly = true)
    public CheckResult check(
            UUID timetableId, EntryRequest request, UUID editedEntryId, LocaleCode locale) {
        Timetable timetable = queries.requireTimetable(timetableId);
        Resolved resolved = resolve(request);
        List<TimetableEntry> all = entries.findAllOf(timetableId);
        return new CheckResult(
                conflictViews(detect(request, resolved, editedEntryId, all), all, locale),
                warnings(timetable, request, resolved));
    }

    @Transactional
    public EntryView createEntry(
            UUID timetableId, EntryRequest request, UUID actorId, String ip, LocaleCode locale) {
        Timetable timetable = lock(timetableId);
        timetable.requireEditable();
        Resolved resolved = resolve(request);
        List<WarningView> warnings = guard(timetable, request, resolved, null, actorId, ip, locale);
        TimetableEntry entry = new TimetableEntry(timetable);
        define(entry, request, resolved);
        TimetableEntry saved = entries.save(entry);
        audit.record(
                AuditAction.TIMETABLE_ENTRY_CHANGED,
                actorId,
                "TimetableEntry",
                saved.getId(),
                ip,
                null,
                Map.of("change", "created"));
        return queries.entryView(saved, locale, warnings);
    }

    /** Edit or move (drag and drop sends the same body with a new day / pair). */
    @Transactional
    public EntryView updateEntry(
            UUID entryId, EntryRequest request, UUID actorId, String ip, LocaleCode locale) {
        TimetableEntry entry = requireEntry(entryId);
        Timetable timetable = lock(entry.getTimetable().getId());
        timetable.requireEditable();
        Resolved resolved = resolve(request);
        List<WarningView> warnings =
                guard(timetable, request, resolved, entryId, actorId, ip, locale);
        define(entry, request, resolved);
        audit.record(
                AuditAction.TIMETABLE_ENTRY_CHANGED,
                actorId,
                "TimetableEntry",
                entryId,
                ip,
                null,
                Map.of("change", "updated"));
        return queries.entryView(entry, locale, warnings);
    }

    @Transactional
    public void deleteEntry(UUID entryId, UUID actorId, String ip) {
        TimetableEntry entry = requireEntry(entryId);
        lock(entry.getTimetable().getId()).requireEditable();
        entries.delete(entry);
        audit.record(
                AuditAction.TIMETABLE_ENTRY_CHANGED,
                actorId,
                "TimetableEntry",
                entryId,
                ip,
                null,
                Map.of("change", "deleted"));
    }

    @Transactional(readOnly = true)
    public TimetableEntry requireEntry(UUID entryId) {
        return entries.findDetailedById(entryId)
                .orElseThrow(() -> Exceptions.notFound("TimetableEntry", entryId));
    }

    /** Refuses a clash; refuses warnings unless forced with a reason (then audits the override). */
    private List<WarningView> guard(
            Timetable timetable,
            EntryRequest request,
            Resolved resolved,
            UUID editedEntryId,
            UUID actorId,
            String ip,
            LocaleCode locale) {
        List<TimetableEntry> all = entries.findAllOf(timetable.getId());
        List<Conflict> conflicts = detect(request, resolved, editedEntryId, all);
        List<WarningView> warnings = warnings(timetable, request, resolved);
        boolean forced = Boolean.TRUE.equals(request.force());
        if (!conflicts.isEmpty() || (!warnings.isEmpty() && !forced)) {
            throw conflict(
                    conflicts.isEmpty()
                            ? "The class has warnings; send force=true with a reason to place it anyway"
                            : "The teacher, room or group is already busy in this slot",
                    conflictViews(conflicts, all, locale),
                    warnings);
        }
        if (!warnings.isEmpty()) {
            if (request.forceReason() == null || request.forceReason().isBlank()) {
                throw Exceptions.validation("forceReason is required when force is true");
            }
            audit.record(
                    AuditAction.TIMETABLE_CONFLICT_OVERRIDDEN,
                    actorId,
                    "Timetable",
                    timetable.getId(),
                    ip,
                    null,
                    Map.of(
                            "warnings",
                            warnings.stream()
                                    .map(WarningView::code)
                                    .collect(Collectors.joining(",")),
                            "reason",
                            request.forceReason()));
        }
        return warnings;
    }

    private static DomainException conflict(
            String message, List<ConflictView> conflicts, List<WarningView> warnings) {
        return new DomainException(ErrorCode.TIMETABLE_CONFLICT, message)
                .with("conflicts", conflicts)
                .with("warnings", warnings);
    }

    private List<Conflict> detect(
            EntryRequest request, Resolved resolved, UUID editedEntryId, List<TimetableEntry> all) {
        Slot candidate =
                new Slot(
                        editedEntryId,
                        request.teacherUserId(),
                        request.roomId(),
                        resolved.groups().stream()
                                .map(StudentGroup::getId)
                                .collect(Collectors.toSet()),
                        blankToNull(request.subgroup()),
                        request.dayOfWeek(),
                        pairTime(resolved.slot()),
                        request.weekParity() == null ? WeekParity.EVERY : request.weekParity(),
                        request.validFrom(),
                        request.validTo());
        return TimetableConflictDetector.detect(
                candidate, all.stream().map(TimetableService::slotOf).toList());
    }

    private static Slot slotOf(TimetableEntry e) {
        return new Slot(
                e.getId(),
                e.getTeacherUserId(),
                e.getRoom() == null ? null : e.getRoom().getId(),
                e.getGroups().stream().map(StudentGroup::getId).collect(Collectors.toSet()),
                e.getSubgroup(),
                e.getDayOfWeek(),
                pairTime(e.getPairSlot()),
                e.getWeekParity(),
                e.getValidFrom(),
                e.getValidTo());
    }

    private static PairTime pairTime(PairSlot slot) {
        return new PairTime(slot.getStartsAt(), slot.getEndsAt());
    }

    private List<ConflictView> conflictViews(
            List<Conflict> conflicts, List<TimetableEntry> all, LocaleCode locale) {
        if (conflicts.isEmpty()) {
            return List.of();
        }
        Map<UUID, EntryView> views =
                queries
                        .toEntryViews(
                                all.stream()
                                        .filter(
                                                e ->
                                                        conflicts.stream()
                                                                .anyMatch(
                                                                        c ->
                                                                                e.getId()
                                                                                        .equals(
                                                                                                c
                                                                                                        .withEntryId())))
                                        .toList(),
                                locale,
                                Map.of())
                        .stream()
                        .collect(Collectors.toMap(EntryView::id, v -> v));
        return conflicts.stream()
                .map(
                        c -> {
                            EntryView v = views.get(c.withEntryId());
                            return new ConflictView(
                                    c.kind().name(),
                                    v.id(),
                                    v.subjectCode(),
                                    v.courseTitle(),
                                    v.teacherName(),
                                    v.roomLabel(),
                                    v.groups().stream().map(g -> g.code()).toList(),
                                    v.dayOfWeek(),
                                    v.pairNo(),
                                    v.weekParity());
                        })
                .toList();
    }

    private List<WarningView> warnings(
            Timetable timetable, EntryRequest request, Resolved resolved) {
        List<WarningView> warnings = new ArrayList<>();
        Room room = resolved.room();
        if (room != null) {
            if (!room.isActive()) {
                warnings.add(
                        new WarningView(
                                "ROOM_INACTIVE", "Room " + room.label() + " is marked inactive"));
            }
            Map<String, Long> sizes =
                    people.groupSizes(
                            resolved.groups().stream().map(StudentGroup::getCode).toList());
            long students = 0;
            for (StudentGroup g : resolved.groups()) {
                long actual = sizes.getOrDefault(g.getCode(), 0L);
                students +=
                        actual > 0 ? actual : (g.getPlannedSize() == null ? 0 : g.getPlannedSize());
            }
            if (blankToNull(request.subgroup()) != null) {
                students = (students + 1) / 2; // a subgroup is roughly half of its group
            }
            if (students > room.getCapacity()) {
                warnings.add(
                        new WarningView(
                                "ROOM_CAPACITY",
                                "Room "
                                        + room.label()
                                        + " seats "
                                        + room.getCapacity()
                                        + ", the class has "
                                        + students));
            }
            if (request.sessionType() == SessionType.LABORATORY
                    && room.getRoomType() != RoomType.COMPUTER_LAB
                    && room.getRoomType() != RoomType.LABORATORY) {
                warnings.add(
                        new WarningView(
                                "ROOM_TYPE",
                                "A laboratory class is placed in a room that is not a lab"));
            }
        }
        if (!courses.isTeacherOfCourse(request.courseId(), request.teacherUserId())) {
            warnings.add(
                    new WarningView(
                            "TEACHER_NOT_ASSIGNED", "This teacher is not assigned to the course"));
        }
        PairSlot pair = resolved.slot();
        for (StudentGroup g : resolved.groups()) {
            if (g.getShift() != pair.getShift()) {
                warnings.add(
                        new WarningView(
                                "SHIFT_MISMATCH",
                                "Group "
                                        + g.getCode()
                                        + " studies in the "
                                        + g.getShift().name().toLowerCase(java.util.Locale.ROOT)
                                        + " shift; this pair belongs to the "
                                        + pair.getShift().name().toLowerCase(java.util.Locale.ROOT)
                                        + " shift"));
            }
        }
        if (!timetable.getTermId().equals(resolved.course().termId())) {
            warnings.add(
                    new WarningView(
                            "COURSE_OTHER_TERM",
                            "The course belongs to another term than the timetable"));
        }
        return warnings;
    }

    private record Resolved(
            CourseLabel course, Room room, PairSlot slot, Set<StudentGroup> groups) {}

    private Resolved resolve(EntryRequest request) {
        CourseLabel course =
                courses.labels(List.of(request.courseId()), LocaleCode.DEFAULT)
                        .get(request.courseId());
        if (course == null) {
            throw Exceptions.notFound("Course", request.courseId());
        }
        if (!people.userExists(request.teacherUserId())) {
            throw Exceptions.notFound("User", request.teacherUserId());
        }
        if (!people.isTeacher(request.teacherUserId())) {
            throw Exceptions.validation("teacherUserId must be a user with the TEACHER role");
        }
        Room room =
                request.roomId() == null
                        ? null
                        : rooms.findById(request.roomId())
                                .orElseThrow(() -> Exceptions.notFound("Room", request.roomId()));
        PairSlot slot =
                pairSlots
                        .findById(request.pairSlotId())
                        .orElseThrow(() -> Exceptions.notFound("PairSlot", request.pairSlotId()));
        Set<UUID> ids = new HashSet<>(request.groupIds());
        Set<StudentGroup> found = new LinkedHashSet<>(groups.findByIdIn(ids));
        if (found.size() != ids.size()) {
            Set<UUID> known = found.stream().map(StudentGroup::getId).collect(Collectors.toSet());
            UUID missing = ids.stream().filter(id -> !known.contains(id)).findFirst().orElseThrow();
            throw Exceptions.notFound("StudentGroup", missing);
        }
        return new Resolved(course, room, slot, found);
    }

    private static void define(TimetableEntry entry, EntryRequest request, Resolved resolved) {
        entry.define(
                request.courseId(),
                request.sessionType(),
                request.teacherUserId(),
                resolved.room(),
                request.dayOfWeek(),
                resolved.slot(),
                request.weekParity(),
                request.validFrom(),
                request.validTo(),
                request.subgroup(),
                request.note(),
                resolved.groups());
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    // ------------------------------------------------------------------ one-off changes

    /**
     * Cancels, moves or replaces one occurrence of a class. The date must be a day the class is
     * really held on. A move or replacement is refused if the new slot, room or teacher is already
     * taken on that date; the teacher(s) and the students of the groups are notified.
     */
    @Transactional
    public ExceptionView addException(
            UUID entryId, ExceptionRequest request, UUID actorId, String ip, LocaleCode locale) {
        TimetableEntry entry = requireEntry(entryId);
        Timetable timetable = lock(entry.getTimetable().getId());
        timetable.requireEditable();
        Term term = calendar.requireTerm(timetable.getTermId());
        LocalDate date = request.onDate();

        List<TimetableQueryService.Occurrence> dailyOccurrences =
                queries.occurrences(timetable, term, date, date);
        if (request.kind() == ExceptionKind.ADD) {
            if (date.isBefore(term.getStartsOn()) || date.isAfter(term.getEndsOn())) {
                throw Exceptions.validation("The date is outside the term");
            }
            if (entry.getValidFrom() != null && date.isBefore(entry.getValidFrom())) {
                throw Exceptions.validation("The date is outside its date range");
            }
            if (entry.getValidTo() != null && date.isAfter(entry.getValidTo())) {
                throw Exceptions.validation("The date is outside its date range");
            }
            if (holidays.existsByOnDate(date)) {
                throw Exceptions.validation("The date is a holiday");
            }
            if (exceptions.existsByEntry_IdAndOnDate(entryId, date)) {
                throw Exceptions.validation(
                        "An exception already exists for this entry on this date");
            }
            boolean exists =
                    dailyOccurrences.stream()
                            .anyMatch(
                                    o ->
                                            o.entry().getId().equals(entryId)
                                                    && o.date().equals(date));
            if (exists) {
                throw Exceptions.validation(
                        "An occurrence of this class already exists on this date");
            }
            if (request.newPairSlotId() == null) {
                throw Exceptions.validation("newPairSlotId is required for ADD exception");
            }
        } else {
            boolean heldThatDay =
                    dailyOccurrences.stream()
                            .anyMatch(
                                    o ->
                                            o.entry().getId().equals(entryId)
                                                    && o.date().equals(date)
                                                    && TimetableQueryService.SCHEDULED.equals(
                                                            o.status()));
            if (!heldThatDay) {
                throw Exceptions.validation(
                        "The class is not held on this date (wrong weekday or week, a holiday, outside its"
                                + " date range, or already changed)");
            }
        }

        PairSlot newSlot =
                request.newPairSlotId() == null
                        ? null
                        : pairSlots
                                .findById(request.newPairSlotId())
                                .orElseThrow(
                                        () ->
                                                Exceptions.notFound(
                                                        "PairSlot", request.newPairSlotId()));
        Room newRoom =
                request.newRoomId() == null
                        ? null
                        : rooms.findById(request.newRoomId())
                                .orElseThrow(
                                        () -> Exceptions.notFound("Room", request.newRoomId()));
        if (request.newTeacherUserId() != null && !people.isTeacher(request.newTeacherUserId())) {
            throw Exceptions.validation("newTeacherUserId must be a user with the TEACHER role");
        }
        TimetableException exception =
                new TimetableException(
                        entry,
                        date,
                        request.kind(),
                        request.newDate(),
                        newSlot,
                        newRoom,
                        request.newTeacherUserId(),
                        request.reason(),
                        actorId);
        if (request.kind() != ExceptionKind.CANCELLED) {
            refuseIfBusy(timetable, term, entry, exception, locale);
        }
        TimetableException saved = exceptions.save(exception);
        audit.record(
                AuditAction.TIMETABLE_EXCEPTION_ADDED,
                actorId,
                "TimetableEntry",
                entryId,
                ip,
                null,
                Map.of("kind", request.kind().name(), "date", date.toString()));
        notifyChange(entry, saved);
        Map<UUID, String> names =
                saved.getNewTeacherUserId() == null
                        ? Map.of()
                        : people.fullNames(List.of(saved.getNewTeacherUserId()));
        return queries.toExceptionView(saved, names);
    }

    @Transactional
    public void removeException(UUID exceptionId) {
        TimetableException exception =
                exceptions
                        .findById(exceptionId)
                        .orElseThrow(() -> Exceptions.notFound("TimetableException", exceptionId));
        lock(exception.getEntry().getTimetable().getId()).requireEditable();
        exceptions.delete(exception);
    }

    /** The one-off slot must be free of other held classes for the teacher, room and groups. */
    private void refuseIfBusy(
            Timetable timetable,
            Term term,
            TimetableEntry entry,
            TimetableException x,
            LocaleCode locale) {
        LocalDate date = x.getKind() == ExceptionKind.MOVED ? x.getNewDate() : x.getOnDate();
        PairSlot slot =
                (x.getKind() == ExceptionKind.MOVED || x.getKind() == ExceptionKind.ADD)
                        ? x.getNewPairSlot()
                        : entry.getPairSlot();
        UUID teacher =
                x.getNewTeacherUserId() != null
                        ? x.getNewTeacherUserId()
                        : entry.getTeacherUserId();
        Room room = x.getNewRoom() != null ? x.getNewRoom() : entry.getRoom();
        Set<UUID> groupIds =
                entry.getGroups().stream().map(StudentGroup::getId).collect(Collectors.toSet());

        List<TimetableEntry> clashing = new ArrayList<>();
        List<Conflict> conflicts = new ArrayList<>();
        for (Occurrence o : queries.occurrences(timetable, term, date, date)) {
            if (!o.held()
                    || o.entry().getId().equals(entry.getId())
                    || !o.slot().getId().equals(slot.getId())) {
                continue;
            }
            boolean sameTeacher = teacher.equals(o.teacherUserId());
            boolean sameRoom =
                    room != null && o.room() != null && room.getId().equals(o.room().getId());
            boolean sameGroup =
                    o.entry().getGroups().stream().anyMatch(g -> groupIds.contains(g.getId()))
                            && !(entry.getSubgroup() != null
                                    && o.entry().getSubgroup() != null
                                    && !Objects.equals(
                                            entry.getSubgroup(), o.entry().getSubgroup()));
            if (sameTeacher) {
                conflicts.add(
                        new Conflict(TimetableConflictDetector.Kind.TEACHER, o.entry().getId()));
            }
            if (sameRoom) {
                conflicts.add(new Conflict(TimetableConflictDetector.Kind.ROOM, o.entry().getId()));
            }
            if (sameGroup) {
                conflicts.add(
                        new Conflict(TimetableConflictDetector.Kind.GROUP, o.entry().getId()));
            }
            if (sameTeacher || sameRoom || sameGroup) {
                clashing.add(o.entry());
            }
        }
        if (!conflicts.isEmpty()) {
            throw conflict(
                    "The teacher, room or group is already busy on that date and pair",
                    conflictViews(conflicts, clashing, locale),
                    List.of());
        }
    }

    private void notifyChange(TimetableEntry entry, TimetableException x) {
        Set<UUID> recipients = new LinkedHashSet<>();
        recipients.add(entry.getTeacherUserId());
        if (x.getNewTeacherUserId() != null) {
            recipients.add(x.getNewTeacherUserId());
        }
        for (StudentGroup group : entry.getGroups()) {
            people.studentsOfGroup(group.getCode()).forEach(s -> recipients.add(s.userId()));
        }
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("entryId", entry.getId().toString());
        payload.put("courseId", entry.getCourseId().toString());
        payload.put("kind", x.getKind().name());
        payload.put("date", x.getOnDate().toString());
        payload.put(
                "pairNo",
                x.getKind() == ExceptionKind.ADD
                        ? x.getNewPairSlot().getPairNo()
                        : entry.getPairSlot().getPairNo());
        if (x.getNewDate() != null) {
            payload.put("newDate", x.getNewDate().toString());
            payload.put("newPairNo", x.getNewPairSlot().getPairNo());
        }
        if (x.getNewRoom() != null) {
            payload.put("newRoom", x.getNewRoom().label());
        }
        if (x.getReason() != null) {
            payload.put("reason", x.getReason());
        }
        Instant now = Instant.now(clock);
        for (UUID recipient : recipients) {
            events.publishEvent(
                    new NotificationRequestedEvent(
                            recipient,
                            "TIMETABLE_CHANGED",
                            payload,
                            "timetable-change:" + x.getId(),
                            now));
        }
    }
}
