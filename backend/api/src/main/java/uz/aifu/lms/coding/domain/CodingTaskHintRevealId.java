package uz.aifu.lms.coding.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CodingTaskHintRevealId implements Serializable {

    @Column(name = "hint_id")
    private UUID hintId;

    @Column(name = "user_id")
    private UUID userId;

    protected CodingTaskHintRevealId() {}

    public CodingTaskHintRevealId(UUID hintId, UUID userId) {
        this.hintId = hintId;
        this.userId = userId;
    }

    public UUID getHintId() {
        return hintId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CodingTaskHintRevealId that)) {
            return false;
        }
        return Objects.equals(hintId, that.hintId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hintId, userId);
    }
}
