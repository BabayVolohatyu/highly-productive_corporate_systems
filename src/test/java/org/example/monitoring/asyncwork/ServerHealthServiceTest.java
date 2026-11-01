package org.example.monitoring.asyncwork;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServerHealthServiceTest {

    private final ReachabilityProbe probe = mock(ReachabilityProbe.class);
    private final CertificateLookup lookup = mock(CertificateLookup.class);

    @Test
    void report_bothCallsSucceed_returnsCombinedResult() {
        when(probe.probe("edge-1")).thenReturn(CompletableFuture.completedFuture(
                new ProbeSnapshot("edge-1", true, "monitoring-async-1")));
        when(lookup.lookup("edge-1")).thenReturn(CompletableFuture.completedFuture(
                new CertificateSnapshot("edge-1", "OK", "monitoring-async-2")));

        HealthReport report = service(Duration.ofSeconds(2)).report("edge-1");

        assertThat(report.hostname()).isEqualTo("edge-1");
        assertThat(report.reachable()).isTrue();
        assertThat(report.certificateStatus()).isEqualTo("OK");
        assertThat(report.probeThread()).isEqualTo("monitoring-async-1");
        assertThat(report.certificateThread()).isEqualTo("monitoring-async-2");
        assertThat(report.timedOut()).isFalse();
    }

    @Test
    void report_whenCertificateLookupFails_returnsUnavailableWithoutThrowing() {
        when(probe.probe("edge-1")).thenReturn(CompletableFuture.completedFuture(
                new ProbeSnapshot("edge-1", true, "monitoring-async-1")));
        when(lookup.lookup("edge-1")).thenReturn(CompletableFuture.failedFuture(
                new IllegalStateException("Certificate authority unreachable")));

        HealthReport report = service(Duration.ofSeconds(2)).report("edge-1");

        assertThat(report.reachable()).isTrue();
        assertThat(report.certificateStatus()).isEqualTo("UNAVAILABLE");
        assertThat(report.timedOut()).isFalse();
    }

    @Test
    void report_whenProbeNeverCompletes_stopsWaitingAtTimeout() {
        when(probe.probe("edge-1")).thenReturn(new CompletableFuture<>());
        when(lookup.lookup("edge-1")).thenReturn(CompletableFuture.completedFuture(
                new CertificateSnapshot("edge-1", "OK", "monitoring-async-2")));
        long started = System.nanoTime();

        HealthReport report = service(Duration.ofMillis(100)).report("edge-1");

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        assertThat(report.timedOut()).isTrue();
        assertThat(report.hostname()).isEqualTo("edge-1");
        assertThat(elapsedMs).isLessThan(1000);
    }

    private ServerHealthService service(Duration timeout) {
        return new ServerHealthService(probe, lookup, timeout);
    }
}
