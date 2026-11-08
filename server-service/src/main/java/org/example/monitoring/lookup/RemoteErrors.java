package org.example.monitoring.lookup;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.function.Supplier;

public final class RemoteErrors {

    private RemoteErrors() {
    }

    public static <T> T call(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RemoteResourceNotFoundException | RemoteServiceUnavailableException known) {
            throw known;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RemoteResourceNotFoundException("Notification service has no matching record");
            }
            throw new RemoteServiceUnavailableException("Notification service returned " + exception.getStatusCode().value());
        } catch (RestClientException exception) {
            throw new RemoteServiceUnavailableException("Notification service is unavailable");
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new RemoteResourceNotFoundException(blankTo(exception.getStatus().getDescription(), "Notification service has no matching record"));
            }
            throw new RemoteServiceUnavailableException("Notification service is unavailable: " + exception.getStatus().getCode().name());
        }
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
