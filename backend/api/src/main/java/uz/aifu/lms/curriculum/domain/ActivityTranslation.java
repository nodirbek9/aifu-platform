package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import uz.aifu.lms.common.util.Guards;

@Entity
@Table(name = "activity_translations")
public class ActivityTranslation {

    @EmbeddedId private ActivityTranslationId id;

    @MapsId("activityId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "instructions")
    private String instructions;

    protected ActivityTranslation() {}

    public ActivityTranslation(
            Activity activity, String locale, String title, String instructions) {
        this.activity = Guards.required(activity, "activity");
        this.id =
                new ActivityTranslationId(activity.getId(), Guards.requiredText(locale, "locale"));
        this.title = Guards.requiredText(title, "title");
        this.instructions = instructions;
    }

    public void update(String title, String instructions) {
        this.title = Guards.requiredText(title, "title");
        this.instructions = instructions;
    }

    public String getLocale() {
        return id.getLocale();
    }

    public Activity getActivity() {
        return activity;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructions() {
        return instructions;
    }
}
