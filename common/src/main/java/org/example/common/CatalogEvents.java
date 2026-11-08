package org.example.common;

public final class CatalogEvents {

    public static final String QUEUE = "catalog.events";

    public static String amqpAddress() {
        return "/queues/" + QUEUE;
    }
    public static final String SERVER_REGISTERED = "SERVER_REGISTERED";
    public static final String SERVER_STATUS_CHANGED = "SERVER_STATUS_CHANGED";
    public static final String REPORT_REQUESTED = "REPORT_REQUESTED";

    private CatalogEvents() {
    }
}
