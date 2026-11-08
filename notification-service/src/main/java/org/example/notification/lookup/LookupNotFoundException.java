package org.example.notification.lookup;

public class LookupNotFoundException extends RuntimeException {

    public LookupNotFoundException(String message) {
        super(message);
    }
}
