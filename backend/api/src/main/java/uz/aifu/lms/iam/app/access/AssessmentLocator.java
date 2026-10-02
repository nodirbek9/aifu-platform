package uz.aifu.lms.iam.app.access;

import java.util.Optional;
import java.util.UUID;

/**
 * Port: "which course owns this quiz?", implemented by {@code assessment}'s adapter (ADR-013 —
 * split out of {@link CurriculumLocator}, whose single-bean shape cannot have more than one
 * implementer).
 */
public interface AssessmentLocator {

    Optional<UUID> courseIdOfQuiz(UUID quizId);
}
