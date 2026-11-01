package org.example.monitoring.schedule;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
public class StaleDownServerReminderJob implements Job {

    @Autowired
    private ReminderService reminderService;

    @Override
    public void execute(JobExecutionContext context) {
        var data = context.getMergedJobDataMap();
        reminderService.remind(
                JobParameters.requiredInt(data, "thresholdHours"),
                JobParameters.requiredText(data, "windowKey"));
    }
}
