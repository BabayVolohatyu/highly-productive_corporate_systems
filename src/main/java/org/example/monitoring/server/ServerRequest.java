package org.example.monitoring.server;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServerRequest(
        @NotBlank @Size(max = 1024) String hostname,
        @NotBlank @Size(max = 1024) String ipAddress,
        @NotBlank @Size(max = 64) String environment,
        @NotBlank @Size(max = 64) String status,
        @Size(max = 1024) String description
) {
}
