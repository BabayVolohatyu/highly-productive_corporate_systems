package org.example.monitoring.schedule;

import org.springframework.stereotype.Component;

@Component
public class OpenReminderGate implements ReminderGate {

    @Override
    public void beforePersist() {
    }
}
