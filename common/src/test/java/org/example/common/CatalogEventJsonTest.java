package org.example.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogEventJsonTest {

    @Test
    void roundTripKeepsEveryField() {
        CatalogEvent event = new CatalogEvent("evt-1", "SERVER_REGISTERED", 4L, "edge-1", "UP");

        CatalogEvent restored = CatalogEventJson.read(CatalogEventJson.write(event));

        assertEquals(event, restored);
    }
}
