package org.example.monitoring.asyncwork;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoringAsyncExceptionHandlerTest {

    @Test
    void handleUncaughtException_recordsMethodAndMessage() throws Exception {
        AsyncFailureLog log = new AsyncFailureLog();
        MonitoringAsyncExceptionHandler handler = new MonitoringAsyncExceptionHandler(log);

        handler.handleUncaughtException(
                new IllegalStateException("Mail gateway rejected edge-1"),
                OperatorNotifier.class.getMethod("notifyServerRegistered", String.class),
                "edge-1");

        assertThat(log.count()).isEqualTo(1);
        assertThat(log.snapshot()).containsExactly("notifyServerRegistered: Mail gateway rejected edge-1");
    }
}
