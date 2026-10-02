package uz.aifu.lms.scheduling.app;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import uz.aifu.lms.scheduling.domain.WeekParity;

/**
 * Week arithmetic for the timetable. Weeks run Monday to Sunday and are numbered from the week the
 * term starts in (week 1), which carries the term's {@code first_week_parity}; parity then
 * alternates. Clients never compute this themselves.
 */
public final class WeekCalendar {

    private WeekCalendar() {}

    public static LocalDate mondayOf(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /** 1 for the term's first week; 0 or negative before the term starts. */
    public static int weekNumber(LocalDate termStartsOn, LocalDate date) {
        return (int) ChronoUnit.WEEKS.between(mondayOf(termStartsOn), mondayOf(date)) + 1;
    }

    public static WeekParity parityOf(WeekParity firstWeekParity, int weekNumber) {
        boolean sameAsFirst = Math.floorMod(weekNumber, 2) == 1;
        return sameAsFirst ? firstWeekParity : firstWeekParity.flipped();
    }

    public static WeekParity parityOn(
            LocalDate termStartsOn, String firstWeekParity, LocalDate date) {
        return parityOf(WeekParity.valueOf(firstWeekParity), weekNumber(termStartsOn, date));
    }
}
