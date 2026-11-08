package org.example.monitoring.lookup;

public final class ClientMode {

    public static final String HTTP = "http";
    public static final String GRPC = "grpc";

    private ClientMode() {
    }

    public static String requireKnown(String mode) {
        if (HTTP.equals(mode) || GRPC.equals(mode)) {
            return mode;
        }
        throw new IllegalStateException("Unknown notification.client.mode '" + mode + "'. Use http or grpc.");
    }
}
