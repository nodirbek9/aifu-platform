package uz.aifu.lms.coding.app;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.StudentHintView;
import uz.aifu.lms.coding.domain.CodingTaskHint;
import uz.aifu.lms.coding.domain.CodingTaskHintReveal;
import uz.aifu.lms.coding.domain.CodingTaskHintRevealId;
import uz.aifu.lms.coding.domain.CodingTaskHintTranslation;
import uz.aifu.lms.coding.persistence.CodingTaskHintRepository;
import uz.aifu.lms.coding.persistence.CodingTaskHintRevealRepository;
import uz.aifu.lms.coding.persistence.CodingTaskHintTranslationRepository;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;

/**
 * Hints as a student meets them (product spec S-05.1): opened one at a time, in order, and each
 * opening is recorded. A hint that was not opened has no text in any response.
 */
@Service
public class CodingHintService {

    private final CodingTaskHintRepository hints;
    private final CodingTaskHintTranslationRepository translations;
    private final CodingTaskHintRevealRepository reveals;
    private final Clock clock;

    public CodingHintService(
            CodingTaskHintRepository hints,
            CodingTaskHintTranslationRepository translations,
            CodingTaskHintRevealRepository reveals,
            Clock clock) {
        this.hints = hints;
        this.translations = translations;
        this.reveals = reveals;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<StudentHintView> list(UUID codingTaskId, UUID userId, String locale) {
        List<CodingTaskHint> ordered = hints.findByCodingTask_IdOrderByPosition(codingTaskId);
        return toViews(ordered, revealedBy(userId, ordered), locale);
    }

    /**
     * Opens the next hint. Opening one that is already open changes nothing; skipping ahead is
     * refused.
     */
    @Transactional
    public List<StudentHintView> reveal(
            UUID codingTaskId, UUID hintId, UUID userId, String locale) {
        List<CodingTaskHint> ordered = hints.findByCodingTask_IdOrderByPosition(codingTaskId);
        CodingTaskHint wanted =
                ordered.stream()
                        .filter(h -> h.getId().equals(hintId))
                        .findFirst()
                        .orElseThrow(() -> Exceptions.notFound("CodingTaskHint", hintId));
        Set<UUID> revealed = revealedBy(userId, ordered);
        if (!revealed.contains(hintId)) {
            boolean skipping =
                    ordered.stream()
                            .anyMatch(
                                    h ->
                                            h.getPosition() < wanted.getPosition()
                                                    && !revealed.contains(h.getId()));
            if (skipping) {
                throw Exceptions.rule(
                        ErrorCode.INVALID_STATE_TRANSITION, "Open the earlier hints first");
            }
            if (!reveals.existsById(new CodingTaskHintRevealId(hintId, userId))) {
                reveals.save(new CodingTaskHintReveal(hintId, userId, clock.instant()));
            }
            revealed.add(hintId);
        }
        return toViews(ordered, revealed, locale);
    }

    private Set<UUID> revealedBy(UUID userId, List<CodingTaskHint> ordered) {
        if (ordered.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(
                reveals.findRevealedHintIds(
                        userId, ordered.stream().map(CodingTaskHint::getId).toList()));
    }

    private List<StudentHintView> toViews(
            List<CodingTaskHint> ordered, Set<UUID> revealed, String locale) {
        Map<UUID, List<CodingTaskHintTranslation>> bodies =
                revealed.isEmpty()
                        ? Map.of()
                        : translations.findAllByHintIds(revealed).stream()
                                .collect(Collectors.groupingBy(t -> t.getHint().getId()));
        return ordered.stream()
                .map(
                        h ->
                                new StudentHintView(
                                        h.getId(),
                                        h.getPosition(),
                                        revealed.contains(h.getId()),
                                        revealed.contains(h.getId())
                                                ? body(
                                                        bodies.getOrDefault(h.getId(), List.of()),
                                                        locale)
                                                : null))
                .toList();
    }

    /** The requested locale, else whatever translation exists. */
    private static String body(List<CodingTaskHintTranslation> all, String locale) {
        return all.stream()
                .filter(t -> t.getLocale().equals(locale))
                .findFirst()
                .or(() -> all.stream().findFirst())
                .map(CodingTaskHintTranslation::getBody)
                .orElse(null);
    }
}
