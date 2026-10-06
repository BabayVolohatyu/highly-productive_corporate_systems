package org.example.monitoring.server;

import java.time.Instant;

public record ServerResponse(
        Long id,
        String hostname,
        String ipAddress,
        String environment,
        String status,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
