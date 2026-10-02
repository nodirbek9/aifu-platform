package uz.aifu.lms.coding.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CodingTaskDetail;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CodingTaskStudentDetail;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.CodingTaskSummary;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.HintView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.LanguageTeacherView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.LanguageView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.TestCaseStudentView;
import uz.aifu.lms.coding.api.dto.CodingTaskDtos.TestCaseView;
import uz.aifu.lms.coding.domain.CodingTask;
import uz.aifu.lms.coding.domain.CodingTaskHint;
import uz.aifu.lms.coding.domain.CodingTaskHintTranslation;
import uz.aifu.lms.coding.domain.CodingTaskLanguage;
import uz.aifu.lms.coding.domain.CodingTaskTestCase;
import uz.aifu.lms.coding.domain.CodingTaskTranslation;

/**
 * Entity-to-DTO mapping for the coding task controllers. The student-facing methods are the
 * enforcement point for skills/coding-execution-security/SKILL.md's redaction rules: {@code
 * solutionCode} and {@code HIDDEN} test case stdin/expectedStdout never reach {@link
 * #toStudentDetail}.
 */
final class CodingTaskMapper {

    private CodingTaskMapper() {}

    private record TranslationResolution(
            String title,
            String description,
            String instructions,
            String constraintsText,
            String explanation,
            boolean fallback) {}

    /** Same three-step resolution as {@code assessment.api.QuizMapper} (ADR-005). */
    private static TranslationResolution resolve(
            List<CodingTaskTranslation> translations,
            String requestedLocale,
            String defaultLocale) {
        return translations.stream()
                .filter(t -> t.getLocale().equals(requestedLocale))
                .findFirst()
                .map(t -> toResolution(t, false))
                .or(
                        () ->
                                translations.stream()
                                        .filter(t -> t.getLocale().equals(defaultLocale))
                                        .findFirst()
                                        .map(t -> toResolution(t, true)))
                .or(() -> translations.stream().findFirst().map(t -> toResolution(t, true)))
                .orElse(new TranslationResolution(null, null, null, null, null, true));
    }

    private static TranslationResolution toResolution(CodingTaskTranslation t, boolean fallback) {
        return new TranslationResolution(
                t.getTitle(),
                t.getDescription(),
                t.getInstructions(),
                t.getConstraintsText(),
                t.getExplanation(),
                fallback);
    }

    static CodingTaskSummary toSummary(
            CodingTask task, List<CodingTaskTranslation> translations, String resolvedLocale) {
        TranslationResolution t =
                resolve(
                        translations,
                        resolvedLocale,
                        task.getCourse().getInstructionLanguage().code());
        return new CodingTaskSummary(
                task.getId(),
                task.getCourse().getId(),
                task.getCode(),
                task.getDifficulty(),
                task.getStatus(),
                task.getMaxPoints(),
                t.title(),
                t.fallback());
    }

    static CodingTaskDetail toDetail(
            CodingTask task,
            List<CodingTaskTranslation> translations,
            String resolvedLocale,
            List<CodingTaskLanguage> languages,
            List<CodingTaskHint> hints,
            Map<UUID, List<CodingTaskHintTranslation>> hintTranslations,
            List<CodingTaskTestCase> testCases) {
        TranslationResolution t =
                resolve(
                        translations,
                        resolvedLocale,
                        task.getCourse().getInstructionLanguage().code());
        return new CodingTaskDetail(
                task.getId(),
                task.getCourse().getId(),
                task.getCode(),
                task.getDifficulty(),
                task.getStatus(),
                task.getMaxPoints(),
                task.getScoringStrategy(),
                task.getMaxAttempts(),
                task.getCpuTimeLimitMs(),
                task.getWallTimeLimitMs(),
                task.getMemoryLimitKb(),
                task.getPublishedAt(),
                t.title(),
                t.description(),
                t.instructions(),
                t.constraintsText(),
                t.explanation(),
                t.fallback(),
                languages.stream().map(CodingTaskMapper::toLanguageTeacherView).toList(),
                hints.stream()
                        .map(
                                h ->
                                        toHintView(
                                                h,
                                                hintTranslations.getOrDefault(h.getId(), List.of()),
                                                resolvedLocale))
                        .toList(),
                testCases.stream().map(CodingTaskMapper::toTestCaseView).toList());
    }

    static CodingTaskStudentDetail toStudentDetail(
            CodingTask task,
            List<CodingTaskTranslation> translations,
            String resolvedLocale,
            List<CodingTaskLanguage> languages,
            List<CodingTaskTestCase> testCases) {
        TranslationResolution t =
                resolve(
                        translations,
                        resolvedLocale,
                        task.getCourse().getInstructionLanguage().code());
        List<TestCaseStudentView> examples =
                testCases.stream()
                        .filter(CodingTaskTestCase::isExample)
                        .map(CodingTaskMapper::toTestCaseStudentView)
                        .toList();
        return new CodingTaskStudentDetail(
                task.getId(),
                task.getCourse().getId(),
                task.getCode(),
                task.getDifficulty(),
                task.getMaxPoints(),
                task.getMaxAttempts(),
                task.getCpuTimeLimitMs(),
                task.getWallTimeLimitMs(),
                task.getMemoryLimitKb(),
                t.title(),
                t.description(),
                t.instructions(),
                t.constraintsText(),
                t.fallback(),
                languages.stream().map(CodingTaskMapper::toLanguageView).toList(),
                examples);
    }

    private static LanguageView toLanguageView(CodingTaskLanguage entry) {
        return new LanguageView(
                entry.getLanguage().getCode(),
                entry.getLanguage().getDisplayName(),
                entry.getLanguage().getVersionLabel(),
                entry.getStarterCode());
    }

    private static LanguageTeacherView toLanguageTeacherView(CodingTaskLanguage entry) {
        return new LanguageTeacherView(
                entry.getLanguage().getCode(),
                entry.getLanguage().getDisplayName(),
                entry.getLanguage().getVersionLabel(),
                entry.getStarterCode(),
                entry.getSolutionCode(),
                entry.getId());
    }

    private static HintView toHintView(
            CodingTaskHint hint,
            List<CodingTaskHintTranslation> translations,
            String resolvedLocale) {
        String body =
                translations.stream()
                        .filter(tr -> tr.getLocale().equals(resolvedLocale))
                        .findFirst()
                        .or(() -> translations.stream().findFirst())
                        .map(CodingTaskHintTranslation::getBody)
                        .orElse(null);
        return new HintView(hint.getId(), hint.getPosition(), hint.getPenaltyPercent(), body);
    }

    private static TestCaseView toTestCaseView(CodingTaskTestCase testCase) {
        return new TestCaseView(
                testCase.getId(),
                testCase.getPosition(),
                testCase.getName(),
                testCase.getVisibility(),
                testCase.isExample(),
                testCase.getWeight(),
                testCase.getStdin(),
                testCase.getExpectedStdout(),
                testCase.getComparisonMode(),
                testCase.getNumericTolerance(),
                testCase.getCpuTimeLimitMs());
    }

    private static TestCaseStudentView toTestCaseStudentView(CodingTaskTestCase testCase) {
        return new TestCaseStudentView(
                testCase.getId(),
                testCase.getPosition(),
                testCase.getName(),
                testCase.isExample(),
                testCase.getStdin(),
                testCase.getExpectedStdout());
    }
}
