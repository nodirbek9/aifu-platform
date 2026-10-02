package uz.aifu.lms.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * A one-off change to a recurring class on one date: cancelled, moved to another date and pair, or
 * held with a replacement teacher and/or room.
 */
@Entity
@Table(name = "timetable_exceptions")
public class TimetableException {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entry_id", nullable = false, updatable = false)
    private TimetableEntry entry;

    @Column(name = "on_date", nullable = false)
    private LocalDate onDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private ExceptionKind kind;

    @Column(name = "new_date")
    private LocalDate newDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_pair_slot_id")
    private PairSlot newPairSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_room_id")
    private Room newRoom;

    @Column(name = "new_teacher_user_id")
    private UUID newTeacherUserId;

    @Column(name = "reason")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    protected TimetableException() {}

    public TimetableException(
            TimetableEntry entry,
            LocalDate onDate,
            ExceptionKind kind,
            LocalDate newDate,
            PairSlot newPairSlot,
            Room newRoom,
            UUID newTeacherUserId,
            String reason,
            UUID createdBy) {
        this.entry = Guards.required(entry, "entry");
        this.onDate = Guards.required(onDate, "onDate");
        this.kind = Guards.required(kind, "kind");
        if (kind == ExceptionKind.MOVED && (newDate == null || newPairSlot == null)) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "A moved class needs its new date and pair");
        }
        if (kind == ExceptionKind.REPLACED && newRoom == null && newTeacherUserId == null) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "A replacement needs a new teacher or a new room");
        }
        this.newDate = kind == ExceptionKind.MOVED ? newDate : null;
        this.newPairSlot = kind == ExceptionKind.MOVED ? newPairSlot : null;
        this.newRoom = kind == ExceptionKind.CANCELLED ? null : newRoom;
        this.newTeacherUserId = kind == ExceptionKind.CANCELLED ? null : newTeacherUserId;
        this.reason = (reason == null || reason.isBlank()) ? null : reason.trim();
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public TimetableEntry getEntry() {
        return entry;
    }

    public LocalDate getOnDate() {
        return onDate;
    }

    public ExceptionKind getKind() {
        return kind;
    }

    public LocalDate getNewDate() {
        return newDate;
    }

    public PairSlot getNewPairSlot() {
        return newPairSlot;
    }

    public Room getNewRoom() {
        return newRoom;
    }

    public UUID getNewTeacherUserId() {
        return newTeacherUserId;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
