package uz.aifu.lms.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.common.domain.AuditableEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * The timetable ("dars jadvali") of one term. Students and teachers read the single PUBLISHED one;
 * drafts are the admin's workspace.
 */
@Entity
@Table(name = "timetables")
public class Timetable extends AuditableEntity {

    @Column(name = "term_id", nullable = false, updatable = false)
    private UUID termId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TimetableStatus status = TimetableStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by")
    private UUID publishedBy;

    protected Timetable() {}

    public Timetable(UUID termId, String name) {
        this.termId = Guards.required(termId, "termId");
        this.name = Guards.requiredText(name, "name").trim();
    }

    public void rename(String name) {
        this.name = Guards.requiredText(name, "name").trim();
    }

    public void publish(UUID actorId, Instant now) {
        if (status == TimetableStatus.ARCHIVED) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION,
                    "An archived timetable cannot be published");
        }
        this.status = TimetableStatus.PUBLISHED;
        this.publishedAt = now;
        this.publishedBy = actorId;
    }

    public void archive() {
        this.status = TimetableStatus.ARCHIVED;
    }

    /** Entries of an archived timetable are history and may not change. */
    public void requireEditable() {
        if (status == TimetableStatus.ARCHIVED) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION, "An archived timetable cannot be edited");
        }
    }

    public UUID getTermId() {
        return termId;
    }

    public String getName() {
        return name;
    }

    public TimetableStatus getStatus() {
        return status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public UUID getPublishedBy() {
        return publishedBy;
    }
}
