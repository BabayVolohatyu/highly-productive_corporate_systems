package org.example.monitoring.asyncwork;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OperatorNotifier {

    private final MailGateway mailGateway;
    private final NotificationTrace trace;

    public OperatorNotifier(MailGateway mailGateway, NotificationTrace trace) {
        this.mailGateway = mailGateway;
        this.trace = trace;
    }

    @Async("monitoringExecutor")
    public void notifyServerRegistered(String hostname) {
        mailGateway.send(hostname);
        trace.record(hostname, Thread.currentThread().getName());
    }
}
