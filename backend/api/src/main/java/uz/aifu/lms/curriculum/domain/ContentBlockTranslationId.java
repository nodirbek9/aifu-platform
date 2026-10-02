package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ContentBlockTranslationId implements Serializable {

    @Column(name = "block_id")
    private UUID blockId;

    @Column(name = "locale")
    private String locale;

    protected ContentBlockTranslationId() {}

    public ContentBlockTranslationId(UUID blockId, String locale) {
        this.blockId = blockId;
        this.locale = locale;
    }

    public UUID getBlockId() {
        return blockId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContentBlockTranslationId that)) {
            return false;
        }
        return Objects.equals(blockId, that.blockId) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockId, locale);
    }
}
