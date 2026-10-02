package uz.aifu.lms.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalTime;
import uz.aifu.lms.common.domain.BaseEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;

/** One "juftlik" (pair) of the bell schedule. */
@Entity
@Table(name = "pair_slots")
public class PairSlot extends BaseEntity {

    @Column(name = "pair_no", nullable = false, unique = true)
    private int pairNo;

    @Column(name = "starts_at", nullable = false)
    private LocalTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalTime endsAt;

    protected PairSlot() {}

    public PairSlot(int pairNo, LocalTime startsAt, LocalTime endsAt) {
        update(pairNo, startsAt, endsAt);
    }

    public final void update(int pairNo, LocalTime startsAt, LocalTime endsAt) {
        if (pairNo < 1 || pairNo > 12) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "pairNo must be between 1 and 12");
        }
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "endsAt must be after startsAt");
        }
        this.pairNo = pairNo;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public int getPairNo() {
        return pairNo;
    }

    public LocalTime getStartsAt() {
        return startsAt;
    }

    public LocalTime getEndsAt() {
        return endsAt;
    }
}
