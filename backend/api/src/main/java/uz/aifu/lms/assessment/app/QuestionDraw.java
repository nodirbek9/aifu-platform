package uz.aifu.lms.assessment.app;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * Pure helper for drawing a subset of questions from a quiz question bank for an attempt.
 *
 * <p>Draw policy is UNSEEN-FIRST (ADR-016):
 *
 * <ul>
 *   <li>Prefer questions never served in earlier attempts of the same enrollment.
 *   <li>Fill any remainder with the least recently served ones (oldest {@link Instant} first).
 *   <li>Shuffle inside each group using the provided {@link RandomGenerator}.
 * </ul>
 */
public final class QuestionDraw {

    private QuestionDraw() {}

    /**
     * Draws {@code poolSize} question IDs from {@code bank}.
     *
     * @param bank all question IDs available in the quiz bank
     * @param lastServed map of question ID to the most recent time it was served to this enrollment
     * @param poolSize number of questions to draw
     * @param rng random generator for shuffling within groups
     * @return list of chosen question IDs
     */
    public static List<UUID> draw(
            List<UUID> bank, Map<UUID, Instant> lastServed, int poolSize, RandomGenerator rng) {
        Objects.requireNonNull(bank, "bank must not be null");
        Objects.requireNonNull(rng, "rng must not be null");

        if (bank.isEmpty() || poolSize <= 0) {
            return List.of();
        }
        if (poolSize >= bank.size()) {
            return new ArrayList<>(bank);
        }

        List<UUID> unseen = new ArrayList<>();
        Map<Instant, List<UUID>> seenByTime = new TreeMap<>();

        for (UUID id : bank) {
            Instant servedAt = (lastServed != null) ? lastServed.get(id) : null;
            if (servedAt == null) {
                unseen.add(id);
            } else {
                seenByTime.computeIfAbsent(servedAt, k -> new ArrayList<>()).add(id);
            }
        }

        List<UUID> result = new ArrayList<>(poolSize);

        // 1. Unseen questions first
        shuffle(unseen, rng);
        if (unseen.size() <= poolSize) {
            result.addAll(unseen);
            if (result.size() == poolSize) {
                return result;
            }
        } else {
            result.addAll(unseen.subList(0, poolSize));
            return result;
        }

        // 2. Fill remainder with least recently served (oldest Instant first)
        for (List<UUID> group : seenByTime.values()) {
            shuffle(group, rng);
            int needed = poolSize - result.size();
            if (group.size() <= needed) {
                result.addAll(group);
            } else {
                result.addAll(group.subList(0, needed));
            }
            if (result.size() >= poolSize) {
                break;
            }
        }

        return result;
    }

    private static <T> void shuffle(List<T> list, RandomGenerator rng) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            Collections.swap(list, i, j);
        }
    }
}
