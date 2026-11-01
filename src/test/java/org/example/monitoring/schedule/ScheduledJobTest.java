package org.example.monitoring.schedule;

import org.junit.jupiter.api.Test;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ScheduledJobTest {

    @Test
    void reminderJob_missingKey_throwsBeforeCallingTheService() {
        ReminderService reminderService = mock(ReminderService.class);
        StaleDownServerReminderJob job = new StaleDownServerReminderJob();
        ReflectionTestUtils.setField(job, "reminderService", reminderService);
        JobExecutionContext context = mock(JobExecutionContext.class);
        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());

        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("thresholdHours");
        verifyNoInteractions(reminderService);
    }

    @Test
    void reminderJob_readsParametersAndCallsTheServiceOnce() {
        ReminderService reminderService = mock(ReminderService.class);
        when(reminderService.remind(12, "daily")).thenReturn(RemindResult.stored("daily", 1));
        StaleDownServerReminderJob job = new StaleDownServerReminderJob();
        ReflectionTestUtils.setField(job, "reminderService", reminderService);
        JobDataMap data = new JobDataMap();
        data.put("thresholdHours", 12);
        data.put("windowKey", "daily");
        JobExecutionContext context = mock(JobExecutionContext.class);
        when(context.getMergedJobDataMap()).thenReturn(data);

        job.execute(context);

        verify(reminderService).remind(12, "daily");
    }

    @Test
    void reminderAndDigestJobsDisallowConcurrentExecution() {
        assertThat(StaleDownServerReminderJob.class.getAnnotation(DisallowConcurrentExecution.class)).isNotNull();
        assertThat(CatalogDigestJob.class.getAnnotation(DisallowConcurrentExecution.class)).isNotNull();
    }
}
