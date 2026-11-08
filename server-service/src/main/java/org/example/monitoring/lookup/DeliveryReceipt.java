package org.example.monitoring.lookup;

public record DeliveryReceipt(String eventId, boolean delivered, String message) {
}
