package org.example.monitoring.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BooleanValueRepository extends JpaRepository<BooleanValue, ValueKey> {
}
