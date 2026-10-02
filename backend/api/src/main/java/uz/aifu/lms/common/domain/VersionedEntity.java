package uz.aifu.lms.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

/**
 * Adds optimistic locking. Use for anything two requests can edit at once: progress, enrollment,
 * course, lesson, submission, attempt. A conflict surfaces as {@code
 * ErrorCode.CONCURRENT_MODIFICATION}.
 */
@MappedSuperclass
public abstract class VersionedEntity extends BaseEntity {

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public long getVersion() {
        return version;
    }
}
