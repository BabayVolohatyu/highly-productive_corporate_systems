package org.example.monitoring.server;

import org.example.monitoring.aop.CallMeter;
import org.example.monitoring.aop.OperationTimings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOverviewControllerTest {

    @Mock
    private ServerService serverService;

    @Mock
    private CallMeter callMeter;

    @Mock
    private OperationTimings operationTimings;

    @InjectMocks
    private AdminOverviewController controller;

    @Test
    void overview_returnsServerCountFromService() {
        // Arrange
        when(serverService.countServers()).thenReturn(7L);
        when(callMeter.getFindByIdEntries()).thenReturn(3L);
        when(operationTimings.getLastListMillis()).thenReturn(42L);

        // Act
        Map<String, Object> result = controller.overview("alice");

        // Assert
        assertThat(result).containsEntry("serverCount", 7L);
        assertThat(result).containsEntry("requestedBy", "alice");
        assertThat(result).containsEntry("findByIdEntries", 3L);
        assertThat(result).containsEntry("lastListMillis", 42L);
        verify(serverService).countServers();
    }
}
