package org.example.monitoring.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerReminderRepository extends JpaRepository<ServerReminder, Long> {

    boolean existsByServerIdAndWindowKey(Long serverId, String windowKey);
}
