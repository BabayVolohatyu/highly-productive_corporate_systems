package org.example.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogEventTest {

    @Test
    void keepsTheFieldsUsedToSerializeAnEvent() {
        CatalogEvent event = new CatalogEvent("evt-1", "SERVER_REGISTERED", 4L, "edge-1", "UP");

        assertEquals("evt-1", event.eventId());
        assertEquals("SERVER_REGISTERED", event.type());
        assertEquals(4L, event.serverId());
        assertEquals("edge-1", event.hostname());
        assertEquals("UP", event.status());
    }
}
