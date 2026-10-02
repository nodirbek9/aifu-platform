package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ActivityTranslationId implements Serializable {

    @Column(name = "activity_id")
    private UUID activityId;

    @Column(name = "locale")
    private String locale;

    protected ActivityTranslationId() {}

    public ActivityTranslationId(UUID activityId, String locale) {
        this.activityId = activityId;
        this.locale = locale;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ActivityTranslationId that)) {
            return false;
        }
        return Objects.equals(activityId, that.activityId) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, locale);
    }
}
