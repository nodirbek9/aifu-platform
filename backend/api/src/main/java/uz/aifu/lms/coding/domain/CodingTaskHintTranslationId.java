package uz.aifu.lms.coding.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CodingTaskHintTranslationId implements Serializable {

    @Column(name = "hint_id")
    private UUID hintId;

    @Column(name = "locale")
    private String locale;

    protected CodingTaskHintTranslationId() {}

    public CodingTaskHintTranslationId(UUID hintId, String locale) {
        this.hintId = hintId;
        this.locale = locale;
    }

    public UUID getHintId() {
        return hintId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CodingTaskHintTranslationId that)) {
            return false;
        }
        return Objects.equals(hintId, that.hintId) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hintId, locale);
    }
}
