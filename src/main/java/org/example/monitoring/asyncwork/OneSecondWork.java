package org.example.monitoring.asyncwork;

import org.springframework.stereotype.Component;

@Component
public class OneSecondWork implements BlockingWork {

    @Override
    public void block() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
