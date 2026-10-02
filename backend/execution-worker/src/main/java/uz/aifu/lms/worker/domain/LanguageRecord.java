package uz.aifu.lms.worker.domain;

/** One {@code programming_languages} row (V010). */
public record LanguageRecord(
        String code, String executorKey, boolean compileRequired, String sourceFilename) {}
