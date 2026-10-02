package uz.aifu.lms.files.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.common.util.Guards;

/**
 * File metadata (V013, ADR-008). Named {@code StoredFile}, not {@code File} — {@code java.io.File}
 * would shadow the JDK type in every import in this package.
 *
 * <p>{@code uploadedBy} is a bare {@code UUID}, not a {@code @ManyToOne User}: {@code files} may
 * depend on nothing beyond {@code common} (module map — the one module with zero allowed
 * cross-module edges), so it cannot hold a reference to {@code iam.domain.User} at all. Ownership
 * checks compare this id against {@code common.security.AuthenticatedUser.id()} directly — see
 * {@code FileService}.
 *
 * <p>Content is never trusted from the client: {@code storageKey} is server-generated (a random id,
 * never the client's filename) so it can never be used as a path-traversal vector, and {@code
 * originalFilename} is metadata only, never a path component (see the column comment on {@code
 * files.storage_key} in V013).
 */
@Entity
@Table(name = "files")
public class StoredFile {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "storage_key", nullable = false, updatable = false, unique = true)
    private String storageKey;

    @Column(name = "bucket", updatable = false)
    private String bucket;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, updatable = false)
    private FileProvider provider;

    @Column(name = "original_filename", nullable = false, updatable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, updatable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false, updatable = false)
    private long sizeBytes;

    @Column(name = "checksum_sha256", nullable = false, updatable = false)
    private String checksumSha256;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private FileVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FileStatus status;

    @Column(name = "uploaded_by", updatable = false)
    private UUID uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected StoredFile() {}

    public StoredFile(
            String storageKey,
            FileProvider provider,
            String originalFilename,
            String contentType,
            long sizeBytes,
            String checksumSha256,
            FileVisibility visibility,
            UUID uploadedBy,
            Instant now) {
        this.storageKey = Guards.required(storageKey, "storageKey");
        this.provider = Guards.required(provider, "provider");
        this.originalFilename = Guards.required(originalFilename, "originalFilename");
        this.contentType = Guards.required(contentType, "contentType");
        Guards.require(sizeBytes > 0, "sizeBytes must be > 0");
        this.sizeBytes = sizeBytes;
        this.checksumSha256 = Guards.required(checksumSha256, "checksumSha256");
        this.visibility = visibility == null ? FileVisibility.AUTHORIZED : visibility;
        this.status = FileStatus.ACTIVE;
        this.uploadedBy = uploadedBy;
        this.createdAt = Guards.required(now, "now");
    }

    public void markDeleted(Instant now) {
        if (status == FileStatus.DELETED) {
            return;
        }
        this.status = FileStatus.DELETED;
        this.deletedAt = Guards.required(now, "now");
    }

    public UUID getId() {
        return id;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public FileProvider getProvider() {
        return provider;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public FileVisibility getVisibility() {
        return visibility;
    }

    public FileStatus getStatus() {
        return status;
    }

    public UUID getUploadedBy() {
        return uploadedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
