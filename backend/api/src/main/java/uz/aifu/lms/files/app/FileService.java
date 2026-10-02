package uz.aifu.lms.files.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.aifu.lms.common.config.StorageProperties;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.files.app.storage.FileStorage;
import uz.aifu.lms.files.domain.FileProvider;
import uz.aifu.lms.files.domain.FileStatus;
import uz.aifu.lms.files.domain.FileVisibility;
import uz.aifu.lms.files.domain.StoredFile;
import uz.aifu.lms.files.persistence.StoredFileRepository;

/**
 * File upload/download/delete (V013, ADR-008). {@code files} may depend on nothing beyond {@code
 * common} (module map's one zero-cross-module-edge entry), so ownership/visibility checks here
 * compare bare {@code UUID}s and {@code common.security.AuthenticatedUser} facts — never an {@code
 * iam} type. Callers (controllers in other modules that embed an uploaded file) resolve their own
 * course/content ownership separately; this service only ever answers "may THIS caller read/delete
 * THIS file."
 */
@Service
public class FileService {

    private final StoredFileRepository files;
    private final FileStorage storage;
    private final StorageProperties properties;
    private final Clock clock;

    public FileService(
            StoredFileRepository files,
            FileStorage storage,
            StorageProperties properties,
            Clock clock) {
        this.files = files;
        this.storage = storage;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public StoredFile upload(MultipartFile upload, FileVisibility visibility, UUID uploaderId) {
        if (upload == null || upload.isEmpty()) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "File is empty");
        }
        if (upload.getSize() > properties.maxUploadBytes()) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED,
                    "File exceeds the maximum upload size of "
                            + properties.maxUploadBytes()
                            + " bytes");
        }
        String contentType = upload.getContentType();
        List<String> allowed = properties.allowedContentTypes();
        if (contentType == null
                || (allowed != null && !allowed.isEmpty() && !allowed.contains(contentType))) {
            throw Exceptions.rule(
                    ErrorCode.FILE_TYPE_NOT_ALLOWED, "Content type not allowed: " + contentType);
        }

        byte[] bytes;
        try {
            bytes = upload.getBytes();
        } catch (IOException e) {
            throw Exceptions.rule(
                    ErrorCode.STORAGE_UNAVAILABLE, "Could not read the uploaded file");
        }

        String checksum = sha256Hex(bytes);
        String storageKey =
                "uploads/" + UUID.randomUUID() + extensionOf(upload.getOriginalFilename());
        try {
            storage.write(storageKey, new ByteArrayInputStream(bytes), bytes.length);
        } catch (IOException e) {
            throw Exceptions.rule(ErrorCode.STORAGE_UNAVAILABLE, "Could not persist the file");
        }

        Instant now = clock.instant();
        StoredFile entry =
                new StoredFile(
                        storageKey,
                        FileProvider.LOCAL,
                        originalFilenameOf(upload),
                        contentType,
                        bytes.length,
                        checksum,
                        visibility,
                        uploaderId,
                        now);
        return files.save(entry);
    }

    @Transactional(readOnly = true)
    public StoredFile require(UUID id) {
        return files.findById(id).orElseThrow(() -> Exceptions.notFound("File", id));
    }

    /**
     * {@code PUBLIC} is readable by anyone reaching this (already-authenticated) endpoint; {@code
     * AUTHORIZED} likewise, since every caller here already holds a valid session; {@code PRIVATE}
     * is the uploader or an admin only.
     */
    public boolean canRead(StoredFile file, UUID callerId, boolean callerIsAdmin) {
        if (file.getStatus() != FileStatus.ACTIVE) {
            return false;
        }
        return switch (file.getVisibility()) {
            case PUBLIC, AUTHORIZED -> true;
            case PRIVATE ->
                    callerIsAdmin
                            || file.getUploadedBy() != null
                                    && file.getUploadedBy().equals(callerId);
        };
    }

    public Resource readContent(StoredFile file) {
        try {
            return storage.read(file.getStorageKey());
        } catch (IOException e) {
            throw Exceptions.rule(ErrorCode.STORAGE_UNAVAILABLE, "Could not read the stored file");
        }
    }

    @Transactional
    public void delete(UUID id) {
        StoredFile file = require(id);
        file.markDeleted(clock.instant());
    }

    private static String originalFilenameOf(MultipartFile upload) {
        String name = upload.getOriginalFilename();
        return (name == null || name.isBlank()) ? "file" : name;
    }

    private static String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        return (dot < 0 || dot == originalFilename.length() - 1)
                ? ""
                : originalFilename.substring(dot);
    }

    private static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the JDK spec", e);
        }
    }
}
