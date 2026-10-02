package uz.aifu.lms.assessment.domain;

/** THE multi-type extension point (V009) — dispatched by {@code QuestionGraderRegistry}. */
public enum QuestionType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    SHORT_ANSWER,
    NUMERIC,
    MATCHING,
    ORDERING
}
