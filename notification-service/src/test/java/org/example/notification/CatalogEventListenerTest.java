package org.example.notification;

import org.example.common.CatalogEvent;
import org.example.common.CatalogEvents;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.client.AmqpClient;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogEventListenerTest {

    @Test
    void drain_passesEachQueuedEventToTheInbox() {
        AmqpClient client = mock(AmqpClient.class);
        AmqpClient.ReceiveSpec receive = mock(AmqpClient.ReceiveSpec.class);
        NotificationInbox inbox = mock(NotificationInbox.class);
        CatalogEvent first = new CatalogEvent("evt-1", CatalogEvents.SERVER_REGISTERED, 1L, "edge-1", "UP");
        CatalogEvent second = new CatalogEvent("evt-2", CatalogEvents.SERVER_STATUS_CHANGED, 1L, "edge-1", "DOWN");
        when(client.from(CatalogEvents.amqpAddress())).thenReturn(receive);
        when(receive.timeout(any(Duration.class))).thenReturn(receive);
        when(receive.receiveAndConvert()).thenReturn(
                CompletableFuture.completedFuture(first),
                CompletableFuture.completedFuture(second),
                CompletableFuture.completedFuture(null));
        CatalogEventListener listener = new CatalogEventListener(client, inbox);

        listener.drain();

        verify(inbox).accept(first);
        verify(inbox).accept(second);
    }
}
