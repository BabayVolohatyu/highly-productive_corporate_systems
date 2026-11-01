package org.example.monitoring.asyncwork;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/mail")
public class MailStatusController {

    private final NotificationTrace trace;
    private final AsyncFailureLog failureLog;

    public MailStatusController(NotificationTrace trace, AsyncFailureLog failureLog) {
        this.trace = trace;
        this.failureLog = failureLog;
    }

    @GetMapping
    public Map<String, Object> status() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("lastMailedHost", trace.lastHost() == null ? "" : trace.lastHost());
        body.put("threadName", trace.lastThread() == null ? "" : trace.lastThread());
        body.put("failureCount", failureLog.count());
        return body;
    }
}
