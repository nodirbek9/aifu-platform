package uz.aifu.lms.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

/**
 * Identity, optimistic locking and an {@code updated_at} for tables that were deliberately built
 * without a {@code created_at} column (V008 progress tables: a row's insert moment is not a fact
 * worth tracking — only its most recent change is). {@link BaseEntity} always carries {@code
 * created_at}, so it does not fit here; this is a sibling, not a subclass.
 */
@MappedSuperclass
public abstract class UpdatableEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public UUID getId() {
        return id;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
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
        UUID otherId = ((UpdatableEntity) other).getId();
        return id != null && id.equals(otherId);
    }

    @Override
    public final int hashCode() {
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
