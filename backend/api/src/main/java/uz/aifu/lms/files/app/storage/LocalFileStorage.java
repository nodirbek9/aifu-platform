package uz.aifu.lms.files.app.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.config.StorageProperties;

/**
 * Local-filesystem {@link FileStorage} adapter — the dev/demo default (ADR-008). {@code storageKey}
 * is always server-generated (never a client filename), so resolving it under {@code localRoot} can
 * never escape that root; there is no path-traversal input here to sanitize.
 */
@Component
@ConditionalOnProperty(
        prefix = "aifu.storage",
        name = "provider",
        havingValue = "local",
        matchIfMissing = true)
public class LocalFileStorage implements FileStorage {

    private final Path root;

    public LocalFileStorage(StorageProperties properties) throws IOException {
        this.root = Path.of(properties.localRoot()).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    @Override
    public void write(String storageKey, InputStream content, long sizeBytes) throws IOException {
        Path target = resolve(storageKey);
        Files.createDirectories(target.getParent());
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Resource read(String storageKey) {
        return new FileSystemResource(resolve(storageKey));
    }

    @Override
    public void delete(String storageKey) throws IOException {
        Files.deleteIfExists(resolve(storageKey));
    }

    private Path resolve(String storageKey) {
        return root.resolve(storageKey).normalize();
    }
}
