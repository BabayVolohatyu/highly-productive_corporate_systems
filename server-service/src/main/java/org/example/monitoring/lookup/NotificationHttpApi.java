package org.example.monitoring.lookup;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "/api/lookup", accept = "application/json")
public interface NotificationHttpApi {

    @GetExchange("/alerts/{hostname}")
    AlertStatus status(@PathVariable String hostname);

    @GetExchange("/alerts/{hostname}/count")
    AlertCount count(@PathVariable String hostname);

    @GetExchange("/receipts/{eventId}")
    DeliveryReceipt receipt(@PathVariable String eventId);
}
