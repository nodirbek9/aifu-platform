package uz.aifu.lms.grading.app;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.grading.domain.Feedback;
import uz.aifu.lms.grading.domain.FeedbackTargetType;
import uz.aifu.lms.grading.domain.FeedbackVisibility;
import uz.aifu.lms.grading.persistence.FeedbackRepository;

/**
 * Free-text feedback on a submission/attempt/grade/activity/enrollment (V012). Markdown in,
 * sanitized on write, exactly like {@code common.content.ContentSanitizer}'s other callers
 * (CLAUDE.md §7.6).
 */
@Service
public class FeedbackService {

    private final FeedbackRepository feedback;
    private final ContentSanitizer sanitizer;
    private final AuditService audit;
    private final Clock clock;

    public FeedbackService(
            FeedbackRepository feedback,
            ContentSanitizer sanitizer,
            AuditService audit,
            Clock clock) {
        this.feedback = feedback;
        this.sanitizer = sanitizer;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Feedback require(UUID id) {
        return feedback.findById(id).orElseThrow(() -> Exceptions.notFound("Feedback", id));
    }

    @Transactional(readOnly = true)
    public List<Feedback> listForTarget(FeedbackTargetType targetType, UUID targetId) {
        return feedback.findByTargetTypeAndTargetIdOrderByCreatedAt(targetType, targetId);
    }

    /** Student-facing read: {@link FeedbackVisibility#INTERNAL} entries never reach a student. */
    @Transactional(readOnly = true)
    public List<Feedback> listVisibleForTarget(FeedbackTargetType targetType, UUID targetId) {
        return listForTarget(targetType, targetId).stream()
                .filter(f -> f.getVisibility() == FeedbackVisibility.STUDENT_VISIBLE)
                .toList();
    }

    @Transactional
    public Feedback create(
            FeedbackTargetType targetType,
            UUID targetId,
            UUID authorUserId,
            FeedbackVisibility visibility,
            String bodyMarkdown,
            UUID actorId,
            String ip) {
        String sanitized = sanitizer.sanitizeRichText(bodyMarkdown);
        Feedback entry =
                new Feedback(
                        targetType,
                        targetId,
                        authorUserId,
                        visibility,
                        bodyMarkdown,
                        sanitized,
                        clock.instant());
        Feedback saved = feedback.save(entry);
        audit.record(
                AuditAction.FEEDBACK_CREATED,
                actorId,
                "feedback",
                saved.getId(),
                ip,
                null,
                Map.of("targetType", targetType.name(), "targetId", targetId.toString()));
        return saved;
    }

    @Transactional
    public Feedback update(
            UUID id, FeedbackVisibility visibility, String bodyMarkdown, UUID actorId, String ip) {
        Feedback entry = require(id);
        String sanitized = sanitizer.sanitizeRichText(bodyMarkdown);
        entry.update(visibility, bodyMarkdown, sanitized, clock.instant());
        audit.record(AuditAction.FEEDBACK_UPDATED, actorId, "feedback", id, ip, null);
        return entry;
    }
}
