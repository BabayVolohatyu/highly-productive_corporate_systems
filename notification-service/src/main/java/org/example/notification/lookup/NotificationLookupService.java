package org.example.notification.lookup;

import org.example.notification.StoredNotification;
import org.example.notification.StoredNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationLookupService {

    private final StoredNotificationRepository repository;

    public NotificationLookupService(StoredNotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AlertStatus requireStatus(String hostname) {
        StoredNotification row = repository.findFirstByHostnameOrderByIdDesc(hostname)
                .orElseThrow(() -> new LookupNotFoundException("No alert for " + hostname));
        return new AlertStatus(row.getHostname(), row.getStatus(), row.getMessage());
    }

    @Transactional(readOnly = true)
    public AlertCount requireCount(String hostname) {
        int count = repository.countByHostname(hostname);
        if (count == 0) {
            throw new LookupNotFoundException("No alerts for " + hostname);
        }
        return new AlertCount(hostname, count);
    }

    @Transactional(readOnly = true)
    public DeliveryReceipt requireReceipt(String eventId) {
        StoredNotification row = repository.findByEventId(eventId)
                .orElseThrow(() -> new LookupNotFoundException("No receipt for " + eventId));
        return new DeliveryReceipt(row.getEventId(), true, row.getMessage());
    }
}
