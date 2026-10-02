package uz.aifu.lms.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.common.domain.AuditableEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * One recurring class: a course's session for one or more groups, with a teacher, in a room, on a
 * weekday and pair, every week or only odd / even weeks, optionally limited to a date range.
 */
@Entity
@Table(name = "timetable_entries")
public class TimetableEntry extends AuditableEntity {

    private static final Set<SessionType> SCHEDULABLE =
            Set.of(
                    SessionType.LECTURE,
                    SessionType.SEMINAR,
                    SessionType.PRACTICAL,
                    SessionType.LABORATORY);

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "timetable_id", nullable = false, updatable = false)
    private Timetable timetable;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Column(name = "teacher_user_id", nullable = false)
    private UUID teacherUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pair_slot_id", nullable = false)
    private PairSlot pairSlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "week_parity", nullable = false)
    private WeekParity weekParity = WeekParity.EVERY;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "subgroup")
    private String subgroup;

    @Column(name = "note")
    private String note;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "timetable_entry_groups",
            joinColumns = @JoinColumn(name = "entry_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<StudentGroup> groups = new LinkedHashSet<>();

    protected TimetableEntry() {}

    public TimetableEntry(Timetable timetable) {
        this.timetable = Guards.required(timetable, "timetable");
    }

    /** Sets every field of the class at once; the same call serves create, edit and move. */
    public void define(
            UUID courseId,
            SessionType sessionType,
            UUID teacherUserId,
            Room room,
            int dayOfWeek,
            PairSlot pairSlot,
            WeekParity weekParity,
            LocalDate validFrom,
            LocalDate validTo,
            String subgroup,
            String note,
            Set<StudentGroup> groups) {
        if (!SCHEDULABLE.contains(Guards.required(sessionType, "sessionType"))) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED,
                    "Only lecture, seminar, practical and laboratory classes are scheduled");
        }
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "dayOfWeek must be 1 (Monday) to 7");
        }
        if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "validTo must not be before validFrom");
        }
        if (groups == null || groups.isEmpty()) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "A class needs at least one group");
        }
        this.courseId = Guards.required(courseId, "courseId");
        this.sessionType = sessionType;
        this.teacherUserId = Guards.required(teacherUserId, "teacherUserId");
        this.room = room;
        this.dayOfWeek = dayOfWeek;
        this.pairSlot = Guards.required(pairSlot, "pairSlot");
        this.weekParity = weekParity == null ? WeekParity.EVERY : weekParity;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.subgroup = (subgroup == null || subgroup.isBlank()) ? null : subgroup.trim();
        this.note = (note == null || note.isBlank()) ? null : note.trim();
        this.groups.clear();
        this.groups.addAll(groups);
    }

    /** Whether this class is held on the given date as far as its own date range goes. */
    public boolean isValidOn(LocalDate date) {
        return (validFrom == null || !date.isBefore(validFrom))
                && (validTo == null || !date.isAfter(validTo));
    }

    public Timetable getTimetable() {
        return timetable;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public UUID getTeacherUserId() {
        return teacherUserId;
    }

    public Room getRoom() {
        return room;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public PairSlot getPairSlot() {
        return pairSlot;
    }

    public WeekParity getWeekParity() {
        return weekParity;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public String getSubgroup() {
        return subgroup;
    }

    public String getNote() {
        return note;
    }

    public Set<StudentGroup> getGroups() {
        return groups;
    }
}
