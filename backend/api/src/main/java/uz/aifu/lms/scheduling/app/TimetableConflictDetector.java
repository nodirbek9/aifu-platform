package uz.aifu.lms.scheduling.app;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import uz.aifu.lms.scheduling.domain.WeekParity;

/**
 * Decides whether two recurring classes clash. Pure logic, no persistence: the service maps entries
 * to {@link Slot}s and holds the timetable lock around "check then write".
 *
 * <p>Two classes clash when they sit in the same weekday at overlapping times (pairs of different
 * shifts never share a row, so the times decide, not the pair), can meet in the same week (parity)
 * and on a common date (validity ranges), and share a teacher, a room, or a group. Two different
 * subgroups of one group ("A" and "B" of a split lab) do not clash with each other; a whole-group
 * class clashes with any subgroup of that group.
 */
public final class TimetableConflictDetector {

    private TimetableConflictDetector() {}

    /** The facts of one class that matter for clashes. {@code entryId} is null for a new class. */
    public record Slot(
            UUID entryId,
            UUID teacherUserId,
            UUID roomId,
            Set<UUID> groupIds,
            String subgroup,
            int dayOfWeek,
            PairTime pair,
            WeekParity parity,
            LocalDate validFrom,
            LocalDate validTo) {}

    /** When a pair is held; a pair ending at 13:30 and one starting at 13:30 do not overlap. */
    public record PairTime(LocalTime startsAt, LocalTime endsAt) {
        boolean overlaps(PairTime other) {
            return startsAt.isBefore(other.endsAt) && other.startsAt.isBefore(endsAt);
        }
    }

    public enum Kind {
        TEACHER,
        ROOM,
        GROUP
    }

    public record Conflict(Kind kind, UUID withEntryId) {}

    public static List<Conflict> detect(Slot candidate, Collection<Slot> existing) {
        List<Conflict> conflicts = new ArrayList<>();
        for (Slot other : existing) {
            if (other.entryId() != null && other.entryId().equals(candidate.entryId())) {
                continue; // an entry never clashes with itself when it is edited or moved
            }
            if (!sameTime(candidate, other)) {
                continue;
            }
            if (candidate.teacherUserId().equals(other.teacherUserId())) {
                conflicts.add(new Conflict(Kind.TEACHER, other.entryId()));
            }
            if (candidate.roomId() != null && candidate.roomId().equals(other.roomId())) {
                conflicts.add(new Conflict(Kind.ROOM, other.entryId()));
            }
            if (sharesGroup(candidate, other)) {
                conflicts.add(new Conflict(Kind.GROUP, other.entryId()));
            }
        }
        return conflicts;
    }

    private static boolean sameTime(Slot a, Slot b) {
        return a.dayOfWeek() == b.dayOfWeek()
                && a.pair().overlaps(b.pair())
                && a.parity().overlaps(b.parity())
                && rangesOverlap(a.validFrom(), a.validTo(), b.validFrom(), b.validTo());
    }

    /** Open ends (null) mean "the whole term". */
    static boolean rangesOverlap(LocalDate aFrom, LocalDate aTo, LocalDate bFrom, LocalDate bTo) {
        boolean aEndsBeforeB = aTo != null && bFrom != null && aTo.isBefore(bFrom);
        boolean bEndsBeforeA = bTo != null && aFrom != null && bTo.isBefore(aFrom);
        return !aEndsBeforeB && !bEndsBeforeA;
    }

    private static boolean sharesGroup(Slot a, Slot b) {
        boolean common = a.groupIds().stream().anyMatch(b.groupIds()::contains);
        if (!common) {
            return false;
        }
        boolean differentSubgroups =
                a.subgroup() != null
                        && b.subgroup() != null
                        && !Objects.equals(a.subgroup(), b.subgroup());
        return !differentSubgroups;
    }
}
