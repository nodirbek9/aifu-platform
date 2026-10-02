package uz.aifu.lms.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** File storage settings (ADR-008). {@code provider} selects the {@code FileStorage} adapter. */
@ConfigurationProperties(prefix = "aifu.storage")
public record StorageProperties(
        String provider,
        String localRoot,
        long maxUploadBytes,
        String endpoint,
        String bucket,
        String accessKey,
        String secretKey,
        String region,
        List<String> allowedContentTypes) {}
