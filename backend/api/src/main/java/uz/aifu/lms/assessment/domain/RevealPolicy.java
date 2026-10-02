package uz.aifu.lms.assessment.domain;

/**
 * Controls when correct answers/explanations may be serialized to a student (V009 column comment).
 * The student DTO mapper honours this; it is never a client-side concern.
 */
public enum RevealPolicy {
    NEVER,
    IMMEDIATELY,
    AFTER_ATTEMPT,
    AFTER_LAST_ATTEMPT,
    AFTER_DUE_DATE
}
