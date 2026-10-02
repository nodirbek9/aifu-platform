package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.aifu.lms.common.util.Guards;

/**
 * Per-locale, per-block-type-polymorphic content (ADR-005). {@code payload} is ALWAYS sanitized on
 * write by {@code ContentSanitizer} before it ever reaches this entity — never trusted on read.
 * {@code plainText} is a flattened, sanitized copy used only to populate the DB-generated FTS
 * column; it is never returned to a client.
 */
@Entity
@Table(name = "content_block_translations")
public class ContentBlockTranslation {

    @EmbeddedId private ContentBlockTranslationId id;

    @MapsId("blockId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id")
    private ContentBlock block;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "plain_text")
    private String plainText;

    protected ContentBlockTranslation() {}

    public ContentBlockTranslation(
            ContentBlock block, String locale, String payloadJson, String plainText) {
        this.block = Guards.required(block, "block");
        this.id =
                new ContentBlockTranslationId(block.getId(), Guards.requiredText(locale, "locale"));
        this.payload = Guards.requiredText(payloadJson, "payload");
        this.plainText = plainText;
    }

    public void update(String payloadJson, String plainText) {
        this.payload = Guards.requiredText(payloadJson, "payload");
        this.plainText = plainText;
    }

    public String getLocale() {
        return id.getLocale();
    }

    public ContentBlock getBlock() {
        return block;
    }

    public String getPayload() {
        return payload;
    }

    public String getPlainText() {
        return plainText;
    }
}
