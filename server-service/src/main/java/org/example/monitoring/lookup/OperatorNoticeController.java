package org.example.monitoring.lookup;

import org.example.monitoring.server.ServerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servers/{id}/operator-notices")
public class OperatorNoticeController {

    private final ServerService serverService;
    private final OperatorNoticeService operatorNoticeService;

    public OperatorNoticeController(ServerService serverService, OperatorNoticeService operatorNoticeService) {
        this.serverService = serverService;
        this.operatorNoticeService = operatorNoticeService;
    }

    @GetMapping("/status")
    public AlertStatus status(@PathVariable Long id) {
        return operatorNoticeService.status(serverService.findById(id).hostname());
    }

    @GetMapping("/count")
    public AlertCount count(@PathVariable Long id) {
        return operatorNoticeService.count(serverService.findById(id).hostname());
    }

    @GetMapping("/receipts/{eventId}")
    public DeliveryReceipt receipt(@PathVariable Long id, @PathVariable String eventId) {
        serverService.findById(id);
        return operatorNoticeService.receipt(eventId);
    }
}
