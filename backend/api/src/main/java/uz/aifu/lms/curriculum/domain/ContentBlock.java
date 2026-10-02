package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.aifu.lms.common.domain.VersionedEntity;
import uz.aifu.lms.common.util.Guards;

/**
 * "A lesson is never one giant text column" (brief §12). One typed, positioned unit of content.
 * {@code settings} is non-localized, type-specific configuration (code-sample language, callout
 * variant, embed provider, referenced quiz/task id) — validated per {@link ContentBlockType} by
 * {@code curriculum.app.content.ContentBlockPayloadValidator}. {@code file_id} is a bare UUID, not
 * a {@code @ManyToOne}: {@code files} (Phase 11) does not exist yet, matching {@code
 * CourseTeacher.assignedBy}'s pattern for not-yet-built modules.
 */
@Entity
@Table(name = "content_blocks")
public class ContentBlock extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false, updatable = false)
    private Lesson lesson;

    @Column(name = "position", nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false, updatable = false)
    private ContentBlockType blockType;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private BlockVisibility visibility = BlockVisibility.ALL;

    /**
     * JSON text, e.g. {@code {"language":"python"}} or {@code
     * {"provider":"YOUTUBE","externalId":"..."}}. Always validated (never merely stored) by the
     * matching {@code ContentBlockPayloadValidator}.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings", nullable = false)
    private String settings = "{}";

    @Column(name = "file_id")
    private UUID fileId;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected ContentBlock() {}

    public ContentBlock(Lesson lesson, int position, ContentBlockType blockType) {
        this.lesson = Guards.required(lesson, "lesson");
        Guards.require(position >= 0, "position must be >= 0");
        this.position = position;
        this.blockType = Guards.required(blockType, "blockType");
    }

    public void updateSettings(
            String settingsJson, UUID fileId, boolean required, BlockVisibility visibility) {
        this.settings = settingsJson == null ? "{}" : settingsJson;
        this.fileId = fileId;
        this.required = required;
        this.visibility = visibility == null ? BlockVisibility.ALL : visibility;
    }

    public void reorderTo(int newPosition) {
        Guards.require(newPosition >= 0, "position must be >= 0");
        this.position = newPosition;
    }

    public void softDelete(Instant now) {
        this.deletedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public int getPosition() {
        return position;
    }

    public ContentBlockType getBlockType() {
        return blockType;
    }

    public boolean isRequired() {
        return required;
    }

    public BlockVisibility getVisibility() {
        return visibility;
    }

    public String getSettings() {
        return settings;
    }

    public UUID getFileId() {
        return fileId;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
