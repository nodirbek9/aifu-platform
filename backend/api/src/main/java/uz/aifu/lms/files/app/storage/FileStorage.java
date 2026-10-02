package uz.aifu.lms.files.app.storage;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.Resource;

/**
 * The storage port (ADR-008). {@code provider} on {@code StoredFile} records which adapter wrote a
 * given row; the bean actually wired at runtime is selected by {@code aifu.storage.provider}
 * (application.yml), matching how {@code execution}'s {@code SandboxEngine} port is selected.
 */
public interface FileStorage {

    /** Writes {@code content} under {@code storageKey}, which the caller has already generated. */
    void write(String storageKey, InputStream content, long sizeBytes) throws IOException;

    Resource read(String storageKey) throws IOException;

    void delete(String storageKey) throws IOException;
}
