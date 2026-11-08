package org.example.monitoring.lookup;

public class HttpAlertCountLookup implements AlertCountLookup {

    private final NotificationHttpApi api;

    public HttpAlertCountLookup(NotificationHttpApi api) {
        this.api = api;
    }

    @Override
    public AlertCount countByHostname(String hostname) {
        return RemoteErrors.call(() -> api.count(hostname));
    }
}
