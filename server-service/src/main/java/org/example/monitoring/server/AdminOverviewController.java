package org.example.monitoring.server;

import org.example.monitoring.aop.CallMeter;
import org.example.monitoring.aop.OperationTimings;
import org.example.monitoring.web.CurrentOperator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminOverviewController {

    private final ServerService serverService;
    private final CallMeter callMeter;
    private final OperationTimings operationTimings;

    public AdminOverviewController(ServerService serverService,
                                   CallMeter callMeter,
                                   OperationTimings operationTimings) {
        this.serverService = serverService;
        this.callMeter = callMeter;
        this.operationTimings = operationTimings;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview(@CurrentOperator String operator) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("serverCount", serverService.countServers());
        body.put("requestedBy", operator);
        body.put("findByIdEntries", callMeter.getFindByIdEntries());
        body.put("lastListMillis", operationTimings.getLastListMillis());
        return body;
    }
}
