package org.example.monitoring.messaging;

import org.example.common.CatalogEvents;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
public class OutboxDispatcher {

    private final OutboxRepository outbox;
    private final AmqpTemplate rabbitTemplate;

    public OutboxDispatcher(OutboxRepository outbox, AmqpTemplate rabbitTemplate) {
        this.outbox = outbox;
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void onCommit(OutboxCommitted committed) {
        outbox.findById(committed.id()).ifPresent(row -> {
            if (row.getPublishedAt() != null) {
                return;
            }
            rabbitTemplate.convertAndSend(CatalogEvents.QUEUE, row.toEvent());
            row.markPublished(Instant.now());
            outbox.save(row);
        });
    }
}
