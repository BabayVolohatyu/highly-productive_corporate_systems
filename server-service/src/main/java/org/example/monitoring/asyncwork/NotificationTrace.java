package org.example.monitoring.asyncwork;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class NotificationTrace {

    private final AtomicReference<String> lastHost = new AtomicReference<>();
    private final AtomicReference<String> lastThread = new AtomicReference<>();

    public void record(String hostname, String threadName) {
        lastHost.set(hostname);
        lastThread.set(threadName);
    }

    public String lastHost() {
        return lastHost.get();
    }

    public String lastThread() {
        return lastThread.get();
    }
}
