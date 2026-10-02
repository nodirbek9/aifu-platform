package uz.aifu.lms.iam.app.access;

import java.util.Optional;
import java.util.UUID;

/**
 * Port: "which course owns this coding task?", to be implemented by {@code coding}'s adapter in
 * Phase 8 (ADR-013 — split out of {@link CurriculumLocator}). Registered now, like {@link
 * EnrollmentLookup}/{@link CurriculumLocator} were before their owning modules existed, so the
 * first class written in {@code coding} is already governed by this port instead of drifting.
 */
public interface CodingLocator {

    Optional<UUID> courseIdOfCodingTask(UUID codingTaskId);
}
