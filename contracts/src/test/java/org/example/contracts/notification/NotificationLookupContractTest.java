package org.example.contracts.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationLookupContractTest {

    @Test
    void generatedServiceDeclaresTheThreeRpcs() {
        var names = NotificationLookupGrpc.getServiceDescriptor().getMethods().stream()
                .map(method -> method.getBareMethodName())
                .sorted()
                .toList();

        assertEquals(java.util.List.of("CountAlerts", "GetAlertStatus", "GetDeliveryReceipt"), names);
    }
}
