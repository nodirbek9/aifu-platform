package uz.aifu.lms.announcement.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/** Request and response shapes of announcements. */
public final class AnnouncementDtos {

    private AnnouncementDtos() {}

    @Schema(
            description =
                    "Post an announcement to the students of a course OR of a group (exactly one of"
                            + " courseId and groupId).")
    public record CreateAnnouncementRequest(
            UUID courseId,
            UUID groupId,
            @NotBlank @Size(max = 200) String title,
            @Schema(description = "Rich text (HTML); sanitized on write")
                    @NotBlank
                    @Size(max = 20000)
                    String body) {}

    public record AnnouncementView(
            UUID id,
            UUID courseId,
            String subjectCode,
            String courseTitle,
            UUID groupId,
            String groupCode,
            UUID authorUserId,
            String authorName,
            String title,
            String body,
            Instant createdAt,
            @Schema(description = "true when the caller may delete it") boolean mine) {}
}
