package org.example.monitoring.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.example.common.CatalogEvent;

import java.time.Instant;

@Entity
@Table(name = "outbox_message")
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(nullable = false, length = 64)
    private String type;

    @Column(nullable = false)
    private Long serverId;

    @Column(nullable = false)
    private String hostname;

    @Column(nullable = false, length = 64)
    private String status;

    private Instant publishedAt;

    protected OutboxMessage() {
    }

    public static OutboxMessage pending(CatalogEvent event) {
        OutboxMessage row = new OutboxMessage();
        row.eventId = event.eventId();
        row.type = event.type();
        row.serverId = event.serverId();
        row.hostname = event.hostname();
        row.status = event.status();
        return row;
    }

    public Long getId() {
        return id;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void markPublished(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public CatalogEvent toEvent() {
        return new CatalogEvent(eventId, type, serverId, hostname, status);
    }
}
