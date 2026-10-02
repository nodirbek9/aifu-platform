# ADR-008 — File storage: `FileStorage` port, local FS in dev, S3-compatible in prod

**Status:** Accepted

## Context
Lesson images, diagrams (both syllabi need flowcharts), videos, PDFs, downloadable materials,
and student file submissions (`FILE_SUBMISSION` activities). Brief §23: the domain must not
depend on a provider.

## Decision
A single port in `files.domain.port`:
```java
public interface FileStorage {
    StoredFile store(FileUploadCommand cmd);      // returns immutable storage key
    Optional<StoredFileContent> retrieve(String storageKey);
    Optional<URI> presignedUrl(String storageKey, Duration ttl);  // empty if unsupported
    void delete(String storageKey);
}
```
Adapters: `LocalFileSystemStorage` (dev/small prod; keys are content-addressed
`sha256/ab/cd/<hash>` so the tree never collides and dedupes for free) and `S3FileStorage`
(AWS SDK v2, works against MinIO, Wasabi, Backblaze B2, any S3-compatible endpoint).
Selected by `STORAGE_PROVIDER`. **MinIO is optional**, not a dependency — it is one possible
implementation of the same port, not an architectural commitment.

Metadata lives in `files` (id, storage_key, bucket, original_filename, content_type,
size_bytes, checksum_sha256, uploaded_by, visibility, status, created_at). The domain
references a `file_id`; it never sees a bucket name, a path, or a URL.

**Upload safety** (brief §39): max size (default 50 MB, configurable), extension **and**
sniffed-content-type allowlist (never trust the client's `Content-Type`), filename
sanitization (never use the client filename as a path), SHA-256 checksum, generated storage
keys only, SVG uploads either rejected or sanitized (SVG is a script vector), and downloads
served with `Content-Disposition: attachment` + `X-Content-Type-Options: nosniff`.
Access is authorized through the owning domain object — a `file_id` is never a bearer
capability. Presigned URLs are short-lived and only issued after that check.

Deletion is **soft** (`status = 'DELETED'`) with a separate reclaim job, so a mis-click cannot
destroy a published lesson's media, and `files` rows referenced by published content cannot
be reclaimed at all.

## Consequences
Dev needs no object storage. Production switches with env vars. Video streaming works via
presigned URLs on S3; the local adapter returns `Optional.empty()` and the API falls back to
streaming through the app (fine at this scale, documented as a limitation).
