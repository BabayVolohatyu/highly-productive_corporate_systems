package org.example.monitoring.asyncwork;

public record MaintenanceDecision(String hostname, boolean accepted, String message) {

    public static MaintenanceDecision accepted(String hostname) {
        return new MaintenanceDecision(hostname, true, "Maintenance window queued");
    }

    public static MaintenanceDecision tryLater(String hostname) {
        return new MaintenanceDecision(hostname, false, "Try again later");
    }
}
