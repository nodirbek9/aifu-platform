package uz.aifu.lms.scheduling.domain;

/** Which weeks a recurring class is held in ("toq" = ODD, "juft" = EVEN). */
public enum WeekParity {
    EVERY,
    ODD,
    EVEN;

    /** Two classes can meet in the same week unless one is ODD-only and the other EVEN-only. */
    public boolean overlaps(WeekParity other) {
        return this == EVERY || other == EVERY || this == other;
    }

    /** Whether a class with this parity is held in a week of the given concrete parity. */
    public boolean includes(WeekParity concreteWeek) {
        return this == EVERY || this == concreteWeek;
    }

    public WeekParity flipped() {
        return switch (this) {
            case ODD -> EVEN;
            case EVEN -> ODD;
            case EVERY -> EVERY;
        };
    }
}
