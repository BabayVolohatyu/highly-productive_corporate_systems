package org.example.monitoring.lookup;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemoteErrorsTest {

    @Test
    void http404BecomesNotFound() {
        RestClientResponseException notFound = new RestClientResponseException(
                "missing", 404, "Not Found", null, new byte[0], StandardCharsets.UTF_8);

        assertThatThrownBy(() -> RemoteErrors.call(() -> {
            throw notFound;
        })).isInstanceOf(RemoteResourceNotFoundException.class);
    }

    @Test
    void http5xxAndTimeoutsBecomeUnavailable() {
        RestClientResponseException failure = new RestClientResponseException(
                "down", 503, "Service Unavailable", null, new byte[0], StandardCharsets.UTF_8);

        assertThatThrownBy(() -> RemoteErrors.call(() -> {
            throw failure;
        })).isInstanceOf(RemoteServiceUnavailableException.class);
        assertThatThrownBy(() -> RemoteErrors.call(() -> {
            throw new ResourceAccessException("connect timed out");
        })).isInstanceOf(RemoteServiceUnavailableException.class);
    }

    @Test
    void grpcNotFoundBecomesNotFoundAndOtherStatusesBecomeUnavailable() {
        assertThatThrownBy(() -> RemoteErrors.call(() -> {
            throw Status.NOT_FOUND.withDescription("No alert for edge-1").asRuntimeException();
        })).isInstanceOf(RemoteResourceNotFoundException.class)
                .hasMessageContaining("No alert for edge-1");

        assertThatThrownBy(() -> RemoteErrors.call(() -> {
            throw Status.DEADLINE_EXCEEDED.asRuntimeException();
        })).isInstanceOf(RemoteServiceUnavailableException.class);
        assertThatThrownBy(() -> RemoteErrors.call(() -> {
            throw Status.UNAVAILABLE.asRuntimeException();
        })).isInstanceOf(RemoteServiceUnavailableException.class);
    }

    @Test
    void successfulCallReturnsTheValue() {
        assertThat(RemoteErrors.call(() -> "ok")).isEqualTo("ok");
    }

    @Test
    void adaptersDoNotLeakTransportExceptions() {
        NotificationHttpApi api = new NotificationHttpApi() {
            @Override
            public AlertStatus status(String hostname) {
                throw new RestClientResponseException("missing", 404, "Not Found", null, new byte[0], StandardCharsets.UTF_8);
            }

            @Override
            public AlertCount count(String hostname) {
                return null;
            }

            @Override
            public DeliveryReceipt receipt(String eventId) {
                return null;
            }
        };

        assertThatThrownBy(() -> new HttpAlertStatusLookup(api).findByHostname("edge-1"))
                .isInstanceOf(RemoteResourceNotFoundException.class)
                .isNotInstanceOf(RestClientResponseException.class)
                .isNotInstanceOf(StatusRuntimeException.class);
    }
}
