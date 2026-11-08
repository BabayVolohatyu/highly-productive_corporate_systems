package org.example.notification;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationPageControllerTest {

    @Test
    void home_showsTheStoredNotifications() {
        NotificationInbox inbox = mock(NotificationInbox.class);
        NotificationView row = new NotificationView(1L, "evt-1", "SERVER_REGISTERED edge-1 UP", Instant.parse("2024-01-01T00:00:00Z"));
        when(inbox.list()).thenReturn(List.of(row));
        ConcurrentModel model = new ConcurrentModel();

        String view = new NotificationPageController(inbox).home(model);

        assertThat(view).isEqualTo("notifications/list");
        assertThat(model.getAttribute("notifications")).isEqualTo(List.of(row));
    }
}
