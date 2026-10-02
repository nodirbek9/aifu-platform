package uz.aifu.lms.curriculum.app.activity;

import java.util.Set;
import org.springframework.stereotype.Component;
import uz.aifu.lms.curriculum.domain.ActivityType;

/**
 * {@code MANUAL_TASK}, {@code CASE_STUDY}, {@code PRESENTATION}, {@code GROUP_PROJECT}, {@code
 * FILE_SUBMISSION}, {@code DISCUSSION}: the activity's own translation (title + instructions) is
 * the whole assignment — no {@code referenceId} into another module's content is needed.
 */
@Component
public class SelfContainedActivityTypeHandler implements ActivityTypeHandler {

    @Override
    public Set<ActivityType> supports() {
        return Set.of(
                ActivityType.MANUAL_TASK,
                ActivityType.CASE_STUDY,
                ActivityType.PRESENTATION,
                ActivityType.GROUP_PROJECT,
                ActivityType.FILE_SUBMISSION,
                ActivityType.DISCUSSION);
    }

    @Override
    public boolean requiresReference() {
        return false;
    }
}
