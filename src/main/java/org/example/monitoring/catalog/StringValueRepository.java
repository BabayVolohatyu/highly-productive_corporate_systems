package org.example.monitoring.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StringValueRepository extends JpaRepository<StringValue, ValueKey> {

    @Query("""
            select value.id.entityId as entityId, attribute.code as attributeCode, value.value as value
            from StringValue value
            join value.attribute attribute
            where value.id.entityId in :entityIds
            """)
    List<StringAssignment> findAssignments(@Param("entityIds") Collection<Long> entityIds);

    @Query("""
            select count(value) > 0 from StringValue value
            join value.attribute attribute
            join value.entity entity
            join entity.entityType entityType
            where entityType.code = :typeCode
              and attribute.code = :attributeCode
              and value.value = :value
              and (:excludeEntityId is null or entity.id <> :excludeEntityId)
            """)
    boolean existsForType(@Param("typeCode") String typeCode,
                          @Param("attributeCode") String attributeCode,
                          @Param("value") String value,
                          @Param("excludeEntityId") Long excludeEntityId);

    interface StringAssignment {
        Long getEntityId();
        String getAttributeCode();
        String getValue();
    }
}
