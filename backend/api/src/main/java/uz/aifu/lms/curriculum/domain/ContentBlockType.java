package uz.aifu.lms.curriculum.domain;

/**
 * The multi-subject content extension point (brief §12): a lesson is never one giant text column.
 * Each value is handled by exactly one {@code ContentBlockPayloadValidator} in {@code
 * curriculum.app.content} — adding a type touches that enum + one handler + one test, never a
 * central switch statement (CLAUDE.md §14 DoD for this phase).
 */
public enum ContentBlockType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    CODE_EXAMPLE,
    CALLOUT,
    DIAGRAM,
    TABLE,
    CASE_STUDY,
    FILE,
    LINK,
    EMBED,
    QUIZ_REF,
    CODING_TASK_REF,
    ACTIVITY_REF,
    /** The teacher's plan of the class (V024). Always {@code TEACHER_ONLY}. */
    LESSON_PLAN;

    /** Whether a block of this type may ever be shown to a student. */
    public boolean teacherOnly() {
        return this == LESSON_PLAN;
    }
}
