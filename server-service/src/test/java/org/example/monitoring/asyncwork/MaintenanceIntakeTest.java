package org.example.monitoring.asyncwork;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class MaintenanceIntakeTest {

    @Test
    void submit_whenPoolAndQueueAreFull_returnsTryLaterInsteadOfFailing() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("monitoring-maintenance-");
        executor.initialize();
        CountDownLatch release = new CountDownLatch(1);
        MaintenanceIntake intake = new MaintenanceIntake(executor, () -> {
            try {
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("maintenance gate was not released");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });

        try {
            assertThat(intake.submit("edge-1").accepted()).isTrue();
            assertThat(intake.submit("edge-2").accepted()).isTrue();

            MaintenanceDecision rejected = intake.submit("edge-3");

            assertThat(rejected.accepted()).isFalse();
            assertThat(rejected.message()).isEqualTo("Try again later");
            assertThat(rejected.hostname()).isEqualTo("edge-3");
        } finally {
            release.countDown();
            executor.shutdown();
        }
    }
}
