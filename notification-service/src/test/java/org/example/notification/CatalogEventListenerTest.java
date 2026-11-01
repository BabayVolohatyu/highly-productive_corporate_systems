package org.example.notification;

import org.example.common.CatalogEvent;
import org.example.common.CatalogEvents;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogEventListenerTest {

    @Test
    void drain_passesEachQueuedEventToTheInbox() {
        AmqpTemplate amqp = mock(AmqpTemplate.class);
        NotificationInbox inbox = mock(NotificationInbox.class);
        CatalogEvent first = new CatalogEvent("evt-1", CatalogEvents.SERVER_REGISTERED, 1L, "edge-1", "UP");
        CatalogEvent second = new CatalogEvent("evt-2", CatalogEvents.SERVER_STATUS_CHANGED, 1L, "edge-1", "DOWN");
        when(amqp.receiveAndConvert(CatalogEvents.QUEUE)).thenReturn(first, second, null);
        CatalogEventListener listener = new CatalogEventListener(amqp, inbox);

        listener.drain();

        verify(inbox).accept(first);
        verify(inbox).accept(second);
    }
}
