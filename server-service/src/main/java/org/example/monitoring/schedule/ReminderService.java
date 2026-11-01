package org.example.monitoring.schedule;

import org.example.monitoring.catalog.TrackedEntityRepository;
import org.example.monitoring.server.ServerAttributes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReminderService {

    static final int MAX_ATTEMPTS = 3;

    private final TrackedEntityRepository entities;
    private final ServerReminderRepository reminders;
    private final ReminderGate gate;
    private final JobFailureLog failures;

    public ReminderService(TrackedEntityRepository entities,
                           ServerReminderRepository reminders,
                           ReminderGate gate,
                           JobFailureLog failures) {
        this.entities = entities;
        this.reminders = reminders;
        this.gate = gate;
        this.failures = failures;
    }

    @Transactional
    public RemindResult remind(int thresholdHours, String windowKey) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                gate.beforePersist();
                return persist(thresholdHours, windowKey);
            } catch (TransientReminderException exception) {
                failures.record(windowKey, exception.getMessage());
            }
        }
        return RemindResult.gaveUp(windowKey);
    }

    private RemindResult persist(int thresholdHours, String windowKey) {
        List<Long> downServers = entities.findIdsByOption(
                ServerAttributes.ENTITY_TYPE,
                ServerAttributes.STATUS,
                "DOWN");
        int created = 0;
        for (Long serverId : downServers) {
            if (reminders.existsByServerIdAndWindowKey(serverId, windowKey)) {
                continue;
            }
            reminders.save(ServerReminder.create(serverId, windowKey, thresholdHours));
            created++;
        }
        return RemindResult.stored(windowKey, created);
    }
}
