package uz.aifu.lms.grading.app;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import uz.aifu.lms.academics.domain.AssessmentComponentKind;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.grading.domain.GradeEntry;

/**
 * One student's grades in one course as the grades page shows them: every component of the course's
 * assessment scheme with what the student has earned in it, the entries that count now, and every
 * entry there ever was (grades are append-only: a corrected or voided grade stays in the history).
 *
 * @param complete every top-level component has been graded, so the final score and its grade are
 *     the result of the course and not a running total
 */
public record StudentGradebook(
        Enrollment enrollment,
        List<ComponentLine> components,
        List<EntryLine> current,
        List<EntryLine> history,
        boolean complete) {

    /**
     * @param graded the component, or one of its children, has an entry that counts
     * @param gatePassed whether the earned share reaches the component's pass mark; {@code null}
     *     when it has no pass mark or nothing has been graded in it yet
     */
    public record ComponentLine(
            UUID id,
            UUID parentId,
            String code,
            AssessmentComponentKind kind,
            BigDecimal maxPoints,
            BigDecimal minPassPercent,
            boolean blocksFinal,
            BigDecimal earned,
            boolean graded,
            Boolean gatePassed) {}

    /**
     * @param title the graded work's title in the reader's language, when the entry came from one
     * @param componentCode the code of the component the entry counts towards
     */
    public record EntryLine(GradeEntry entry, String title, String componentCode) {}
}
