package uz.aifu.lms.attendance.app;

import java.util.Collection;
import uz.aifu.lms.attendance.api.dto.AttendanceDtos.Totals;
import uz.aifu.lms.attendance.domain.AttendanceStatus;

/** Counting marks the same way everywhere: the class page, the journal and a student's own view. */
final class AttendanceMath {

    private AttendanceMath() {}

    /**
     * @param marks one element per class or student; {@code null} means not marked
     * @return counts, and the share of marked classes the student attended (present or late),
     *     rounded to a whole percent; no percentage when nothing is marked
     */
    static Totals totals(Collection<AttendanceStatus> marks) {
        int present = 0;
        int late = 0;
        int absent = 0;
        int excused = 0;
        int unmarked = 0;
        for (AttendanceStatus mark : marks) {
            if (mark == null) {
                unmarked++;
                continue;
            }
            switch (mark) {
                case PRESENT -> present++;
                case LATE -> late++;
                case ABSENT -> absent++;
                case EXCUSED -> excused++;
            }
        }
        int marked = present + late + absent + excused;
        Integer percent = marked == 0 ? null : Math.round((present + late) * 100f / marked);
        return new Totals(present, late, absent, excused, unmarked, percent);
    }
}
