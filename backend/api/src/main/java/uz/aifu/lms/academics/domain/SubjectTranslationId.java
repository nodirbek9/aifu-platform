package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SubjectTranslationId implements Serializable {

    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "locale")
    private String locale;

    protected SubjectTranslationId() {}

    public SubjectTranslationId(UUID subjectId, String locale) {
        this.subjectId = subjectId;
        this.locale = locale;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubjectTranslationId that)) {
            return false;
        }
        return Objects.equals(subjectId, that.subjectId) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectId, locale);
    }
}
