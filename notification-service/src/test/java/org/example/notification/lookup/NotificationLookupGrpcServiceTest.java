package org.example.notification.lookup;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.example.contracts.notification.AlertStatusReply;
import org.example.contracts.notification.HostnameRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationLookupGrpcServiceTest {

    @Test
    void missingAlert_returnsGrpcNotFound() {
        NotificationLookupService lookupService = mock(NotificationLookupService.class);
        when(lookupService.requireStatus("missing")).thenThrow(new LookupNotFoundException("No alert for missing"));
        NotificationLookupGrpcService grpcService = new NotificationLookupGrpcService(lookupService);
        @SuppressWarnings("unchecked")
        StreamObserver<AlertStatusReply> observer = mock(StreamObserver.class);

        grpcService.getAlertStatus(HostnameRequest.newBuilder().setHostname("missing").build(), observer);

        var error = org.mockito.ArgumentCaptor.forClass(Throwable.class);
        verify(observer).onError(error.capture());
        assertThat(error.getValue()).isInstanceOf(StatusRuntimeException.class);
        assertThat(((StatusRuntimeException) error.getValue()).getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }
}
