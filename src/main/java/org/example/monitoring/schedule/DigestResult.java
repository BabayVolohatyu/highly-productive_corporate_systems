package org.example.monitoring.schedule;

public record DigestResult(String digestKey, long serverCount, boolean created) {

    public static DigestResult created(String digestKey, long serverCount) {
        return new DigestResult(digestKey, serverCount, true);
    }

    public static DigestResult unchanged(String digestKey) {
        return new DigestResult(digestKey, 0, false);
    }
}
