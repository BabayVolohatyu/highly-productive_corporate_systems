package org.example.monitoring.asyncwork;

public record ProbeSnapshot(String hostname, boolean reachable, String threadName) {

    public static ProbeSnapshot unreachable(String hostname) {
        return new ProbeSnapshot(hostname, false, Thread.currentThread().getName());
    }
}
