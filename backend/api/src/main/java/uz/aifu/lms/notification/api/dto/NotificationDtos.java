package uz.aifu.lms.notification.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
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
            String payloadJson,
            Instant createdAt,
            Instant sentAt,
            Instant readAt) {}

    @Schema(description = "Unread notification count for the caller")
    public record UnreadCountView(long count) {}
}
