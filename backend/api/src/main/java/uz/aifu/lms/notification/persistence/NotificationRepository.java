package uz.aifu.lms.notification.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.aifu.lms.notification.domain.Notification;
import uz.aifu.lms.notification.domain.NotificationStatus;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(
            UUID recipientUserId, Pageable pageable);

    Page<Notification> findByRecipientUserIdAndStatusOrderByCreatedAtDesc(
            UUID recipientUserId, NotificationStatus status, Pageable pageable);

    long countByRecipientUserIdAndStatus(UUID recipientUserId, NotificationStatus status);

    @Modifying
    @Query(
            "update Notification n set n.status = uz.aifu.lms.notification.domain.NotificationStatus.READ,"
                    + " n.readAt = :now where n.recipientUserId = :recipient"
                    + " and n.status = uz.aifu.lms.notification.domain.NotificationStatus.SENT")
    int markAllRead(@Param("recipient") UUID recipientUserId, @Param("now") Instant now);

    /** Ownership-safe read — a notification id alone is never enough to read someone else's row. */
    Optional<Notification> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);
}
