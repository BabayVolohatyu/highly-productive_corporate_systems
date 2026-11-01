package org.example.common;

public record CatalogEvent(
        String eventId,
        String type,
        Long serverId,
        String hostname,
        String status
) {
}
