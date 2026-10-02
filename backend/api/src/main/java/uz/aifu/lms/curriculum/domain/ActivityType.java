package uz.aifu.lms.curriculum.domain;

/**
 * "Anything a student DOES is an activity" (V007 migration) — THE multi-subject extension point.
 * Adding a new subject's activity shape (e.g. a Cybersecurity case study) is a new value here plus
 * a handler, never a schema redesign. No code anywhere may branch on a subject code or name.
 */
public enum ActivityType {
    QUIZ,
    CODING_TASK,
    MANUAL_TASK,
    CASE_STUDY,
    PRESENTATION,
    GROUP_PROJECT,
    FILE_SUBMISSION,
    DISCUSSION
}
