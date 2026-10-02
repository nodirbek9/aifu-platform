package uz.aifu.lms.curriculum.app.content;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import uz.aifu.lms.curriculum.domain.ContentBlockType;

/**
 * Dispatches to the {@link ContentBlockPayloadValidator} registered for a given {@link
 * ContentBlockType}. Fails FAST at application startup — not on the first request for a forgotten
 * type — if any {@link ContentBlockType} has no validator or two validators claim the same type, so
 * "adding a block type" cannot silently ship half-done.
 */
@Component
public class ContentBlockValidatorRegistry {

    private final Map<ContentBlockType, ContentBlockPayloadValidator> byType =
            new EnumMap<>(ContentBlockType.class);

    public ContentBlockValidatorRegistry(List<ContentBlockPayloadValidator> validators) {
        for (ContentBlockPayloadValidator validator : validators) {
            for (ContentBlockType type : validator.supports()) {
                ContentBlockPayloadValidator existing = byType.put(type, validator);
                if (existing != null) {
                    throw new IllegalStateException(
                            "Two ContentBlockPayloadValidators claim "
                                    + type
                                    + ": "
                                    + existing.getClass().getSimpleName()
                                    + " and "
                                    + validator.getClass().getSimpleName());
                }
            }
        }
        for (ContentBlockType type : ContentBlockType.values()) {
            if (!byType.containsKey(type)) {
                throw new IllegalStateException(
                        "No ContentBlockPayloadValidator registered for " + type);
            }
        }
    }

    public ContentBlockPayloadValidator forType(ContentBlockType type) {
        return byType.get(type);
    }
}
