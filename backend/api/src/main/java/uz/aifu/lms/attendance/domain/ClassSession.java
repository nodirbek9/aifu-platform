package uz.aifu.lms.attendance.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.common.domain.AuditableEntity;
import uz.aifu.lms.common.util.Guards;

/**
 * One class on a concrete date: what the teacher records about it (the lesson taught, private
 * notes, whether attendance was taken).
 *
 * <p>Its identity is what the class is — course, date, pair, groups, subgroup — not the timetable
 * entry it came from. Publishing a corrected copy of a timetable gives every class a new entry id,
 * and the journal has to survive that; {@link #entryId} is only a pointer back.
 */
@Entity
@Table(name = "class_sessions")
public class ClassSession extends AuditableEntity {

    @Column(name = "course_id", nullable = false, updatable = false)
    private UUID courseId;

    @Column(name = "on_date", nullable = false, updatable = false)
    private LocalDate onDate;

    @Column(name = "pair_no", nullable = false, updatable = false)
    private int pairNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Column(name = "groups_key", nullable = false, updatable = false)
    private String groupsKey;

    @Column(name = "subgroup", nullable = false, updatable = false)
    private String subgroup = "";

    @Column(name = "entry_id")
    private UUID entryId;

    @Column(name = "teacher_user_id")
    private UUID teacherUserId;

    @Column(name = "lesson_id")
    private UUID lessonId;

    @Column(name = "topic")
    private String topic;

    @Column(name = "notes")
    private String notes;

    @Column(name = "attendance_marked_at")
    private Instant attendanceMarkedAt;

    @Column(name = "attendance_marked_by")
    private UUID attendanceMarkedBy;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "class_session_groups", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "group_id")
    private Set<UUID> groupIds = new LinkedHashSet<>();

    protected ClassSession() {}

    public ClassSession(
            UUID courseId,
            LocalDate onDate,
            int pairNo,
            SessionType sessionType,
            Collection<UUID> groupIds,
            String subgroup) {
        this.courseId = Guards.required(courseId, "courseId");
        this.onDate = Guards.required(onDate, "onDate");
        this.pairNo = pairNo;
        this.sessionType = Guards.required(sessionType, "sessionType");
        this.groupIds.addAll(Guards.requireNotEmpty(groupIds, "groupIds"));
        this.groupsKey = groupsKey(groupIds);
        this.subgroup = subgroupKey(subgroup);
    }

    /** The identity part for a set of groups: their ids, sorted and comma-joined. */
    public static String groupsKey(Collection<UUID> groupIds) {
        return groupIds.stream().map(UUID::toString).sorted().collect(Collectors.joining(","));
    }

    /** The identity part for a subgroup: empty for the whole group. */
    public static String subgroupKey(String subgroup) {
        return subgroup == null ? "" : subgroup.trim();
    }

    /** Keeps the pointer to the timetable current (entry ids change when a copy is published). */
    public void pointTo(UUID entryId, UUID teacherUserId) {
        this.entryId = entryId;
        this.teacherUserId = teacherUserId;
    }

    public void describe(UUID lessonId, String topic, String notes) {
        this.lessonId = lessonId;
        this.topic = blankToNull(topic);
        this.notes = blankToNull(notes);
    }

    public void attendanceMarked(UUID actorId, Instant now) {
        this.attendanceMarkedBy = actorId;
        this.attendanceMarkedAt = now;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public UUID getCourseId() {
        return courseId;
    }

    public LocalDate getOnDate() {
        return onDate;
    }

    public int getPairNo() {
        return pairNo;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public String getGroupsKey() {
        return groupsKey;
    }

    public String getSubgroup() {
        return subgroup;
    }

    public UUID getEntryId() {
        return entryId;
    }

    public UUID getTeacherUserId() {
        return teacherUserId;
    }

    public UUID getLessonId() {
        return lessonId;
    }

    public String getTopic() {
        return topic;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getAttendanceMarkedAt() {
        return attendanceMarkedAt;
    }

    public UUID getAttendanceMarkedBy() {
        return attendanceMarkedBy;
    }

    public Set<UUID> getGroupIds() {
        return groupIds;
    }
}
