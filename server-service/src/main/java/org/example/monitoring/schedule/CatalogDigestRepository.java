package org.example.monitoring.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogDigestRepository extends JpaRepository<CatalogDigest, Long> {

    boolean existsByDigestKey(String digestKey);
}
