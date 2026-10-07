package uz.aifu.lms.scheduling.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.scheduling.app.TimetableConflictDetector.Conflict;
import uz.aifu.lms.scheduling.app.TimetableConflictDetector.Kind;
import uz.aifu.lms.scheduling.app.TimetableConflictDetector.PairTime;
import uz.aifu.lms.scheduling.app.TimetableConflictDetector.Slot;
import uz.aifu.lms.scheduling.domain.WeekParity;

class TimetableConflictDetectorTest {

    private static final PairTime PAIR_1 = new PairTime(LocalTime.of(9, 0), LocalTime.of(10, 20));
    private static final PairTime PAIR_2 = new PairTime(LocalTime.of(10, 35), LocalTime.of(11, 55));
    private static final UUID TEACHER = UUID.randomUUID();
    private static final UUID ROOM = UUID.randomUUID();
    private static final UUID GROUP = UUID.randomUUID();

    private static Slot slot(UUID teacher, UUID room, UUID group, WeekParity parity) {
        return new Slot(
                UUID.randomUUID(),
                teacher,
                room,
                Set.of(group),
                null,
                1,
                PAIR_1,
                parity,
                null,
                null);
    }

    private static List<Kind> kinds(Slot candidate, Slot... existing) {
        return TimetableConflictDetector.detect(candidate, List.of(existing)).stream()
                .map(Conflict::kind)
                .toList();
    }

    @Test
    void sameSlot_reportsTeacherRoomAndGroupSeparately() {
        Slot existing = slot(TEACHER, ROOM, GROUP, WeekParity.EVERY);

        assertThat(
                        kinds(
                                slot(
                                        TEACHER,
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        WeekParity.EVERY),
                                existing))
                .containsExactly(Kind.TEACHER);
        assertThat(
                        kinds(
                                slot(UUID.randomUUID(), ROOM, UUID.randomUUID(), WeekParity.EVERY),
                                existing))
                .containsExactly(Kind.ROOM);
        assertThat(
                        kinds(
                                slot(UUID.randomUUID(), UUID.randomUUID(), GROUP, WeekParity.EVERY),
                                existing))
                .containsExactly(Kind.GROUP);
        assertThat(kinds(slot(TEACHER, ROOM, GROUP, WeekParity.EVERY), existing))
                .containsExactly(Kind.TEACHER, Kind.ROOM, Kind.GROUP);
    }

    @Test
    void differentDayOrPair_neverClashes() {
        Slot existing = slot(TEACHER, ROOM, GROUP, WeekParity.EVERY);
        Slot otherPair =
                new Slot(
                        null,
                        TEACHER,
                        ROOM,
                        Set.of(GROUP),
                        null,
                        1,
                        PAIR_2,
                        WeekParity.EVERY,
                        null,
                        null);
        Slot otherDay =
                new Slot(
                        null,
                        TEACHER,
                        ROOM,
                        Set.of(GROUP),
                        null,
                        2,
                        PAIR_1,
                        WeekParity.EVERY,
                        null,
                        null);

        assertThat(kinds(otherPair, existing)).isEmpty();
        assertThat(kinds(otherDay, existing)).isEmpty();
    }

    @Test
    void oddAndEvenWeeksShareASlot_butEveryWeekClashesWithBoth() {
        Slot odd = slot(TEACHER, ROOM, GROUP, WeekParity.ODD);

        assertThat(kinds(slot(TEACHER, ROOM, GROUP, WeekParity.EVEN), odd)).isEmpty();
        assertThat(kinds(slot(TEACHER, ROOM, GROUP, WeekParity.ODD), odd)).hasSize(3);
        assertThat(kinds(slot(TEACHER, ROOM, GROUP, WeekParity.EVERY), odd)).hasSize(3);
    }

    @Test
    void nonOverlappingDateRanges_doNotClash() {
        LocalDate oct1 = LocalDate.of(2026, 10, 1);
        Slot firstHalf =
                new Slot(
                        UUID.randomUUID(),
                        TEACHER,
                        ROOM,
                        Set.of(GROUP),
                        null,
                        1,
                        PAIR_1,
                        WeekParity.EVERY,
                        null,
                        oct1);
        Slot secondHalf =
                new Slot(
                        null,
                        TEACHER,
                        ROOM,
                        Set.of(GROUP),
                        null,
                        1,
                        PAIR_1,
                        WeekParity.EVERY,
                        oct1.plusDays(1),
                        null);
        Slot touching =
                new Slot(
                        null,
                        TEACHER,
                        ROOM,
                        Set.of(GROUP),
                        null,
                        1,
                        PAIR_1,
                        WeekParity.EVERY,
                        oct1,
                        null);

        assertThat(kinds(secondHalf, firstHalf)).isEmpty();
        assertThat(kinds(touching, firstHalf)).hasSize(3);
    }

