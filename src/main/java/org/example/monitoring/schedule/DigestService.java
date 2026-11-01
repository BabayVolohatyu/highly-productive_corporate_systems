package org.example.monitoring.schedule;

import org.example.monitoring.catalog.TrackedEntityRepository;
import org.example.monitoring.server.ServerAttributes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DigestService {

    private final TrackedEntityRepository entities;
    private final CatalogDigestRepository digests;

    public DigestService(TrackedEntityRepository entities, CatalogDigestRepository digests) {
        this.entities = entities;
        this.digests = digests;
    }

    @Transactional
    public DigestResult publish(String digestKey) {
        if (digests.existsByDigestKey(digestKey)) {
            return DigestResult.unchanged(digestKey);
        }
        long serverCount = entities.countByEntityType_Code(ServerAttributes.ENTITY_TYPE);
        digests.save(CatalogDigest.create(digestKey, serverCount));
        return DigestResult.created(digestKey, serverCount);
    }
}
