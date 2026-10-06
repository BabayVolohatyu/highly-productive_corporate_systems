package org.example.monitoring.aop;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class OperationTimings {

    private static final long UNRECORDED = -1L;

    private final AtomicLong lastListMillis = new AtomicLong(UNRECORDED);

    public void recordListDuration(long millis) {
        lastListMillis.set(millis);
    }

    public long getLastListMillis() {
        return lastListMillis.get();
    }

    void resetForTests() {
        lastListMillis.set(UNRECORDED);
    }
}
