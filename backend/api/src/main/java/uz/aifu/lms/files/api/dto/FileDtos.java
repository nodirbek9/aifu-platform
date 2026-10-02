package uz.aifu.lms.files.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.files.domain.FileVisibility;

public final class FileDtos {

    private FileDtos() {}

    @Schema(description = "Uploaded file metadata")
    public record FileDetail(
            UUID id,
            String originalFilename,
            String contentType,
            long sizeBytes,
            String checksumSha256,
            FileVisibility visibility,
            Instant createdAt) {}
}
