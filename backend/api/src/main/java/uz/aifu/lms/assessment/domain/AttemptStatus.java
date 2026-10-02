package uz.aifu.lms.assessment.domain;

public enum AttemptStatus {
    IN_PROGRESS,
    SUBMITTED,
    GRADED,
    EXPIRED,
    ABANDONED;

    public boolean isTerminal() {
        return this != IN_PROGRESS;
    }
}
