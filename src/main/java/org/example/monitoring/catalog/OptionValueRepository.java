package org.example.monitoring.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OptionValueRepository extends JpaRepository<OptionValue, ValueKey> {

    @Query("""
            select value.id.entityId as entityId, attribute.code as attributeCode, option.code as optionCode
            from OptionValue value
            join value.attribute attribute
            join AttributeOption option on option.attribute = attribute and option.id.optionId = value.optionId
            where value.id.entityId in :entityIds
            """)
    List<OptionAssignment> findAssignments(@Param("entityIds") Collection<Long> entityIds);

    interface OptionAssignment {
        Long getEntityId();
        String getAttributeCode();
        String getOptionCode();
    }
}
