package uz.aifu.lms.worker.domain;

import java.util.UUID;

/**
 * The subset of one {@code submissions} row the worker needs (V010). Read-only: the worker writes
 * back only {@code status}/{@code verdict}/{@code score}/{@code maxScore}/{@code
 * testsPassed}/{@code testsTotal}/{@code completedAt}, via the column-level grant in V014 — never
 * {@code source_code}, {@code enrollment_id}, or {@code attempt_number}.
 */
public record SubmissionRecord(
        UUID id, UUID codingTaskId, String languageCode, String sourceCode, long version) {}
