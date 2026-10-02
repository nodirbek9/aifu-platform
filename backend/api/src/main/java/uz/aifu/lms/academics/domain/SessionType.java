package uz.aifu.lms.academics.domain;

/** "ma'ruza / seminar / amaliy / laboratoriya / mustaqil ta'lim / kurs ishi". */
public enum SessionType {
    LECTURE,
    SEMINAR,
    PRACTICAL,
    /**
     * Declared, never allocated hours in either supplied syllabus. Keep it — PENDING-INPUTS §11.
     */
    LABORATORY,
    INDEPENDENT_STUDY,
    COURSE_WORK
}
