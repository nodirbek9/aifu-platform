package uz.aifu.lms.execution.domain;

/**
 * Outcome of an ungraded run (V022). A CUSTOM run that finished normally is {@link #OK} — there is
 * nothing to compare its output with. A run against tests is {@link #ACCEPTED} when every test
 * passed, otherwise it carries the verdict of the first failing test.
 */
public enum CodeRunVerdict {
    OK,
    ACCEPTED,
    WRONG_ANSWER,
    COMPILATION_ERROR,
    RUNTIME_ERROR,
    TIME_LIMIT_EXCEEDED,
    MEMORY_LIMIT_EXCEEDED,
    OUTPUT_LIMIT_EXCEEDED,
    INTERNAL_ERROR
}
