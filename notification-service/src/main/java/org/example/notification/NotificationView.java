package org.example.notification;

import java.time.Instant;

public record NotificationView(Long id, String eventId, String message, Instant createdAt) {

    public static NotificationView from(StoredNotification row) {
        return new NotificationView(row.getId(), row.getEventId(), row.getMessage(), row.getCreatedAt());
    }
}
