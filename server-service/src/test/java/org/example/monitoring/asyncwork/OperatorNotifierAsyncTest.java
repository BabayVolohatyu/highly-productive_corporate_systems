package org.example.monitoring.asyncwork;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringJUnitConfig(classes = {
        AsyncConfig.class,
        OperatorNotifier.class,
        NotificationTrace.class,
        AsyncFailureLog.class,
        MonitoringAsyncExceptionHandler.class,
        OperatorNotifierAsyncTest.GateMailConfig.class
})
class OperatorNotifierAsyncTest {

    @Autowired
    private OperatorNotifier notifier;

    @Autowired
    private NotificationTrace trace;

    @Autowired
    private HoldingGateway gateway;

    @Autowired
    @Qualifier("monitoringExecutor")
    private ThreadPoolTaskExecutor monitoringExecutor;

    @AfterEach
    void release() {
        gateway.release.countDown();
    }

    @Test
    void notifyReturnsBeforeMailFinishes_andRunsOnMonitoringExecutor() throws Exception {
        assertThat(monitoringExecutor.getThreadNamePrefix()).isEqualTo("monitoring-async-");
        assertThat(monitoringExecutor.getCorePoolSize()).isEqualTo(2);
        assertThat(monitoringExecutor.getMaxPoolSize()).isEqualTo(4);
        assertThat(monitoringExecutor.getQueueCapacity()).isEqualTo(20);
        assertThat(monitoringExecutor).isNotInstanceOf(org.springframework.core.task.SimpleAsyncTaskExecutor.class);

        assertThatCode(() -> notifier.notifyServerRegistered("edge-1")).doesNotThrowAnyException();
        assertThat(gateway.started.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(trace.lastHost()).isNull();
        assertThat(gateway.thread).startsWith("monitoring-async-");

        gateway.release.countDown();
        long deadline = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        while (trace.lastHost() == null && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertThat(trace.lastHost()).isEqualTo("edge-1");
        assertThat(trace.lastThread()).startsWith("monitoring-async-");
    }

    @Configuration
    static class GateMailConfig {
        @Bean
        HoldingGateway mailGateway() {
            return new HoldingGateway();
        }
    }

    static final class HoldingGateway implements MailGateway {
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private volatile String thread;

        @Override
        public void send(String hostname) {
            thread = Thread.currentThread().getName();
            started.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("mail gate was not released");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
