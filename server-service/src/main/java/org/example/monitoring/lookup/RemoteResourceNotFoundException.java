package org.example.monitoring.lookup;

public class RemoteResourceNotFoundException extends RuntimeException {

    public RemoteResourceNotFoundException(String message) {
        super(message);
    }
}
