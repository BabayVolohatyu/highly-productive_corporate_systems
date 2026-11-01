package org.example.notification;

import org.example.common.CatalogEvents;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationRabbitConfig {

    @Bean
    Queue catalogEvents() {
        return QueueBuilder.durable(CatalogEvents.QUEUE).build();
    }
}
