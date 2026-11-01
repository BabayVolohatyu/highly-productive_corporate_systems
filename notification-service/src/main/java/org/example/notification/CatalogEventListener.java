package org.example.notification;

import org.example.common.CatalogEvent;
import org.example.common.CatalogEvents;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CatalogEventListener {

    private final AmqpTemplate amqpTemplate;
    private final NotificationInbox inbox;

    public CatalogEventListener(AmqpTemplate amqpTemplate, NotificationInbox inbox) {
        this.amqpTemplate = amqpTemplate;
        this.inbox = inbox;
    }

    @Scheduled(fixedDelay = 1000)
    public void drain() {
        Object message = amqpTemplate.receiveAndConvert(CatalogEvents.QUEUE);
        while (message instanceof CatalogEvent event) {
            inbox.accept(event);
            message = amqpTemplate.receiveAndConvert(CatalogEvents.QUEUE);
        }
    }
}
