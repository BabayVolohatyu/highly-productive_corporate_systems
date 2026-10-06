package org.example.monitoring.server;

import org.example.monitoring.web.CurrentOperator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminOverviewController {

    private final ServerService serverService;

    public AdminOverviewController(ServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview(@CurrentOperator String operator) {
        return Map.of(
                "serverCount", serverService.countServers(),
                "requestedBy", operator);
    }
}
