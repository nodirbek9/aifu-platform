package uz.aifu.lms.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

/**
 * Identity, timestamps and id-based equality for every persistent entity.
 *
 * <p>Equality is id-based on purpose: field-based equality across lazy associations triggers
 * initialization and breaks {@code Set} semantics. Never override this with field equality, and
 * never include associations in {@code toString()}.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** True until the entity has been persisted. */
    public boolean isNew() {
        return id == null;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        Class<?> thisType = effectiveClass(this);
        Class<?> otherType = effectiveClass(other);
        if (!thisType.equals(otherType)) {
            return false;
        }
        UUID otherId = ((BaseEntity) other).getId();
        return id != null && id.equals(otherId);
    }

    @Override
    public final int hashCode() {
        // Stable across the transient -> persistent transition.
        return effectiveClass(this).hashCode();
    }

    private static Class<?> effectiveClass(Object candidate) {
        return candidate instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : candidate.getClass();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + "}";
    }
}
