package org.example.monitoring.messaging;

import org.example.common.CatalogEvent;
import org.example.common.CatalogEvents;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.client.AmqpClient;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxDispatcherTest {

    @Test
    void onCommit_publishesAfterCommitAndMarksTheRow() throws Exception {
        OutboxRepository outbox = mock(OutboxRepository.class);
        AmqpClient client = mock(AmqpClient.class);
        AmqpClient.SendSpec send = mock(AmqpClient.SendSpec.class);
        AmqpClient.SendSpec.ConvertAndSendSpec convert = mock(AmqpClient.SendSpec.ConvertAndSendSpec.class);
        when(client.to(CatalogEvents.amqpAddress())).thenReturn(send);
        when(send.body(any())).thenReturn(convert);
        when(convert.durable(true)).thenReturn(convert);
        when(convert.send()).thenReturn(CompletableFuture.completedFuture(true));
        OutboxMessage row = OutboxMessage.pending(new CatalogEvent("evt-1", CatalogEvents.SERVER_REGISTERED, 4L, "edge-1", "UP"));
        when(outbox.findById(9L)).thenReturn(Optional.of(row));
        OutboxDispatcher dispatcher = new OutboxDispatcher(outbox, client);

        dispatcher.onCommit(new OutboxCommitted(9L));

        verify(send).body(row.toEvent());
        verify(convert).send();
        assertThat(row.getPublishedAt()).isNotNull();
        verify(outbox).save(row);
        TransactionPhase phase = OutboxDispatcher.class
                .getMethod("onCommit", OutboxCommitted.class)
                .getAnnotation(TransactionalEventListener.class)
                .phase();
        assertThat(phase).isEqualTo(TransactionPhase.AFTER_COMMIT);
    }
}
