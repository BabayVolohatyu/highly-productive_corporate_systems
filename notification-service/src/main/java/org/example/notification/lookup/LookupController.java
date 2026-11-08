package org.example.notification.lookup;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lookup")
public class LookupController {

    private final NotificationLookupService lookupService;

    public LookupController(NotificationLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/alerts/{hostname}")
    public AlertStatus status(@PathVariable String hostname) {
        return lookupService.requireStatus(hostname);
    }

    @GetMapping("/alerts/{hostname}/count")
    public AlertCount count(@PathVariable String hostname) {
        return lookupService.requireCount(hostname);
    }

    @GetMapping("/receipts/{eventId}")
    public DeliveryReceipt receipt(@PathVariable String eventId) {
        return lookupService.requireReceipt(eventId);
    }
}
