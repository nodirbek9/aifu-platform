package uz.aifu.lms.execution.domain;

/**
 * {@code execution_test_results.verdict}'s own value set (V011) — distinct from {@code
 * coding.domain.Verdict}, which is the submission-level rollup.
 */
public enum TestVerdict {
    ACCEPTED,
    WRONG_ANSWER,
    RUNTIME_ERROR,
    TIME_LIMIT_EXCEEDED,
    MEMORY_LIMIT_EXCEEDED,
    OUTPUT_LIMIT_EXCEEDED,
    SKIPPED,
    INTERNAL_ERROR
}
