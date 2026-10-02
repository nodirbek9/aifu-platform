package uz.aifu.lms.notification.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import uz.aifu.lms.notification.domain.NotificationChannel;
import uz.aifu.lms.notification.domain.NotificationStatus;
import uz.aifu.lms.notification.domain.NotificationType;

public final class NotificationDtos {

    private NotificationDtos() {}

    @Schema(description = "One in-app notification")
    public record NotificationView(
            UUID id,
            NotificationType type,
            NotificationChannel channel,
            NotificationStatus status,
            @Schema(description = "The raw payload; prefer `context`, which is the same, typed")
                    String payloadJson,
            NotificationContext context,
            Instant createdAt,
            Instant sentAt,
            Instant readAt) {}

    @Schema(
            description =
                    "What a notification is about: the ids to open it by and the words to show."
                            + " Which fields are present depends on the type —"
                            + " ENROLLMENT_CREATED: courseId, enrollmentId. ANNOUNCEMENT:"
                            + " announcementId, title, and courseId or groupId. GRADE_PUBLISHED:"
                            + " courseId, enrollmentId, finalScore, finalGradeSymbol."
                            + " TEACHER_FEEDBACK: courseId, targetType, targetId, and codingTaskId"
                            + " or quizId. TIMETABLE_CHANGED: courseId, entryId, kind, date,"
                            + " pairNo, and newDate, newPairNo, newRoom, reason when they apply")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NotificationContext(
            UUID courseId,
            UUID enrollmentId,
            UUID announcementId,
            UUID groupId,
            UUID quizId,
            UUID codingTaskId,
            String targetType,
            UUID targetId,
            String title,
            UUID entryId,
            String kind,
            LocalDate date,
            Integer pairNo,
            LocalDate newDate,
            Integer newPairNo,
            String newRoom,
            String reason,
            String finalScore,
            String finalGradeSymbol) {}

    @Schema(description = "Unread notification count for the caller")
    public record UnreadCountView(long count) {}
}
