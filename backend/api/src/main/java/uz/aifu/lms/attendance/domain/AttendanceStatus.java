package uz.aifu.lms.attendance.domain;

/** A student's mark for one class. */
public enum AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    /** Absent with a valid reason ("sababli"). */
    EXCUSED;

    /** Whether the student was in the class at all. */
    public boolean attended() {
        return this == PRESENT || this == LATE;
    }
}
