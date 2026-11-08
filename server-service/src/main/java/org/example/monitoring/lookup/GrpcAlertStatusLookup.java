package org.example.monitoring.lookup;

import org.example.contracts.notification.HostnameRequest;
import org.example.contracts.notification.NotificationLookupGrpc;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class GrpcAlertStatusLookup implements AlertStatusLookup {

    private final NotificationLookupGrpc.NotificationLookupBlockingStub stub;
    private final Duration deadline;

    public GrpcAlertStatusLookup(NotificationLookupGrpc.NotificationLookupBlockingStub stub, Duration deadline) {
        this.stub = stub;
        this.deadline = deadline;
    }

    @Override
    public AlertStatus findByHostname(String hostname) {
        return RemoteErrors.call(() -> {
            var reply = stub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                    .getAlertStatus(HostnameRequest.newBuilder().setHostname(hostname).build());
            return new AlertStatus(reply.getHostname(), reply.getStatus(), reply.getMessage());
        });
    }
}
