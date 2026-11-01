package org.example.monitoring.asyncwork;

import org.example.monitoring.server.ServerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servers")
public class HealthController {

    private final ServerService serverService;
    private final ServerHealthService serverHealthService;

    public HealthController(ServerService serverService, ServerHealthService serverHealthService) {
        this.serverService = serverService;
        this.serverHealthService = serverHealthService;
    }

    @GetMapping("/{id}/health")
    public HealthReport health(@PathVariable Long id) {
        return serverHealthService.report(serverService.findById(id).hostname());
    }
}
