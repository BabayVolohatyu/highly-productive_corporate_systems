package org.example.monitoring.asyncwork;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class AsyncFailureLog {

    private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

    public void record(String message) {
        messages.add(message);
    }

    public int count() {
        return messages.size();
    }

    public List<String> snapshot() {
        return new ArrayList<>(messages);
    }

    public String awaitNext(Duration timeout) throws InterruptedException {
        String message = messages.poll(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        if (message == null) {
            throw new IllegalStateException("No asynchronous failure was recorded");
        }
        return message;
    }
}
