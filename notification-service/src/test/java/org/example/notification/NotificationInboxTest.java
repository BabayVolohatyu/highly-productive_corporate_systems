package org.example.notification;

import org.example.common.CatalogEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationInboxTest {

    @Mock
    private StoredNotificationRepository repository;

    @InjectMocks
    private NotificationInbox inbox;

    @Test
    void accept_sameEventTwice_doesNotSaveAgain() {
        StoredNotification existing = StoredNotification.create("evt-1", "SERVER_REGISTERED edge-1 UP", Instant.parse("2024-01-01T00:00:00Z"));
        when(repository.findByEventId("evt-1")).thenReturn(Optional.empty(), Optional.of(existing));
        when(repository.save(any(StoredNotification.class))).thenReturn(existing);
        CatalogEvent event = new CatalogEvent("evt-1", "SERVER_REGISTERED", 4L, "edge-1", "UP");

        NotificationView first = inbox.accept(event);
        NotificationView second = inbox.accept(event);

        assertThat(first.eventId()).isEqualTo("evt-1");
        assertThat(second.message()).isEqualTo(first.message());
        ArgumentCaptor<StoredNotification> saved = ArgumentCaptor.forClass(StoredNotification.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getEventId()).isEqualTo("evt-1");
        verify(repository, never()).delete(any());
    }
}
