package org.example.monitoring.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimestampValueRepository extends JpaRepository<TimestampValue, ValueKey> {
}
