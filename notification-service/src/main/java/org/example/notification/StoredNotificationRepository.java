package org.example.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoredNotificationRepository extends JpaRepository<StoredNotification, Long> {

    Optional<StoredNotification> findByEventId(String eventId);

    List<StoredNotification> findAllByOrderByIdAsc();
}
