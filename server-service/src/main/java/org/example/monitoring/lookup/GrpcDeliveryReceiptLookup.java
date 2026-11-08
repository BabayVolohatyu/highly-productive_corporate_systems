package org.example.monitoring.lookup;

import org.example.contracts.notification.EventRequest;
import org.example.contracts.notification.NotificationLookupGrpc;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class GrpcDeliveryReceiptLookup implements DeliveryReceiptLookup {

    private final NotificationLookupGrpc.NotificationLookupBlockingStub stub;
    private final Duration deadline;

    public GrpcDeliveryReceiptLookup(NotificationLookupGrpc.NotificationLookupBlockingStub stub, Duration deadline) {
        this.stub = stub;
        this.deadline = deadline;
    }

    @Override
    public DeliveryReceipt findByEventId(String eventId) {
        return RemoteErrors.call(() -> {
            var reply = stub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                    .getDeliveryReceipt(EventRequest.newBuilder().setEventId(eventId).build());
            return new DeliveryReceipt(reply.getEventId(), reply.getDelivered(), reply.getMessage());
        });
    }
}
