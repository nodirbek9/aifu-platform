package uz.aifu.lms.grading.api;

import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.grading.api.dto.GradeDtos.ComponentGradeView;
import uz.aifu.lms.grading.api.dto.GradeDtos.EnrollmentGradeSummary;
import uz.aifu.lms.grading.api.dto.GradeDtos.GradeEntryView;
import uz.aifu.lms.grading.api.dto.GradeDtos.StudentGradeSummary;
import uz.aifu.lms.grading.app.StudentGradebook;
import uz.aifu.lms.grading.domain.GradeEntry;

final class GradeMapper {

    private GradeMapper() {}

    static GradeEntryView toView(GradeEntry entry) {
        return toView(entry, null, null);
    }

    static GradeEntryView toView(GradeEntry entry, String title, String componentCode) {
        return new GradeEntryView(
                entry.getId(),
                entry.getAssessmentComponentId(),
                entry.getActivityId(),
                entry.getSource(),
                entry.getSourceRefType(),
                entry.getSourceRefId(),
                entry.getScore(),
                entry.getMaxScore(),
                entry.getStatus(),
                entry.getRevision(),
                entry.getSupersedesId(),
                entry.getVoidReason(),
                entry.getEvaluatedAt(),
                entry.getPublishedAt(),
                title,
                componentCode);
    }

    static EnrollmentGradeSummary toSummary(StudentGradebook book) {
        Enrollment enrollment = book.enrollment();
        return new EnrollmentGradeSummary(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getFinalScore(),
                enrollment.getFinalGradeSymbol(),
                enrollment.getAdmittedToFinal(),
                enrollment.isAcademicDebt(),
                enrollment.getFinalGradeAt(),
                book.complete(),
                book.components().stream()
                        .map(
                                c ->
                                        new ComponentGradeView(
                                                c.id(),
                                                c.parentId(),
                                                c.code(),
                                                c.kind(),
                                                c.maxPoints(),
                                                c.earned(),
                                                c.graded(),
                                                c.minPassPercent(),
                                                c.blocksFinal(),
                                                c.gatePassed()))
                        .toList(),
                book.current().stream().map(GradeMapper::toView).toList(),
                book.history().stream().map(GradeMapper::toView).toList());
    }

    private static GradeEntryView toView(StudentGradebook.EntryLine line) {
        return toView(line.entry(), line.title(), line.componentCode());
    }

    static StudentGradeSummary toStudentSummary(Enrollment enrollment) {
        return new StudentGradeSummary(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getFullName(),
                enrollment.getFinalScore(),
                enrollment.getFinalGradeSymbol(),
                enrollment.getAdmittedToFinal(),
                enrollment.isAcademicDebt());
    }
}
