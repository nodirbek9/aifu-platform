package uz.aifu.lms.coding.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Reference data (V010) — rows come from the migration/admin process, not the student/teacher API;
 * this entity is read-only from application code. {@code code} is the natural key (matches {@code
 * programming_languages.code text PRIMARY KEY}), not a generated UUID — the one non-UUID-keyed
 * entity in this codebase, because the schema itself makes the language code the identity.
 */
@Entity
@Table(name = "programming_languages")
public class ProgrammingLanguage {

    @Id
    @Column(name = "code", nullable = false, updatable = false)
    private String code;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "version_label", nullable = false)
    private String versionLabel;

    @Column(name = "executor_key", nullable = false)
    private String executorKey;

    @Column(name = "compile_required", nullable = false)
    private boolean compileRequired;

    @Column(name = "source_filename", nullable = false)
    private String sourceFilename;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProgrammingLanguage() {}

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public String getExecutorKey() {
        return executorKey;
    }

    public boolean isCompileRequired() {
        return compileRequired;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
