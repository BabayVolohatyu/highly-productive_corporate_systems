package org.example.monitoring.schedule;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
public class CatalogDigestJob implements Job {

    @Autowired
    private DigestService digestService;

    @Override
    public void execute(JobExecutionContext context) {
        digestService.publish(JobParameters.requiredText(context.getMergedJobDataMap(), "digestKey"));
    }
}
