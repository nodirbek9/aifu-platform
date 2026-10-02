package uz.aifu.lms.grading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.common.util.Guards;

/**
 * Free-text feedback attached to a polymorphic target (V012) — a submission, a quiz attempt, a
 * grade entry, an activity, or an enrollment as a whole. {@code targetId} is a bare {@code UUID}
 * (no FK in the schema, by design: the target lives in a different table per {@link
 * FeedbackTargetType}). No {@link uz.aifu.lms.common.domain.BaseEntity}: the table has {@code
 * created_at}/{@code updated_at} but no {@code version} — feedback is edited by its single author
 * only, not a contended write path.
 */
@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, updatable = false)
    private FeedbackTargetType targetType;

    @Column(name = "target_id", nullable = false, updatable = false)
    private UUID targetId;

    @Column(name = "author_user_id", nullable = false, updatable = false)
    private UUID authorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private FeedbackVisibility visibility = FeedbackVisibility.STUDENT_VISIBLE;

    @Column(name = "body_markdown", nullable = false)
    private String bodyMarkdown;

    @Column(name = "body_sanitized_html")
    private String bodySanitizedHtml;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "edited_at")
    private Instant editedAt;

    protected Feedback() {}

    public Feedback(
            FeedbackTargetType targetType,
            UUID targetId,
            UUID authorUserId,
            FeedbackVisibility visibility,
            String bodyMarkdown,
            String bodySanitizedHtml,
            Instant now) {
        this.targetType = Guards.required(targetType, "targetType");
        this.targetId = Guards.required(targetId, "targetId");
        this.authorUserId = Guards.required(authorUserId, "authorUserId");
        this.visibility = visibility == null ? FeedbackVisibility.STUDENT_VISIBLE : visibility;
        this.bodyMarkdown = Guards.requiredText(bodyMarkdown, "bodyMarkdown");
        this.bodySanitizedHtml = bodySanitizedHtml;
        this.createdAt = Guards.required(now, "now");
        this.updatedAt = now;
    }

    public void update(
            FeedbackVisibility visibility,
            String bodyMarkdown,
            String bodySanitizedHtml,
            Instant now) {
        this.visibility = visibility == null ? this.visibility : visibility;
        this.bodyMarkdown = Guards.requiredText(bodyMarkdown, "bodyMarkdown");
        this.bodySanitizedHtml = bodySanitizedHtml;
        this.editedAt = Guards.required(now, "now");
    }

    public UUID getId() {
        return id;
    }

    public FeedbackTargetType getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public FeedbackVisibility getVisibility() {
        return visibility;
    }

    public String getBodyMarkdown() {
        return bodyMarkdown;
    }

    public String getBodySanitizedHtml() {
        return bodySanitizedHtml;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getEditedAt() {
        return editedAt;
    }
}
