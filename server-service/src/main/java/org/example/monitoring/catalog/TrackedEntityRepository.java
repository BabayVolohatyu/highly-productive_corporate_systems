package org.example.monitoring.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrackedEntityRepository extends JpaRepository<TrackedEntity, Long> {

    List<TrackedEntity> findByEntityType_CodeOrderByIdAsc(String code);

    Optional<TrackedEntity> findByIdAndEntityType_Code(Long id, String code);

    long countByEntityType_Code(String code);

    @Query("""
            select entity.id
            from TrackedEntity entity
            where entity.entityType.code = :entityType
              and exists (
                select 1
                from OptionValue value, AttributeOption option
                where value.entity = entity
                  and value.attribute.code = :attributeCode
                  and option.attribute = value.attribute
                  and option.id.optionId = value.optionId
                  and option.code = :optionCode
              )
            """)
    List<Long> findIdsByOption(@Param("entityType") String entityType,
                               @Param("attributeCode") String attributeCode,
                               @Param("optionCode") String optionCode);
}
