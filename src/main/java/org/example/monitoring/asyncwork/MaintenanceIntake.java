package org.example.monitoring.asyncwork;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceIntake {

    private final ThreadPoolTaskExecutor maintenanceExecutor;
    private final BlockingWork blockingWork;

    public MaintenanceIntake(@Qualifier("maintenanceExecutor") ThreadPoolTaskExecutor maintenanceExecutor,
                             BlockingWork blockingWork) {
        this.maintenanceExecutor = maintenanceExecutor;
        this.blockingWork = blockingWork;
    }

    public MaintenanceDecision submit(String hostname) {
        try {
            maintenanceExecutor.submit(blockingWork::block);
            return MaintenanceDecision.accepted(hostname);
        } catch (TaskRejectedException exception) {
            return MaintenanceDecision.tryLater(hostname);
        }
    }
}
