package org.example.monitoring.messaging;

import org.example.common.CatalogEvents;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogEventPublisherTest {

    @Test
    void stage_savesTheOutboxRowBeforePublishingTheCommitSignal() {
        OutboxRepository outbox = mock(OutboxRepository.class);
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        when(outbox.save(any(OutboxMessage.class))).thenAnswer(invocation -> {
            OutboxMessage row = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(row, "id", 15L);
            return row;
        });
        CatalogEventPublisher publisher = new CatalogEventPublisher(outbox, events);

        publisher.stage(CatalogEvents.REPORT_REQUESTED, 4L, "edge-1", "UP");

        verify(outbox).save(any(OutboxMessage.class));
        verify(events).publishEvent(new OutboxCommitted(15L));
    }
}
