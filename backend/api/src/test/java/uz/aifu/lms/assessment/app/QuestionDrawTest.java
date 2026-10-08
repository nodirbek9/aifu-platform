package uz.aifu.lms.assessment.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class QuestionDrawTest {

    @Test
    void bank10Pool5_attempt2SharesZeroQuestionsWithAttempt1() {
        List<UUID> bank = IntStream.range(0, 10).mapToObj(i -> UUID.randomUUID()).toList();
        RandomGenerator rng = new Random(42);

        // Attempt 1: no questions have been served yet
        List<UUID> attempt1 = QuestionDraw.draw(bank, Map.of(), 5, rng);
        assertThat(attempt1).hasSize(5);

        // Record history for attempt 1
        Instant t1 = Instant.parse("2026-10-01T10:00:00Z");
        Map<UUID, Instant> lastServed =
                attempt1.stream().collect(Collectors.toMap(id -> id, id -> t1));

        // Attempt 2: draw with attempt 1 history
        List<UUID> attempt2 = QuestionDraw.draw(bank, lastServed, 5, rng);
        assertThat(attempt2).hasSize(5);

        // Unseen-first means attempt 2 draws the 5 unseen questions; 0 overlap with attempt 1
        assertThat(attempt2).doesNotContainAnyElementsOf(attempt1);
    }

    @Test
    void bank7Pool5_attempt2OverlapsAttempt1InExactly3_andRepeatedOnesAreLeastRecentlyServed() {
        List<UUID> bank = IntStream.range(0, 7).mapToObj(i -> UUID.randomUUID()).toList();
        RandomGenerator rng = new Random(42);

        // Attempt 1: draw 5 out of 7 questions
        List<UUID> attempt1 = QuestionDraw.draw(bank, Map.of(), 5, rng);
        assertThat(attempt1).hasSize(5);

        // The remaining 2 questions in the bank were unseen in attempt 1
        List<UUID> unseenAfterAttempt1 =
                bank.stream().filter(id -> !attempt1.contains(id)).toList();
        assertThat(unseenAfterAttempt1).hasSize(2);

        // Attempt 1 served at t1
        Instant t1 = Instant.parse("2026-10-01T10:00:00Z");
        Map<UUID, Instant> lastServed =
                attempt1.stream().collect(Collectors.toMap(id -> id, id -> t1));

        // Attempt 2: pool 5 from bank 7
        List<UUID> attempt2 = QuestionDraw.draw(bank, lastServed, 5, rng);
        assertThat(attempt2).hasSize(5);

        // Attempt 2 must contain both unseen questions
        assertThat(attempt2).containsAll(unseenAfterAttempt1);

        // Overlap with attempt 1 is exactly 3 questions
        List<UUID> overlap = attempt2.stream().filter(attempt1::contains).toList();
        assertThat(overlap).hasSize(3);

        // The 3 repeated questions are from attempt 1 (served at t1, the least recently served)
        for (UUID repeatedId : overlap) {
            assertThat(lastServed.get(repeatedId)).isEqualTo(t1);
        }
    }

    @Test
    void bank7Pool5_repeatedQuestionsPreferLeastRecentlyServedWhenHistoryHasDifferentTimes() {
        // Bank of 7: 2 unseen, 3 served at tOld (older), 2 served at tNew (newer)
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        UUID o1 = UUID.randomUUID();
        UUID o2 = UUID.randomUUID();
        UUID o3 = UUID.randomUUID();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();

        List<UUID> bank = List.of(u1, u2, o1, o2, o3, n1, n2);
        Instant tOld = Instant.parse("2026-10-01T09:00:00Z");
        Instant tNew = Instant.parse("2026-10-01T10:00:00Z");

        Map<UUID, Instant> lastServed =
                Map.of(
                        o1, tOld,
                        o2, tOld,
                        o3, tOld,
                        n1, tNew,
                        n2, tNew);

        List<UUID> drawn = QuestionDraw.draw(bank, lastServed, 5, new Random(42));
        assertThat(drawn).hasSize(5);

        // Unseen questions chosen first
        assertThat(drawn).contains(u1, u2);

        // Remaining 3 must be the least recently served (from tOld), not from tNew
        assertThat(drawn).contains(o1, o2, o3);
        assertThat(drawn).doesNotContain(n1, n2);
    }

    @Test
    void poolGreaterThanOrEqualToBankReturnsAll() {
        List<UUID> bank = IntStream.range(0, 5).mapToObj(i -> UUID.randomUUID()).toList();
        RandomGenerator rng = new Random(42);
        Map<UUID, Instant> lastServed = Map.of(bank.get(0), Instant.now());

        // poolSize == bank.size()
        List<UUID> resultEqual = QuestionDraw.draw(bank, lastServed, 5, rng);
        assertThat(resultEqual).containsExactlyInAnyOrderElementsOf(bank);

        // poolSize > bank.size()
        List<UUID> resultGreater = QuestionDraw.draw(bank, lastServed, 10, rng);
        assertThat(resultGreater).containsExactlyInAnyOrderElementsOf(bank);
    }

    @Test
    void deterministicWithSeededRng() {
        List<UUID> bank = IntStream.range(0, 20).mapToObj(i -> UUID.randomUUID()).toList();
        Map<UUID, Instant> lastServed =
                Map.of(
                        bank.get(0), Instant.parse("2026-10-01T10:00:00Z"),
                        bank.get(1), Instant.parse("2026-10-01T10:00:00Z"),
                        bank.get(2), Instant.parse("2026-10-02T10:00:00Z"),
                        bank.get(3), Instant.parse("2026-10-02T10:00:00Z"));

        List<UUID> draw1 = QuestionDraw.draw(bank, lastServed, 10, new Random(12345L));
        List<UUID> draw2 = QuestionDraw.draw(bank, lastServed, 10, new Random(12345L));

        assertThat(draw1).isEqualTo(draw2);
    }

    @Test
    void emptyBankOrZeroPoolSizeReturnsEmpty() {
        RandomGenerator rng = new Random(42);
        assertThat(QuestionDraw.draw(List.of(), Map.of(), 5, rng)).isEmpty();
        assertThat(QuestionDraw.draw(List.of(UUID.randomUUID()), Map.of(), 0, rng)).isEmpty();
        assertThat(QuestionDraw.draw(List.of(UUID.randomUUID()), Map.of(), -1, rng)).isEmpty();
    }
}
