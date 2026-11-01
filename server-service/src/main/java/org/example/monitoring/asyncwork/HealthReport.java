package org.example.monitoring.asyncwork;

public record HealthReport(
        String hostname,
        boolean reachable,
        String certificateStatus,
        String probeThread,
        String certificateThread,
        boolean timedOut
) {

    public static HealthReport of(ProbeSnapshot probe, CertificateSnapshot certificate) {
        return new HealthReport(
                probe.hostname(),
                probe.reachable(),
                certificate.status(),
                probe.threadName(),
                certificate.threadName(),
                false);
    }

    public static HealthReport timedOut(String hostname) {
        return new HealthReport(hostname, false, "TIMED_OUT", "", "", true);
    }
}
