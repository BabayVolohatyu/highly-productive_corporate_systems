package org.example.notification;

import org.example.common.CatalogEvent;
import org.example.common.CatalogEventJson;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.SmartMessageConverter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class CatalogEventMessageConverter implements SmartMessageConverter {

    @Override
    public Message toMessage(Object object, MessageProperties properties) {
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        return new Message(CatalogEventJson.write((CatalogEvent) object).getBytes(StandardCharsets.UTF_8), properties);
    }

    @Override
    public Object fromMessage(Message message) {
        return CatalogEventJson.read(new String(message.getBody(), StandardCharsets.UTF_8));
    }

    @Override
    public Object fromMessage(Message message, Object conversionHint) {
        return fromMessage(message);
    }
}
