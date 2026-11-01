package org.example.monitoring.server;

import org.example.monitoring.catalog.TrackedEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ServerMapperTest {

    private final ServerMapper mapper = new ServerMapperImpl();

    @Test
    void toResponse_mapsEntityAndAttributeFields() {
        // Arrange
        TrackedEntity entity = new TrackedEntity();
        ReflectionTestUtils.setField(entity, "id", 9L);
        Instant created = Instant.parse("2024-06-01T12:00:00Z");
        Instant updated = Instant.parse("2024-06-02T12:00:00Z");
        entity.setCreatedAt(created);
        entity.setUpdatedAt(updated);
        ServerMappingSource source = new ServerMappingSource(
                entity, "app-01", "10.10.10.10", "PROD", "UP", "primary");

        // Act
        ServerResponse response = mapper.toResponse(source);

        // Assert
        assertThat(response.id()).isEqualTo(9L);
        assertThat(response.hostname()).isEqualTo("app-01");
        assertThat(response.ipAddress()).isEqualTo("10.10.10.10");
        assertThat(response.environment()).isEqualTo("PROD");
        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.description()).isEqualTo("primary");
        assertThat(response.createdAt()).isEqualTo(created);
        assertThat(response.updatedAt()).isEqualTo(updated);
    }
}
