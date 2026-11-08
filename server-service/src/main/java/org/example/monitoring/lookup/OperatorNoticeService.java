package org.example.monitoring.lookup;

import org.springframework.stereotype.Service;

@Service
public class OperatorNoticeService {

    private final AlertStatusLookup alertStatusLookup;
    private final AlertCountLookup alertCountLookup;
    private final DeliveryReceiptLookup deliveryReceiptLookup;

    public OperatorNoticeService(AlertStatusLookup alertStatusLookup,
                                 AlertCountLookup alertCountLookup,
                                 DeliveryReceiptLookup deliveryReceiptLookup) {
        this.alertStatusLookup = alertStatusLookup;
        this.alertCountLookup = alertCountLookup;
        this.deliveryReceiptLookup = deliveryReceiptLookup;
    }

    public AlertStatus status(String hostname) {
        return alertStatusLookup.findByHostname(hostname);
    }

    public AlertCount count(String hostname) {
        return alertCountLookup.countByHostname(hostname);
    }

    public DeliveryReceipt receipt(String eventId) {
        return deliveryReceiptLookup.findByEventId(eventId);
    }
}
