package org.example.monitoring.lookup;

public class HttpAlertStatusLookup implements AlertStatusLookup {

    private final NotificationHttpApi api;

    public HttpAlertStatusLookup(NotificationHttpApi api) {
        this.api = api;
    }

    @Override
    public AlertStatus findByHostname(String hostname) {
        return RemoteErrors.call(() -> api.status(hostname));
    }
}
