package org.example.monitoring.schedule;

import org.junit.jupiter.api.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoringScheduleTest {

    @Test
    void jobsAndTriggersDeclareNamesGroupsAndJobKeys() {
        JobDetail reminder = MonitoringSchedule.reminderJob();
        CronTrigger daily = MonitoringSchedule.reminderDaily(reminder);
        SimpleTrigger interval = MonitoringSchedule.reminderInterval(reminder);
        JobDetail digest = MonitoringSchedule.digestJob();
        CronTrigger hourly = MonitoringSchedule.digestHourly(digest);

        assertThat(reminder.getKey().getName()).isEqualTo("stale-down-reminder");
        assertThat(reminder.getKey().getGroup()).isEqualTo(MonitoringSchedule.GROUP);
        assertThat(daily.getKey().getName()).isEqualTo("stale-down-reminder-daily");
        assertThat(daily.getKey().getGroup()).isEqualTo(MonitoringSchedule.GROUP);
        assertThat(daily.getJobKey()).isEqualTo(reminder.getKey());
        assertThat(interval.getKey().getName()).isEqualTo("stale-down-reminder-interval");
        assertThat(interval.getKey().getGroup()).isEqualTo(MonitoringSchedule.GROUP);
        assertThat(interval.getJobKey()).isEqualTo(reminder.getKey());
        assertThat(digest.getKey().getName()).isEqualTo("catalog-digest");
        assertThat(digest.getKey().getGroup()).isEqualTo(MonitoringSchedule.GROUP);
        assertThat(hourly.getJobKey()).isEqualTo(digest.getKey());
        assertThat(hourly.getKey().getName()).isEqualTo("catalog-digest-hourly");
        assertThat(hourly.getKey().getGroup()).isEqualTo(MonitoringSchedule.GROUP);
    }

    @Test
    void triggerValuesOverrideJobDefaultsAndLeaveOtherKeysAlone() {
        JobDetail reminder = MonitoringSchedule.reminderJob();
        CronTrigger daily = MonitoringSchedule.reminderDaily(reminder);
        SimpleTrigger interval = MonitoringSchedule.reminderInterval(reminder);

        JobDataMap dailyMerged = merged(reminder, daily.getJobDataMap());
        assertThat(dailyMerged.getInt("thresholdHours")).isEqualTo(12);
        assertThat(dailyMerged.getString("windowKey")).isEqualTo("daily");

        JobDataMap intervalMerged = merged(reminder, interval.getJobDataMap());
        assertThat(intervalMerged.getInt("thresholdHours")).isEqualTo(24);
        assertThat(intervalMerged.getString("windowKey")).isEqualTo("interval");
    }

    @Test
    void eachTriggerSetsMisfireInstructionAndCronUsesEuropeKyiv() {
        JobDetail reminder = MonitoringSchedule.reminderJob();
        CronTrigger daily = MonitoringSchedule.reminderDaily(reminder);
        SimpleTrigger interval = MonitoringSchedule.reminderInterval(reminder);
        CronTrigger hourly = MonitoringSchedule.digestHourly(MonitoringSchedule.digestJob());

        assertThat(daily.getMisfireInstruction()).isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        assertThat(interval.getMisfireInstruction())
                .isEqualTo(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
        assertThat(hourly.getMisfireInstruction()).isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW);
        assertThat(daily.getTimeZone().getID()).isEqualTo("Europe/Kyiv");
        assertThat(hourly.getTimeZone().getID()).isEqualTo("Europe/Kyiv");
        assertThat(TimeZone.getTimeZone("Not/AZone").getID()).isEqualTo("GMT");
    }

    private static JobDataMap merged(JobDetail job, JobDataMap triggerData) {
        JobDataMap merged = new JobDataMap();
        merged.putAll(job.getJobDataMap());
        merged.putAll(triggerData);
        return merged;
    }
}
