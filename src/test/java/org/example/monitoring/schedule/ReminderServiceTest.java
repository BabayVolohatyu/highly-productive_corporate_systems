package org.example.monitoring.schedule;

import org.example.monitoring.catalog.TrackedEntityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private TrackedEntityRepository entities;
    @Mock
    private ServerReminderRepository reminders;
    @Mock
    private ReminderGate gate;
    @Mock
    private JobFailureLog failures;

    @InjectMocks
    private ReminderService reminderService;

    @Test
    void remind_secondRunForSameWindow_doesNotSaveAgain() {
        when(entities.findIdsByOption("SERVER", "status", "DOWN")).thenReturn(List.of(4L));
        when(reminders.existsByServerIdAndWindowKey(4L, "daily")).thenReturn(false, true);

        RemindResult first = reminderService.remind(12, "daily");
        RemindResult second = reminderService.remind(12, "daily");

        assertThat(first.created()).isEqualTo(1);
        assertThat(first.gaveUp()).isFalse();
        assertThat(second.created()).isZero();
        verify(reminders, times(1)).save(any(ServerReminder.class));
    }

    @Test
    void remind_whenGateKeepsFailing_stopsAfterThreeAttempts() {
        org.mockito.Mockito.doThrow(new TransientReminderException("mail relay down"))
                .when(gate).beforePersist();

        RemindResult result = reminderService.remind(24, "daily");

        assertThat(result.gaveUp()).isTrue();
        verify(failures, times(3)).record("daily", "mail relay down");
        verify(reminders, never()).save(any());
    }

    @Test
    void remind_whenGateFailsTwiceThenSucceeds_persistsOnThirdAttempt() {
        AtomicInteger calls = new AtomicInteger();
        org.mockito.Mockito.doAnswer(invocation -> {
            if (calls.incrementAndGet() < 3) {
                throw new TransientReminderException("temporary");
            }
            return null;
        }).when(gate).beforePersist();
        when(entities.findIdsByOption("SERVER", "status", "DOWN")).thenReturn(List.of());

        RemindResult result = reminderService.remind(24, "interval");

        assertThat(result.gaveUp()).isFalse();
        assertThat(calls.get()).isEqualTo(3);
        verify(failures, times(2)).record("interval", "temporary");
    }
}
