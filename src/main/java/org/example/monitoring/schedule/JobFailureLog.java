package org.example.monitoring.schedule;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class JobFailureLog {

    private final List<String> entries = new CopyOnWriteArrayList<>();

    public void record(String key, String message) {
        entries.add(key + ": " + message);
    }

    public List<String> snapshot() {
        return new ArrayList<>(entries);
    }

    public int countFor(String key) {
        return (int) entries.stream().filter(entry -> entry.startsWith(key + ":")).count();
    }
}
