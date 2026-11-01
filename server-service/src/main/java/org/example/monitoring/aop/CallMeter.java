package org.example.monitoring.aop;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class CallMeter {

    private final AtomicLong findByIdEntries = new AtomicLong();

    public void recordExternalCall(String signatureKey) {
        if ("findById".equals(signatureKey)) {
            findByIdEntries.incrementAndGet();
        }
    }

    public long getFindByIdEntries() {
        return findByIdEntries.get();
    }

    void resetForTests() {
        findByIdEntries.set(0);
    }
}
