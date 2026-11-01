package org.example.monitoring.asyncwork;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringJUnitConfig(classes = {
        AsyncConfig.class,
        OperatorNotifier.class,
        NotificationTrace.class,
        AsyncFailureLog.class,
        MonitoringAsyncExceptionHandler.class,
        AsyncFailurePropagationTest.FailingMailConfig.class
})
class AsyncFailurePropagationTest {

    @Autowired
    private OperatorNotifier notifier;

    @Autowired
    private AsyncFailureLog failureLog;

    @Test
    void voidAsyncFailureIsRecordedAndDoesNotEscapeTheCaller() throws Exception {
        assertThatCode(() -> notifier.notifyServerRegistered("edge-1")).doesNotThrowAnyException();

        String recorded = failureLog.awaitNext(Duration.ofSeconds(2));

        assertThat(recorded).contains("Mail gateway rejected edge-1");
    }

    @Configuration
    static class FailingMailConfig {
        @Bean
        MailGateway mailGateway() {
            return hostname -> {
                throw new IllegalStateException("Mail gateway rejected " + hostname);
            };
        }
    }
}
