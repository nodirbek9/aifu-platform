package uz.aifu.lms.notification.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.notification.domain.Notification;
import uz.aifu.lms.notification.domain.NotificationStatus;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(
            UUID recipientUserId, Pageable pageable);

    Page<Notification> findByRecipientUserIdAndStatusOrderByCreatedAtDesc(
            UUID recipientUserId, NotificationStatus status, Pageable pageable);

    long countByRecipientUserIdAndStatus(UUID recipientUserId, NotificationStatus status);

    /** Ownership-safe read — a notification id alone is never enough to read someone else's row. */
    Optional<Notification> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);
}
