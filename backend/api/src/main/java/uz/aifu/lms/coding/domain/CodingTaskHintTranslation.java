package uz.aifu.lms.coding.domain;

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
@Table(name = "coding_task_hint_translations")
public class CodingTaskHintTranslation {

    @EmbeddedId private CodingTaskHintTranslationId id;

    @MapsId("hintId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hint_id")
    private CodingTaskHint hint;

    @Column(name = "body", nullable = false)
    private String body;

    protected CodingTaskHintTranslation() {}

    public CodingTaskHintTranslation(CodingTaskHint hint, String locale, String body) {
        this.hint = Guards.required(hint, "hint");
        this.id =
                new CodingTaskHintTranslationId(
                        hint.getId(), Guards.requiredText(locale, "locale"));
        this.body = Guards.requiredText(body, "body");
    }

    public void update(String body) {
        this.body = Guards.requiredText(body, "body");
    }

    public String getLocale() {
        return id.getLocale();
    }

    public CodingTaskHint getHint() {
        return hint;
    }

    public String getBody() {
        return body;
    }
}