    @Test
    void twoSubgroupsOfOneGroupMayMeetTogether_butWholeGroupBlocksASubgroup() {
        UUID otherTeacher = UUID.randomUUID();
        UUID otherRoom = UUID.randomUUID();
        Slot subgroupA =
                new Slot(
                        UUID.randomUUID(),
                        TEACHER,
                        ROOM,
                        Set.of(GROUP),
                        "A",
                        1,
                        PAIR_1,
                        WeekParity.EVERY,
                        null,
                        null);
        Slot subgroupB =
                new Slot(
                        null,
                        otherTeacher,
                        otherRoom,
                        Set.of(GROUP),
                        "B",
                        1,
                        PAIR_1,
                        WeekParity.EVERY,
                        null,
                        null);
        Slot sameSubgroup =
                new Slot(
                        null,
                        otherTeacher,
                        otherRoom,
                        Set.of(GROUP),
                        "A",
                        1,
                        PAIR_1,
                        WeekParity.EVERY,
                        null,
                        null);
        Slot wholeGroup =
                new Slot(
                        null,
                        otherTeacher,
                        otherRoom,
                        Set.of(GROUP),
                        null,
                        1,
                        PAIR_1,
                        WeekParity.EVERY,
                        null,
                        null);

        assertThat(kinds(subgroupB, subgroupA)).isEmpty();
        assertThat(kinds(sameSubgroup, subgroupA)).containsExactly(Kind.GROUP);
        assertThat(kinds(wholeGroup, subgroupA)).containsExactly(Kind.GROUP);
    }

    @Test
    void anEditedEntryDoesNotClashWithItself_andOnlineClassesShareNoRoom() {
        Slot existing = slot(TEACHER, ROOM, GROUP, WeekParity.EVERY);
        Slot moved =
                new Slot(
                        existing.entryId(),
                        TEACHER,
                        ROOM,
                        Set.of(GROUP),
                        null,
                        1,
                        PAIR_1,
                        WeekParity.EVERY,
                        null,
                        null);
        Slot onlineA =
                new Slot(
                        UUID.randomUUID(),
                        TEACHER,
                        null,
                        Set.of(GROUP),
                        null,
                        1,
                        PAIR_1,
                        WeekParity.EVERY,
                        null,
                        null);
        Slot onlineB =
                new Slot(
                        null,
                        UUID.randomUUID(),
                        null,
                        Set.of(UUID.randomUUID()),
                        null,
                        1,
                        PAIR_1,
                        WeekParity.EVERY,
                        null,
                        null);

        assertThat(kinds(moved, existing)).isEmpty();
        assertThat(kinds(onlineB, onlineA)).isEmpty();
    }

    @Test
    void weekCalendar_numbersWeeksFromTheTermStartAndAlternatesParity() {
        LocalDate termStart = LocalDate.of(2026, 9, 2); // a Wednesday
        assertThat(WeekCalendar.mondayOf(termStart)).isEqualTo(LocalDate.of(2026, 8, 31));
        assertThat(WeekCalendar.weekNumber(termStart, LocalDate.of(2026, 9, 6)))
                .isEqualTo(1); // Sunday
        assertThat(WeekCalendar.weekNumber(termStart, LocalDate.of(2026, 9, 7))).isEqualTo(2);
        assertThat(WeekCalendar.parityOn(termStart, "ODD", LocalDate.of(2026, 9, 4)))
                .isEqualTo(WeekParity.ODD);
        assertThat(WeekCalendar.parityOn(termStart, "ODD", LocalDate.of(2026, 9, 9)))
                .isEqualTo(WeekParity.EVEN);
        assertThat(WeekCalendar.parityOn(termStart, "EVEN", LocalDate.of(2026, 9, 9)))
                .isEqualTo(WeekParity.ODD);
        assertThat(WeekCalendar.parityOn(termStart, "ODD", LocalDate.of(2026, 9, 16)))
                .isEqualTo(WeekParity.ODD);
    }

    @Test
    void scheduledHours_countASplitClassOncePerStudent() {
        assertThat(TimetableQueryService.scheduledHours(null)).isZero();
        assertThat(TimetableQueryService.scheduledHours(java.util.Map.of("", 15))).isEqualTo(30);
        assertThat(TimetableQueryService.scheduledHours(java.util.Map.of("", 15, "A", 8, "B", 7)))
                .isEqualTo(46);
    }

    @Test
    void pairsOfDifferentShifts_clashOnlyWhenTheirTimesOverlap() {
        PairTime morningThird = new PairTime(LocalTime.of(12, 10), LocalTime.of(13, 30));
        PairTime afternoonFirst = new PairTime(LocalTime.of(13, 30), LocalTime.of(14, 50));
        PairTime movedLate = new PairTime(LocalTime.of(13, 0), LocalTime.of(14, 20));
        Slot morning = at(morningThird);

        // 12:10–13:30 and 13:30–14:50 only touch: the same teacher can teach both
        assertThat(TimetableConflictDetector.detect(at(afternoonFirst), List.of(morning)))
                .isEmpty();
        // a pair edited to 13:00–14:20 overlaps the morning pair: a clash, though the rows differ
        assertThat(TimetableConflictDetector.detect(at(movedLate), List.of(morning)))
                .extracting(Conflict::kind)
                .containsExactly(Kind.TEACHER);
    }

    private static Slot at(PairTime pair) {
        return new Slot(
                UUID.randomUUID(),
                TEACHER,
                null,
                Set.of(UUID.randomUUID()),
                null,
                1,
                pair,
                WeekParity.EVERY,
                null,
                null);
    }
}
