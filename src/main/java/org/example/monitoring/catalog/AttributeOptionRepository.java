package org.example.monitoring.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttributeOptionRepository extends JpaRepository<AttributeOption, AttributeOptionId> {

    @Query("""
            select option from AttributeOption option
            join option.attribute attribute
            where attribute.code = :attributeCode
            order by option.id.optionId
            """)
    List<AttributeOption> findByAttributeCode(@Param("attributeCode") String attributeCode);

    @Query("""
            select option from AttributeOption option
            join option.attribute attribute
            where attribute.code = :attributeCode and option.code = :code
            """)
    Optional<AttributeOption> findByAttributeCodeAndCode(@Param("attributeCode") String attributeCode,
                                                         @Param("code") String code);
}
