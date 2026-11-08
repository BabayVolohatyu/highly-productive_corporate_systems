package org.example.notification;

import org.example.common.CatalogEvents;
import org.example.common.DurableQueueDeclaration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CatalogQueueDeclarationRunner implements ApplicationRunner {

    private final String host;
    private final String username;
    private final String password;

    public CatalogQueueDeclarationRunner(@Value("${spring.amqp.host:localhost}") String host,
                                         @Value("${spring.amqp.username:guest}") String username,
                                         @Value("${spring.amqp.password:guest}") String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (int attempt = 0; attempt < 30; attempt++) {
            if (DurableQueueDeclaration.declare(host, username, password, CatalogEvents.QUEUE)) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
