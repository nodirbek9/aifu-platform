package uz.aifu.lms.common.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aifu.api")
public record ApiProperties(
        @NotBlank String basePath,
        @Min(1) @Max(500) int maxPageSize,
        @Min(1) @Max(100) int defaultPageSize) {}
