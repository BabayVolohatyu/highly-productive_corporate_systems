package org.example.monitoring.lookup;

public interface DeliveryReceiptLookup {

    DeliveryReceipt findByEventId(String eventId);
}
