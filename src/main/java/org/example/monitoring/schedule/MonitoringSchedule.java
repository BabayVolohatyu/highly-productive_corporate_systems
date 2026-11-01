package org.example.monitoring.schedule;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;

import java.util.TimeZone;

public final class MonitoringSchedule {

    public static final String GROUP = "monitoring";

    private MonitoringSchedule() {
    }

    public static JobDetail reminderJob() {
        JobDataMap defaults = new JobDataMap();
        defaults.put("thresholdHours", 24);
        defaults.put("windowKey", "default");
        return JobBuilder.newJob(StaleDownServerReminderJob.class)
                .withIdentity("stale-down-reminder", GROUP)
                .usingJobData(defaults)
                .storeDurably()
                .build();
    }

    public static CronTrigger reminderDaily(JobDetail job) {
        return TriggerBuilder.newTrigger()
                .withIdentity("stale-down-reminder-daily", GROUP)
                .forJob(job.getKey())
                .usingJobData("thresholdHours", 12)
                .usingJobData("windowKey", "daily")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 * * ?")
                        .inTimeZone(TimeZone.getTimeZone("Europe/Kyiv"))
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    public static SimpleTrigger reminderInterval(JobDetail job) {
        return TriggerBuilder.newTrigger()
                .withIdentity("stale-down-reminder-interval", GROUP)
                .forJob(job.getKey())
                .usingJobData("windowKey", "interval")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(5)
                        .repeatForever()
                        .withMisfireHandlingInstructionNextWithRemainingCount())
                .build();
    }

    public static JobDetail digestJob() {
        return JobBuilder.newJob(CatalogDigestJob.class)
                .withIdentity("catalog-digest", GROUP)
                .usingJobData("digestKey", "hourly")
                .storeDurably()
                .build();
    }

    public static CronTrigger digestHourly(JobDetail job) {
        return TriggerBuilder.newTrigger()
                .withIdentity("catalog-digest-hourly", GROUP)
                .forJob(job.getKey())
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?")
                        .inTimeZone(TimeZone.getTimeZone("Europe/Kyiv"))
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }
}
