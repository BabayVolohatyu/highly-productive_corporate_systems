package org.example.monitoring.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerControllerTest {

    @Mock
    private ServerService serverService;

    @InjectMocks
    private ServerController controller;

    @Test
    void list_delegatesToService() {
        // Arrange
        ServerResponse row = sampleResponse(1L);
        when(serverService.findAll()).thenReturn(List.of(row));

        // Act
        List<ServerResponse> result = controller.list();

        // Assert
        assertThat(result).containsExactly(row);
        verify(serverService).findAll();
    }

    @Test
    void get_delegatesToService() {
        // Arrange
        ServerResponse row = sampleResponse(2L);
        when(serverService.findById(2L)).thenReturn(row);

        // Act
        ServerResponse result = controller.get(2L);

        // Assert
        assertThat(result).isEqualTo(row);
        verify(serverService).findById(2L);
    }

    @Test
    void create_delegatesToService() {
        // Arrange
        ServerRequest request = new ServerRequest("h", "1.1.1.1", "DEV", "UP", "");
        ServerResponse created = sampleResponse(3L);
        when(serverService.create(request)).thenReturn(created);

        // Act
        ServerResponse result = controller.create(request);

        // Assert
        assertThat(result).isEqualTo(created);
        verify(serverService).create(request);
    }

    @Test
    void update_delegatesToService() {
        // Arrange
        ServerRequest request = new ServerRequest("h", "1.1.1.1", "DEV", "UP", "");
        ServerResponse updated = sampleResponse(4L);
        when(serverService.update(4L, request)).thenReturn(updated);

        // Act
        ServerResponse result = controller.update(4L, request);

        // Assert
        assertThat(result).isEqualTo(updated);
        verify(serverService).update(4L, request);
    }

    @Test
    void delete_delegatesToService() {
        // Act
        controller.delete(5L);

        // Assert
        verify(serverService).delete(5L);
    }

    private static ServerResponse sampleResponse(long id) {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        return new ServerResponse(id, "host", "1.1.1.1", "DEV", "UP", "", now, now);
    }
}
