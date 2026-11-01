package org.example.monitoring.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "catalog_digest")
public class CatalogDigest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "digest_key", nullable = false, unique = true, length = 64)
    private String digestKey;

    @Column(name = "server_count", nullable = false)
    private long serverCount;

    protected CatalogDigest() {
    }

    public static CatalogDigest create(String digestKey, long serverCount) {
        CatalogDigest row = new CatalogDigest();
        row.digestKey = digestKey;
        row.serverCount = serverCount;
        return row;
    }

    public String getDigestKey() {
        return digestKey;
    }

    public long getServerCount() {
        return serverCount;
    }
}
