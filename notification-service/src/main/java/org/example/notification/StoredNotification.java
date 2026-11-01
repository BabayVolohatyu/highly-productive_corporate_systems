package org.example.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "stored_notification")
public class StoredNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String eventId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    protected StoredNotification() {
    }

    public static StoredNotification create(String eventId, String message, Instant createdAt) {
        StoredNotification row = new StoredNotification();
        row.eventId = eventId;
        row.message = message;
        row.createdAt = createdAt;
        return row;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
