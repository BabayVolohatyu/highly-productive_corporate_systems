package org.example.monitoring.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DecimalValueRepository extends JpaRepository<DecimalValue, ValueKey> {
}
