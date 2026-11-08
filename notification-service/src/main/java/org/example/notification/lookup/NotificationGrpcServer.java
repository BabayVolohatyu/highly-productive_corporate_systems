package org.example.notification.lookup;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "notification.grpc.server.enabled", havingValue = "true")
public class NotificationGrpcServer implements SmartLifecycle {

    private final GrpcServerProperties properties;
    private final NotificationLookupGrpcService grpcService;
    private Server server;

    public NotificationGrpcServer(GrpcServerProperties properties, NotificationLookupGrpcService grpcService) {
        this.properties = properties;
        this.grpcService = grpcService;
    }

    @Override
    public void start() {
        try {
            server = ServerBuilder.forPort(properties.port()).addService(grpcService).build().start();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start the notification gRPC server", exception);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Override
    public boolean isRunning() {
        return server != null && !server.isShutdown();
    }
}
