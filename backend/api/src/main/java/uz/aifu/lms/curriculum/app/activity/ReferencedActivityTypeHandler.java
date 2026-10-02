package uz.aifu.lms.curriculum.app.activity;

import java.util.Set;
import org.springframework.stereotype.Component;
import uz.aifu.lms.curriculum.domain.ActivityType;

/**
 * {@code QUIZ}, {@code CODING_TASK}: the graded artifact itself lives in {@code assessment}/{@code
 * coding} (Phases 7/8) and is pointed at via {@code referenceId}.
 */
@Component
public class ReferencedActivityTypeHandler implements ActivityTypeHandler {

    @Override
    public Set<ActivityType> supports() {
        return Set.of(ActivityType.QUIZ, ActivityType.CODING_TASK);
    }

    @Override
    public boolean requiresReference() {
        return true;
    }
}
