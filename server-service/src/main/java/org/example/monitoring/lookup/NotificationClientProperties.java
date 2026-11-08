package org.example.monitoring.lookup;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "notification.client")
public record NotificationClientProperties(String mode, Http http, Grpc grpc) {

    public record Http(String baseUrl, Duration connectTimeout, Duration readTimeout) {
    }

    public record Grpc(String address, Duration deadline) {
    }
}
