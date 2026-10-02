package uz.aifu.lms.iam.app.access;

import java.util.Optional;
import java.util.UUID;

/**
 * Port: "which course owns this content object?", implemented by {@code curriculum}'s single
 * adapter.
 *
 * <p>Authorization for any nested object always resolves up to a course id and then reuses the
 * single course-access decision. This is the mechanism that keeps the rule "one consistent security
 * model" (brief §9) true instead of aspirational.
 *
 * <p>Quiz/coding-task lookups live on their own ports ({@link AssessmentLocator}, {@link
 * CodingLocator}), not here — ADR-013: a single {@code Optional<CurriculumLocator>} bean can have
 * only one implementer, and {@code curriculum} has no `quizzes`/`coding_tasks` tables to answer
 * those questions from anyway.
 */
public interface CurriculumLocator {

    Optional<UUID> courseIdOfModule(UUID moduleId);

    Optional<UUID> courseIdOfLesson(UUID lessonId);

    Optional<UUID> courseIdOfActivity(UUID activityId);
}
