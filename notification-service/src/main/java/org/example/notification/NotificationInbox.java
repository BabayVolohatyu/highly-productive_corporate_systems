package org.example.notification;

import org.example.common.CatalogEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationInbox {

    private final StoredNotificationRepository repository;

    public NotificationInbox(StoredNotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NotificationView accept(CatalogEvent event) {
        return repository.findByEventId(event.eventId())
                .map(NotificationView::from)
                .orElseGet(() -> NotificationView.from(repository.save(StoredNotification.create(
                        event.eventId(),
                        event.type() + " " + event.hostname() + " " + event.status(),
                        Instant.now()))));
    }

    @Transactional
    public NotificationView record(String message) {
        StoredNotification saved = repository.save(StoredNotification.create(
                "local-" + System.nanoTime(),
                message,
                Instant.now()));
        return NotificationView.from(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationView> list() {
        return repository.findAllByOrderByIdAsc().stream().map(NotificationView::from).toList();
    }
}
