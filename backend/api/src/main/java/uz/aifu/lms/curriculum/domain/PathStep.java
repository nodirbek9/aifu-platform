package uz.aifu.lms.curriculum.domain;

/**
 * Distinguishes activities that serve as learning path gates (Phase 5, ADR-015):
 *
 * <ul>
 *   <li>{@link #TEST}: The lesson's gated comprehension quiz.
 *   <li>{@link #PRACTICE}: The lesson's practical coding task or checked-answer task.
 *   <li>{@link #MIDTERM}: The course-level midterm assessment.
 * </ul>
 */
public enum PathStep {
    TEST,
    PRACTICE,
    MIDTERM
}
