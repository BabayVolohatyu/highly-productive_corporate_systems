package org.example.monitoring.asyncwork;

import org.example.monitoring.server.ServerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servers")
public class MaintenanceController {

    private final ServerService serverService;
    private final MaintenanceIntake maintenanceIntake;

    public MaintenanceController(ServerService serverService, MaintenanceIntake maintenanceIntake) {
        this.serverService = serverService;
        this.maintenanceIntake = maintenanceIntake;
    }

    @PostMapping("/{id}/maintenance")
    public ResponseEntity<MaintenanceDecision> maintenance(@PathVariable Long id) {
        String hostname = serverService.findById(id).hostname();
        MaintenanceDecision decision = maintenanceIntake.submit(hostname);
        if (decision.accepted()) {
            return ResponseEntity.accepted().body(decision);
        }
        return ResponseEntity.status(429).body(decision);
    }
}
