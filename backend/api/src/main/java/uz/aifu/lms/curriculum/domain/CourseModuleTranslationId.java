package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CourseModuleTranslationId implements Serializable {

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "locale")
    private String locale;

    protected CourseModuleTranslationId() {}

    public CourseModuleTranslationId(UUID moduleId, String locale) {
        this.moduleId = moduleId;
        this.locale = locale;
    }

    public UUID getModuleId() {
        return moduleId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseModuleTranslationId that)) {
            return false;
        }
        return Objects.equals(moduleId, that.moduleId) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleId, locale);
    }
}
