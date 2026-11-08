package org.example.notification;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationPageController {

    private final NotificationInbox inbox;

    public NotificationPageController(NotificationInbox inbox) {
        this.inbox = inbox;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("notifications", inbox.list());
        return "notifications/list";
    }
}
