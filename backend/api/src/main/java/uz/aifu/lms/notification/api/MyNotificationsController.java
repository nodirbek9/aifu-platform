package uz.aifu.lms.notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.api.PageResponse;
import uz.aifu.lms.common.api.PageableSupport;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.notification.api.dto.NotificationDtos.NotificationView;
import uz.aifu.lms.notification.api.dto.NotificationDtos.UnreadCountView;
import uz.aifu.lms.notification.app.NotificationService;
import uz.aifu.lms.notification.domain.Notification;

/**
 * The caller's own notifications — every route here is self-scoped by the authenticated caller's
 * id, never a path parameter, so there is no ownership guard to forget (ADR-004 Layer 3: the SQL
 * predicate itself makes another user's notification unreachable).
 */
@RestController
@Tag(name = "Notifications", description = "The caller's own in-app notifications")
@SecurityRequirement(name = "bearerAuth")
public class MyNotificationsController {

    private final NotificationService notifications;
    private final PageableSupport pageableSupport;
    private final CurrentUserProvider currentUser;

    public MyNotificationsController(
            NotificationService notifications,
            PageableSupport pageableSupport,
            CurrentUserProvider currentUser) {
        this.notifications = notifications;
        this.pageableSupport = pageableSupport;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/v1/notifications/me")
    @PreAuthorize("hasAuthority('NOTIFICATION_SELF_READ')")
    @Operation(summary = "List the caller's own notifications, newest first")
    public PageResponse<NotificationView> listOwn(
            @RequestParam(defaultValue = "false") boolean unreadOnly, Pageable pageable) {
        UUID callerId = currentUser.require().id();
        var page = notifications.listOwn(callerId, unreadOnly, pageableSupport.sanitize(pageable));
        return PageResponse.of(page, MyNotificationsController::toView);
    }

    @GetMapping("/api/v1/notifications/me/unread-count")
    @PreAuthorize("hasAuthority('NOTIFICATION_SELF_READ')")
    @Operation(summary = "Unread notification count for the caller, for a badge/counter")
    public UnreadCountView unreadCount() {
        return new UnreadCountView(notifications.unreadCount(currentUser.require().id()));
    }

    @PostMapping("/api/v1/notifications/{id}/read")
    @PreAuthorize("hasAuthority('NOTIFICATION_SELF_READ')")
    @Operation(summary = "Mark one of the caller's own notifications read")
    public NotificationView markRead(@PathVariable UUID id) {
        return toView(notifications.markRead(id, currentUser.require().id()));
    }

    @PostMapping("/api/v1/notifications/me/read-all")
    @PreAuthorize("hasAuthority('NOTIFICATION_SELF_READ')")
    @Operation(summary = "Mark every unread notification of mine as read")
    public UnreadCountView markAllRead() {
        notifications.markAllRead(currentUser.require().id());
        return new UnreadCountView(0);
    }

    private static NotificationView toView(Notification n) {
        return new NotificationView(
                n.getId(),
                n.getType(),
                n.getChannel(),
                n.getStatus(),
                n.getPayload(),
                n.getCreatedAt(),
                n.getSentAt(),
                n.getReadAt());
    }
}
