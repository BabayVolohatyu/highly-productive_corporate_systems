package org.example.monitoring.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntityTypeRepository extends JpaRepository<EntityType, Long> {

    Optional<EntityType> findByCode(String code);
}
