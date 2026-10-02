package uz.aifu.lms.coding.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CodingTaskTranslationId implements Serializable {

    @Column(name = "coding_task_id")
    private UUID codingTaskId;

    @Column(name = "locale")
    private String locale;

    protected CodingTaskTranslationId() {}

    public CodingTaskTranslationId(UUID codingTaskId, String locale) {
        this.codingTaskId = codingTaskId;
        this.locale = locale;
    }

    public UUID getCodingTaskId() {
        return codingTaskId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CodingTaskTranslationId that)) {
            return false;
        }
        return Objects.equals(codingTaskId, that.codingTaskId)
                && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codingTaskId, locale);
    }
}
