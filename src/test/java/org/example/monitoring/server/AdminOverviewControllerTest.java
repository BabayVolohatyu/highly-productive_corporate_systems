package org.example.monitoring.server;

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

    @InjectMocks
    private AdminOverviewController controller;

    @Test
    void overview_returnsServerCountFromService() {
        // Arrange
        when(serverService.countServers()).thenReturn(7L);

        // Act
        Map<String, Long> result = controller.overview();

        // Assert
        assertThat(result).containsEntry("serverCount", 7L);
        verify(serverService).countServers();
    }
}
