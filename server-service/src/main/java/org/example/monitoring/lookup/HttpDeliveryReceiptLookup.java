package org.example.monitoring.lookup;

public class HttpDeliveryReceiptLookup implements DeliveryReceiptLookup {

    private final NotificationHttpApi api;

    public HttpDeliveryReceiptLookup(NotificationHttpApi api) {
        this.api = api;
    }

    @Override
    public DeliveryReceipt findByEventId(String eventId) {
        return RemoteErrors.call(() -> api.receipt(eventId));
    }
}
