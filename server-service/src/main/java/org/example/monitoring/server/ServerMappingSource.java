package org.example.monitoring.server;

import org.example.monitoring.catalog.TrackedEntity;

public class ServerMappingSource {

    private TrackedEntity entity;
    private String hostname;
    private String ipAddress;
    private String environment;
    private String status;
    private String description;

    public ServerMappingSource() {
    }

    public ServerMappingSource(TrackedEntity entity,
                               String hostname,
                               String ipAddress,
                               String environment,
                               String status,
                               String description) {
        this.entity = entity;
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.environment = environment;
        this.status = status;
        this.description = description;
    }

    public TrackedEntity getEntity() {
        return entity;
    }

    public void setEntity(TrackedEntity entity) {
        this.entity = entity;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
