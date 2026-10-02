package uz.aifu.lms.assessment.domain;

/** How a free-text/numeric response is compared against {@link QuestionAnswerKey#expectedValue}. */
public enum MatchMode {
    EXACT,
    CASE_INSENSITIVE,
    TRIMMED,
    REGEX,
    NUMERIC_TOLERANCE
}
