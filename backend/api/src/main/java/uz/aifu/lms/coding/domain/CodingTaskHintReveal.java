package uz.aifu.lms.coding.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A student opened a hint (V022). A hint's text reaches a student only once this row exists, so a
 * teacher can tell how much help a solution needed.
 */
@Entity
@Table(name = "coding_task_hint_reveals")
public class CodingTaskHintReveal {

    @EmbeddedId private CodingTaskHintRevealId id;

    @Column(name = "revealed_at", nullable = false, updatable = false)
    private Instant revealedAt;

    protected CodingTaskHintReveal() {}

    public CodingTaskHintReveal(UUID hintId, UUID userId, Instant now) {
        this.id = new CodingTaskHintRevealId(hintId, userId);
        this.revealedAt = now;
    }

    public UUID getHintId() {
        return id.getHintId();
    }

    public UUID getUserId() {
        return id.getUserId();
    }

    public Instant getRevealedAt() {
        return revealedAt;
    }
}
