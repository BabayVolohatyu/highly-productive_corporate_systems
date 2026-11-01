package org.example.monitoring.asyncwork;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {
        AsyncConfig.class,
        ReachabilityProbe.class,
        CertificateLookup.class,
        ServerHealthService.class,
        MonitoringAsyncExceptionHandler.class,
        AsyncFailureLog.class,
        ServerHealthConcurrencyTest.GateConfig.class
})
class ServerHealthConcurrencyTest {

    @Autowired
    private ServerHealthService healthService;

    @Autowired
    private TwoPartyGate gate;

    @AfterEach
    void release() {
        gate.release.countDown();
    }

    @Test
    void report_startsBothCallsBeforeEitherFinishes_andReturnsCombinedResult() throws Exception {
        CompletableFuture<HealthReport> pending = CompletableFuture.supplyAsync(() -> healthService.report("edge-1"));

        assertThat(gate.bothStarted.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(gate.threads).hasSize(2);
        assertThat(gate.threads).allMatch(name -> name.startsWith("monitoring-async-"));

        gate.release.countDown();
        HealthReport report = pending.get(2, TimeUnit.SECONDS);

        assertThat(report.reachable()).isTrue();
        assertThat(report.certificateStatus()).isEqualTo("OK");
        assertThat(report.timedOut()).isFalse();
        assertThat(report.probeThread()).startsWith("monitoring-async-");
        assertThat(report.certificateThread()).startsWith("monitoring-async-");
        assertThat(report.probeThread()).isNotEqualTo(report.certificateThread());
    }

    @Configuration
    static class GateConfig {
        @Bean
        TwoPartyGate blockingWork() {
            return new TwoPartyGate();
        }
    }

    static final class TwoPartyGate implements BlockingWork {
        private final CountDownLatch bothStarted = new CountDownLatch(2);
        private final CountDownLatch release = new CountDownLatch(1);
        private final Set<String> threads = ConcurrentHashMap.newKeySet();

        @Override
        public void block() {
            threads.add(Thread.currentThread().getName());
            bothStarted.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("health gate was not released");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
