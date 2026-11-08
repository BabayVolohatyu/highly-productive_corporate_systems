package org.example.monitoring.lookup;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperatorNoticeServiceTest {

    @Test
    void usesThePortsInsteadOfATransport() {
        AlertStatusLookup statusLookup = mock(AlertStatusLookup.class);
        AlertCountLookup countLookup = mock(AlertCountLookup.class);
        DeliveryReceiptLookup receiptLookup = mock(DeliveryReceiptLookup.class);
        when(statusLookup.findByHostname("edge-1")).thenReturn(new AlertStatus("edge-1", "UP", "registered"));
        when(countLookup.countByHostname("edge-1")).thenReturn(new AlertCount("edge-1", 2));
        when(receiptLookup.findByEventId("evt-1")).thenReturn(new DeliveryReceipt("evt-1", true, "registered"));
        OperatorNoticeService service = new OperatorNoticeService(statusLookup, countLookup, receiptLookup);

        assertThat(service.status("edge-1").status()).isEqualTo("UP");
        assertThat(service.count("edge-1").count()).isEqualTo(2);
        assertThat(service.receipt("evt-1").delivered()).isTrue();
        verify(statusLookup).findByHostname("edge-1");
        verify(countLookup).countByHostname("edge-1");
        verify(receiptLookup).findByEventId("evt-1");
    }
}
