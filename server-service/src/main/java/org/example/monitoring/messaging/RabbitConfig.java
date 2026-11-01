package org.example.monitoring.messaging;

import org.example.common.CatalogEvents;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    Queue catalogEvents() {
        return QueueBuilder.durable(CatalogEvents.QUEUE).build();
    }
}
