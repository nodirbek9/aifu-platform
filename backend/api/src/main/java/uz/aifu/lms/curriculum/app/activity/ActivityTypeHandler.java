package uz.aifu.lms.curriculum.app.activity;

import java.util.Set;
import uz.aifu.lms.curriculum.domain.ActivityType;

/**
 * One handler per {@link ActivityType} (or a family of closely related ones) — "adding a new
 * subject's activity shape... is a new value here plus a handler, never a schema redesign" (V007
 * migration comment). Unlike {@code ContentBlockPayloadValidator}, an activity carries no
 * polymorphic payload of its own; what varies per type today is only whether the activity's actual
 * content lives elsewhere ({@link #requiresReference()}) — {@code assessment}/{@code coding}
 * (Phases 7/8) will extend this once they exist to resolve {@code referenceId} into real content.
 */
public interface ActivityTypeHandler {

    Set<ActivityType> supports();

    /**
     * Whether {@code activities.reference_id} must point at content defined in another module (true
     * for QUIZ/CODING_TASK) versus the activity being self-contained via its own translation (false
     * for MANUAL_TASK, CASE_STUDY, etc).
     */
    boolean requiresReference();
}
