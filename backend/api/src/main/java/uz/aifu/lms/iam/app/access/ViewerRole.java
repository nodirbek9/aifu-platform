package uz.aifu.lms.iam.app.access;

/** How the caller is related to a course. Drives content visibility and DTO shaping. */
public enum ViewerRole {
    /** Platform administrator: full read, no enrollment. */
    ADMIN,
    /** Assigned teacher of this course. Sees drafts, hidden tests, solutions, all students. */
    TEACHER,
    /** Enrolled student. Sees published content only, and only their own records. */
    STUDENT
}
