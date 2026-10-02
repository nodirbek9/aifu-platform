package uz.aifu.lms.common.domain;

/**
 * Shared publication lifecycle for authored academic content.
 *
 * <p>Transition to {@link #PUBLISHED} is never a bare flag flip: it runs the owning module's
 * publication validator (translation completeness, required children, hour reconciliation).
 */
public enum PublicationStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED;

    public boolean isVisibleToStudents() {
        return this == PUBLISHED;
    }

    public boolean canTransitionTo(PublicationStatus target) {
        return switch (this) {
            case DRAFT -> target == PUBLISHED || target == ARCHIVED;
            case PUBLISHED -> target == DRAFT || target == ARCHIVED;
            case ARCHIVED -> target == DRAFT;
        };
    }
}
