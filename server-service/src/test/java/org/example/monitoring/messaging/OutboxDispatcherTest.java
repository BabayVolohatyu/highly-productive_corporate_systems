package org.example.monitoring.messaging;

import org.example.common.CatalogEvent;
import org.example.common.CatalogEvents;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxDispatcherTest {

    @Test
    void onCommit_publishesAfterCommitAndMarksTheRow() throws Exception {
        OutboxRepository outbox = mock(OutboxRepository.class);
        AmqpTemplate rabbit = mock(AmqpTemplate.class);
        OutboxMessage row = OutboxMessage.pending(new CatalogEvent("evt-1", CatalogEvents.SERVER_REGISTERED, 4L, "edge-1", "UP"));
        when(outbox.findById(9L)).thenReturn(Optional.of(row));
        OutboxDispatcher dispatcher = new OutboxDispatcher(outbox, rabbit);

        dispatcher.onCommit(new OutboxCommitted(9L));

        verify(rabbit).convertAndSend(CatalogEvents.QUEUE, row.toEvent());
        assertThat(row.getPublishedAt()).isNotNull();
        verify(outbox).save(row);
        TransactionPhase phase = OutboxDispatcher.class
                .getMethod("onCommit", OutboxCommitted.class)
                .getAnnotation(TransactionalEventListener.class)
                .phase();
        assertThat(phase).isEqualTo(TransactionPhase.AFTER_COMMIT);
    }
}
