package uz.aifu.lms.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalTime;
import uz.aifu.lms.common.domain.BaseEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;

/**
 * One "juftlik" (pair) of the bell schedule. {@code pairNo} is the pair's place in the whole day
 * and never changes once classes point at it; {@code shiftPairNo} is the number people use, the
 * pair's place within its {@link Shift}.
 */
@Entity
@Table(name = "pair_slots")
public class PairSlot extends BaseEntity {

    @Column(name = "pair_no", nullable = false, unique = true, updatable = false)
    private int pairNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = false)
    private Shift shift;

    @Column(name = "shift_pair_no", nullable = false)
    private int shiftPairNo;

    @Column(name = "starts_at", nullable = false)
    private LocalTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalTime endsAt;

    protected PairSlot() {}

    public PairSlot(
            int pairNo, Shift shift, int shiftPairNo, LocalTime startsAt, LocalTime endsAt) {
        if (pairNo < 1 || pairNo > 12) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "pairNo must be between 1 and 12");
        }
        this.pairNo = pairNo;
        update(shift, shiftPairNo, startsAt, endsAt);
    }

    public final void update(Shift shift, int shiftPairNo, LocalTime startsAt, LocalTime endsAt) {
        if (shift == null) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "shift is required");
        }
        if (shiftPairNo < 1 || shiftPairNo > 6) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "shiftPairNo must be between 1 and 6");
        }
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "endsAt must be after startsAt");
        }
        this.shift = shift;
        this.shiftPairNo = shiftPairNo;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public int getPairNo() {
        return pairNo;
    }

    public Shift getShift() {
        return shift;
    }

    public int getShiftPairNo() {
        return shiftPairNo;
    }

    public LocalTime getStartsAt() {
        return startsAt;
    }

    public LocalTime getEndsAt() {
        return endsAt;
    }
}
