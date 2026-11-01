package org.example.monitoring.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "server_reminder", uniqueConstraints = @UniqueConstraint(
        name = "uq_server_reminder_window",
        columnNames = {"server_id", "window_key"}))
public class ServerReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "server_id", nullable = false)
    private Long serverId;

    @Column(name = "window_key", nullable = false, length = 64)
    private String windowKey;

    @Column(name = "threshold_hours", nullable = false)
    private int thresholdHours;

    protected ServerReminder() {
    }

    public static ServerReminder create(Long serverId, String windowKey, int thresholdHours) {
        ServerReminder row = new ServerReminder();
        row.serverId = serverId;
        row.windowKey = windowKey;
        row.thresholdHours = thresholdHours;
        return row;
    }

    public Long getServerId() {
        return serverId;
    }

    public String getWindowKey() {
        return windowKey;
    }

    public int getThresholdHours() {
        return thresholdHours;
    }
}
