package org.example.notification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationInbox inbox;

    public NotificationController(NotificationInbox inbox) {
        this.inbox = inbox;
    }

    @GetMapping
    public List<NotificationView> list() {
        return inbox.list();
    }

    @PostMapping
    public NotificationView create(@RequestBody Map<String, String> body) {
        return inbox.record(body.getOrDefault("message", ""));
    }
}
