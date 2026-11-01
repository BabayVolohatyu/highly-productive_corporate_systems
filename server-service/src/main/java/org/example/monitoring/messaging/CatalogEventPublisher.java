package org.example.monitoring.messaging;

import org.example.common.CatalogEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CatalogEventPublisher {

    private final OutboxRepository outbox;
    private final ApplicationEventPublisher events;

    public CatalogEventPublisher(OutboxRepository outbox, ApplicationEventPublisher events) {
        this.outbox = outbox;
        this.events = events;
    }

    @Transactional
    public void stage(String type, Long serverId, String hostname, String status) {
        OutboxMessage saved = outbox.save(OutboxMessage.pending(new CatalogEvent(
                UUID.randomUUID().toString(),
                type,
                serverId,
                hostname,
                status)));
        events.publishEvent(new OutboxCommitted(saved.getId()));
    }
}
