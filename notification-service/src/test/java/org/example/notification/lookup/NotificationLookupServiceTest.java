package org.example.notification.lookup;

import org.example.notification.StoredNotification;
import org.example.notification.StoredNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationLookupServiceTest {

    @Mock
    private StoredNotificationRepository repository;

    @InjectMocks
    private NotificationLookupService lookupService;

    @Test
    void requireStatus_missingHostname_throwsNotFound() {
        when(repository.findFirstByHostnameOrderByIdDesc("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookupService.requireStatus("missing"))
                .isInstanceOf(LookupNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void requireCount_returnsTheStoredTotal() {
        when(repository.countByHostname("edge-1")).thenReturn(3);

        assertThat(lookupService.requireCount("edge-1").count()).isEqualTo(3);
    }

    @Test
    void requireReceipt_returnsTheStoredEvent() {
        when(repository.findByEventId("evt-1")).thenReturn(Optional.of(
                StoredNotification.create("evt-1", "SERVER_REGISTERED edge-1 UP", "edge-1", "UP", Instant.parse("2024-01-01T00:00:00Z"))));

        DeliveryReceipt receipt = lookupService.requireReceipt("evt-1");

        assertThat(receipt.delivered()).isTrue();
        assertThat(receipt.eventId()).isEqualTo("evt-1");
    }
}
