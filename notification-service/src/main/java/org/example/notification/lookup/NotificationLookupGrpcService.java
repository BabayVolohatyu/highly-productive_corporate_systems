package org.example.notification.lookup;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.contracts.notification.AlertCountReply;
import org.example.contracts.notification.AlertStatusReply;
import org.example.contracts.notification.DeliveryReceiptReply;
import org.example.contracts.notification.EventRequest;
import org.example.contracts.notification.HostnameRequest;
import org.example.contracts.notification.NotificationLookupGrpc;
import org.springframework.stereotype.Service;

@Service
public class NotificationLookupGrpcService extends NotificationLookupGrpc.NotificationLookupImplBase {

    private final NotificationLookupService lookupService;

    public NotificationLookupGrpcService(NotificationLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @Override
    public void getAlertStatus(HostnameRequest request, StreamObserver<AlertStatusReply> responseObserver) {
        reply(responseObserver, () -> {
            AlertStatus status = lookupService.requireStatus(request.getHostname());
            return AlertStatusReply.newBuilder()
                    .setHostname(status.hostname())
                    .setStatus(status.status())
                    .setMessage(status.message())
                    .build();
        });
    }

    @Override
    public void countAlerts(HostnameRequest request, StreamObserver<AlertCountReply> responseObserver) {
        reply(responseObserver, () -> {
            AlertCount count = lookupService.requireCount(request.getHostname());
            return AlertCountReply.newBuilder()
                    .setHostname(count.hostname())
                    .setCount(count.count())
                    .build();
        });
    }

    @Override
    public void getDeliveryReceipt(EventRequest request, StreamObserver<DeliveryReceiptReply> responseObserver) {
        reply(responseObserver, () -> {
            DeliveryReceipt receipt = lookupService.requireReceipt(request.getEventId());
            return DeliveryReceiptReply.newBuilder()
                    .setEventId(receipt.eventId())
                    .setDelivered(receipt.delivered())
                    .setMessage(receipt.message())
                    .build();
        });
    }

    private <T> void reply(StreamObserver<T> responseObserver, ReplySupplier<T> supplier) {
        try {
            responseObserver.onNext(supplier.get());
            responseObserver.onCompleted();
        } catch (LookupNotFoundException exception) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(exception.getMessage()).asRuntimeException());
        } catch (RuntimeException exception) {
            responseObserver.onError(Status.UNAVAILABLE.withDescription(exception.getMessage()).asRuntimeException());
        }
    }

    @FunctionalInterface
    private interface ReplySupplier<T> {
        T get();
    }
}
