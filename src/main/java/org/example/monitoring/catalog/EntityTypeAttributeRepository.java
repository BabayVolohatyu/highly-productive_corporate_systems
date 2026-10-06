package org.example.monitoring.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntityTypeAttributeRepository extends JpaRepository<EntityTypeAttribute, EntityTypeAttributeId> {

    @Query("""
            select link from EntityTypeAttribute link
            join fetch link.attribute attribute
            join fetch attribute.dataType
            join link.entityType entityType
            where entityType.code = :typeCode
            order by link.sortOrder
            """)
    List<EntityTypeAttribute> findLinks(@Param("typeCode") String typeCode);
}
