package org.example.monitoring.schedule;

public record RemindResult(String windowKey, int created, boolean gaveUp) {

    public static RemindResult stored(String windowKey, int created) {
        return new RemindResult(windowKey, created, false);
    }

    public static RemindResult gaveUp(String windowKey) {
        return new RemindResult(windowKey, 0, true);
    }
}
