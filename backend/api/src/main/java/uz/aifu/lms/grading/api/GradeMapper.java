package uz.aifu.lms.grading.api;

import java.util.List;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.grading.api.dto.GradeDtos.EnrollmentGradeSummary;
import uz.aifu.lms.grading.api.dto.GradeDtos.GradeEntryView;
import uz.aifu.lms.grading.api.dto.GradeDtos.StudentGradeSummary;
import uz.aifu.lms.grading.domain.GradeEntry;

final class GradeMapper {

    private GradeMapper() {}

    static GradeEntryView toView(GradeEntry entry) {
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
                entry.getPublishedAt());
    }

    static EnrollmentGradeSummary toSummary(Enrollment enrollment, List<GradeEntry> entries) {
        return new EnrollmentGradeSummary(
                enrollment.getId(),
                enrollment.getFinalScore(),
                enrollment.getFinalGradeSymbol(),
                enrollment.getAdmittedToFinal(),
                enrollment.isAcademicDebt(),
                enrollment.getFinalGradeAt(),
                entries.stream().map(GradeMapper::toView).toList());
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
