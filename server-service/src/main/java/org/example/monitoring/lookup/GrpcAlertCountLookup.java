package org.example.monitoring.lookup;

import org.example.contracts.notification.HostnameRequest;
import org.example.contracts.notification.NotificationLookupGrpc;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class GrpcAlertCountLookup implements AlertCountLookup {

    private final NotificationLookupGrpc.NotificationLookupBlockingStub stub;
    private final Duration deadline;

    public GrpcAlertCountLookup(NotificationLookupGrpc.NotificationLookupBlockingStub stub, Duration deadline) {
        this.stub = stub;
        this.deadline = deadline;
    }

    @Override
    public AlertCount countByHostname(String hostname) {
        return RemoteErrors.call(() -> {
            var reply = stub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                    .countAlerts(HostnameRequest.newBuilder().setHostname(hostname).build());
            return new AlertCount(reply.getHostname(), reply.getCount());
        });
    }
}
