package org.example.notification.lookup;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.grpc.server")
public record GrpcServerProperties(boolean enabled, int port) {
}
