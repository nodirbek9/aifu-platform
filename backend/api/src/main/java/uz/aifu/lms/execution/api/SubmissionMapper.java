package uz.aifu.lms.execution.api;

import java.util.List;
import java.util.Optional;
import uz.aifu.lms.coding.domain.Submission;
import uz.aifu.lms.coding.domain.TestCaseVisibility;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.StudentSubmissionSummary;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.SubmissionDetail;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.SubmissionSummary;
import uz.aifu.lms.execution.api.dto.SubmissionDtos.TestResultView;
import uz.aifu.lms.execution.domain.ExecutionResult;
import uz.aifu.lms.execution.domain.ExecutionTestResult;

/**
 * Entity-to-DTO mapping for {@code execution.api}'s submission controllers. {@link #toDetail}'s
 * {@code redactHidden} flag is the enforcement point for
 * skills/coding-execution-security/SKILL.md's "never echo hidden test-case output to a student"
 * rule — {@code true} for every student-facing caller, {@code false} only for a teacher with {@code
 * SUBMISSION_REVIEW} on the owning course.
 */
final class SubmissionMapper {

    private SubmissionMapper() {}

    static SubmissionDetail toDetail(
            Submission submission,
            Optional<ExecutionResult> result,
            List<ExecutionTestResult> testResults,
            boolean redactHidden) {
        List<TestResultView> views =
                testResults.stream().map(r -> toTestResultView(r, redactHidden)).toList();
        return new SubmissionDetail(
                submission.getId(),
                submission.getCodingTask().getId(),
                submission.getLanguage().getCode(),
                submission.getAttemptNumber(),
                submission.getSourceCode(),
                submission.getStatus(),
                submission.getVerdict(),
                submission.getScore(),
                submission.getMaxScore(),
                submission.getTestsPassed(),
                submission.getTestsTotal(),
                submission.getSubmittedAt(),
                submission.getCompletedAt(),
                result.map(ExecutionResult::getCompileOutput).orElse(null),
                result.map(ExecutionResult::isOutputTruncated).orElse(false),
                views);
    }

    private static TestResultView toTestResultView(ExecutionTestResult r, boolean redactHidden) {
        boolean hidden =
                redactHidden && r.getTestCase().getVisibility() == TestCaseVisibility.HIDDEN;
        return new TestResultView(
                r.getTestCase().getId(),
                r.getTestCase().getName(),
                r.getTestCase().isExample(),
                r.isPassed(),
                r.getVerdict().name(),
                r.getTimeMs(),
                r.getMemoryKb(),
                r.getPointsAwarded(),
                hidden ? null : r.getTestCase().getStdin(),
                hidden ? null : r.getTestCase().getExpectedStdout(),
                hidden ? null : r.getStdoutExcerpt(),
                hidden ? null : r.getStderrExcerpt());
    }

    static SubmissionSummary toSummary(Submission submission) {
        return new SubmissionSummary(
                submission.getId(),
                submission.getCodingTask().getId(),
                submission.getLanguage().getCode(),
                submission.getAttemptNumber(),
                submission.getStatus(),
                submission.getVerdict(),
                submission.getScore(),
                submission.getMaxScore(),
                submission.getSubmittedAt(),
                submission.getCompletedAt());
    }

    static StudentSubmissionSummary toStudentSummary(Submission submission) {
        return new StudentSubmissionSummary(
                submission.getId(),
                submission.getEnrollment().getStudent().getId(),
                submission.getEnrollment().getStudent().getFullName(),
                submission.getLanguage().getCode(),
                submission.getAttemptNumber(),
                submission.getStatus(),
                submission.getVerdict(),
                submission.getScore(),
                submission.getMaxScore(),
                submission.getSubmittedAt(),
                submission.getCompletedAt());
    }
}
