package uz.aifu.lms.worker.domain;

import java.util.List;

/** Everything needed to run and grade one submission, loaded in one shot by {@code data}. */
public record SubmissionContext(
        SubmissionRecord submission,
        CodingTaskRecord task,
        LanguageRecord language,
        List<TestCaseRecord> testCases) {}
