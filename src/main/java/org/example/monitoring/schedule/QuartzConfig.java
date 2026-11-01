package org.example.monitoring.schedule;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    JobDetail reminderJobDetail() {
        return MonitoringSchedule.reminderJob();
    }

    @Bean
    Trigger reminderDailyTrigger(JobDetail reminderJobDetail) {
        return MonitoringSchedule.reminderDaily(reminderJobDetail);
    }

    @Bean
    Trigger reminderIntervalTrigger(JobDetail reminderJobDetail) {
        return MonitoringSchedule.reminderInterval(reminderJobDetail);
    }

    @Bean
    JobDetail digestJobDetail() {
        return MonitoringSchedule.digestJob();
    }

    @Bean
    Trigger digestHourlyTrigger(JobDetail digestJobDetail) {
        return MonitoringSchedule.digestHourly(digestJobDetail);
    }
}
