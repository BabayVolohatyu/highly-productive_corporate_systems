package org.example.monitoring.messaging;

import org.example.common.CatalogEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.client.AmqpClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
public class OutboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    private final OutboxRepository outbox;
    private final AmqpClient amqpClient;

    public OutboxDispatcher(OutboxRepository outbox, AmqpClient amqpClient) {
        this.outbox = outbox;
        this.amqpClient = amqpClient;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCommit(OutboxCommitted committed) {
        outbox.findById(committed.id()).ifPresent(row -> {
            if (row.getPublishedAt() != null) {
                return;
            }
            try {
                amqpClient.to(CatalogEvents.amqpAddress()).body(row.toEvent()).durable(true).send().join();
                row.markPublished(Instant.now());
                outbox.save(row);
            } catch (RuntimeException exception) {
                log.warn("Could not publish catalog event {}: {}", row.toEvent().eventId(), exception.toString());
            }
        });
    }
}
