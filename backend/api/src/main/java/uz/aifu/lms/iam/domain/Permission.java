package uz.aifu.lms.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import uz.aifu.lms.common.domain.BaseEntity;

/** A single capability, e.g. {@code COURSE_CONTENT_WRITE}. Seeded by migration V003. */
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, updatable = false)
    private String code;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "domain", nullable = false)
    private String domain;

    protected Permission() {}

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getDomain() {
        return domain;
    }
}
