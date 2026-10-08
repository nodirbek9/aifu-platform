package uz.aifu.lms.assessment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class AttemptGrantDtos {

    private AttemptGrantDtos() {}

    @Schema(description = "Request payload to grant an extra attempt")
    public record AttemptGrantRequest(
            @NotBlank
                    @Size(min = 5, max = 1000)
                    @Schema(
                            description = "Reason for the grant",
                            example = "Student experienced a power outage")
                    String reason) {}

    @Schema(description = "Representation of a granted extra attempt")
    public record AttemptGrantView(
            @Schema(description = "Grant ID") UUID id,
            @Schema(description = "Quiz ID") UUID quizId,
            @Schema(description = "Enrollment ID") UUID enrollmentId,
            @Schema(description = "Admin user ID who granted it") UUID grantedBy,
            @Schema(description = "Reason for the grant") String reason,
            @Schema(description = "When the grant was created") Instant createdAt) {}
}
