package org.example.monitoring.asyncwork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ServerHealthService {

    private final ReachabilityProbe reachabilityProbe;
    private final CertificateLookup certificateLookup;
    private final Duration timeout;

    @Autowired
    public ServerHealthService(ReachabilityProbe reachabilityProbe, CertificateLookup certificateLookup) {
        this(reachabilityProbe, certificateLookup, Duration.ofSeconds(2));
    }

    ServerHealthService(ReachabilityProbe reachabilityProbe,
                        CertificateLookup certificateLookup,
                        Duration timeout) {
        this.reachabilityProbe = reachabilityProbe;
        this.certificateLookup = certificateLookup;
        this.timeout = timeout;
    }

    public HealthReport report(String hostname) {
        var probe = reachabilityProbe.probe(hostname)
                .exceptionally(error -> ProbeSnapshot.unreachable(hostname));
        var certificate = certificateLookup.lookup(hostname)
                .exceptionally(error -> CertificateSnapshot.unavailable(hostname));
        try {
            return probe.thenCombine(certificate, HealthReport::of)
                    .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .join();
        } catch (CompletionException exception) {
            if (exception.getCause() instanceof TimeoutException) {
                return HealthReport.timedOut(hostname);
            }
            throw exception;
        }
    }
}
