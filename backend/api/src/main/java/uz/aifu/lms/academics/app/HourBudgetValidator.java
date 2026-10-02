package uz.aifu.lms.academics.app;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.academics.domain.CourseSessionAllocation;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;

/**
 * Reconciles the declared per-session-type hour budget against {@code course.totalHours}. "Hours
 * must reconcile" (skills/educational-domain/SKILL.md) — warn on draft (returned as an {@link
 * Optional} mismatch description), block on publish (thrown).
 *
 * <p>This only reconciles the DECLARED budget against the course total for now. Reconciling against
 * actual lesson hours is {@code curriculum}'s job once Phase 5 exists — this validator is exactly
 * where that check will be added, not a new one.
 */
@Component
public class HourBudgetValidator {

    /**
     * @return a human-readable mismatch description, or empty when the budget reconciles.
     */
    public Optional<String> checkReconciliation(
            Course course, List<CourseSessionAllocation> allocations) {
        int allocated =
                allocations.stream().mapToInt(CourseSessionAllocation::getAllocatedHours).sum();
        if (allocated == course.getTotalHours()) {
            return Optional.empty();
        }
        return Optional.of(
                "Session allocations sum to "
                        + allocated
                        + "h but the course declares "
                        + course.getTotalHours()
                        + "h total");
    }

    public void validateForPublish(Course course, List<CourseSessionAllocation> allocations) {
        checkReconciliation(course, allocations)
                .ifPresent(
                        mismatch -> {
                            throw Exceptions.rule(
                                    ErrorCode.PUBLICATION_VALIDATION_FAILED, mismatch);
                        });
    }
}
