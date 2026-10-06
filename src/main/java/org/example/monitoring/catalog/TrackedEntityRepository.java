package org.example.monitoring.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackedEntityRepository extends JpaRepository<TrackedEntity, Long> {

    List<TrackedEntity> findByEntityType_CodeOrderByIdAsc(String code);

    Optional<TrackedEntity> findByIdAndEntityType_Code(Long id, String code);

    long countByEntityType_Code(String code);
}
