package org.example.notification;

import org.example.common.CatalogEvent;
import org.example.common.CatalogEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.client.AmqpClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletionException;

@Component
public class CatalogEventListener {

    private static final Logger log = LoggerFactory.getLogger(CatalogEventListener.class);

    private final AmqpClient amqpClient;
    private final NotificationInbox inbox;

    public CatalogEventListener(AmqpClient amqpClient, NotificationInbox inbox) {
        this.amqpClient = amqpClient;
        this.inbox = inbox;
    }

    @Scheduled(fixedDelay = 1000)
    public void drain() {
        CatalogEvent event = poll();
        while (event != null) {
            inbox.accept(event);
            event = poll();
        }
    }

    private CatalogEvent poll() {
        try {
            return amqpClient.from(CatalogEvents.amqpAddress())
                    .timeout(Duration.ofMillis(200))
                    .<CatalogEvent>receiveAndConvert()
                    .join();
        } catch (CompletionException exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            boolean empty = cause instanceof java.util.concurrent.TimeoutException
                    || cause instanceof NullPointerException;
            if (!empty) {
                log.warn("Could not read a catalog event: {}", cause.toString());
            }
            return null;
        }
    }
}
