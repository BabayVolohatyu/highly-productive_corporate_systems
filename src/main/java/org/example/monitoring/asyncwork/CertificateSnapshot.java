package org.example.monitoring.asyncwork;

public record CertificateSnapshot(String hostname, String status, String threadName) {

    public static CertificateSnapshot unavailable(String hostname) {
        return new CertificateSnapshot(hostname, "UNAVAILABLE", Thread.currentThread().getName());
    }
}
