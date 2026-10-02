package uz.aifu.lms.curriculum.app.activity;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import uz.aifu.lms.curriculum.domain.ActivityType;

/**
 * Same fail-fast completeness guarantee as {@code ContentBlockValidatorRegistry}: every {@link
 * ActivityType} must have exactly one handler, checked at application startup, not on first use.
 */
@Component
public class ActivityTypeHandlerRegistry {

    private final Map<ActivityType, ActivityTypeHandler> byType = new EnumMap<>(ActivityType.class);

    public ActivityTypeHandlerRegistry(List<ActivityTypeHandler> handlers) {
        for (ActivityTypeHandler handler : handlers) {
            for (ActivityType type : handler.supports()) {
                ActivityTypeHandler existing = byType.put(type, handler);
                if (existing != null) {
                    throw new IllegalStateException(
                            "Two ActivityTypeHandlers claim "
                                    + type
                                    + ": "
                                    + existing.getClass().getSimpleName()
                                    + " and "
                                    + handler.getClass().getSimpleName());
                }
            }
        }
        for (ActivityType type : ActivityType.values()) {
            if (!byType.containsKey(type)) {
                throw new IllegalStateException("No ActivityTypeHandler registered for " + type);
            }
        }
    }

    public ActivityTypeHandler forType(ActivityType type) {
        return byType.get(type);
    }
}
