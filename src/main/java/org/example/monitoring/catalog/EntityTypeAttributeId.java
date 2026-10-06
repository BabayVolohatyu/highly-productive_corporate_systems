package org.example.monitoring.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EntityTypeAttributeId implements Serializable {

    @Column(name = "entity_type_id")
    private Long entityTypeId;

    @Column(name = "attribute_id")
    private Long attributeId;

    public EntityTypeAttributeId() {
    }

    public EntityTypeAttributeId(Long entityTypeId, Long attributeId) {
        this.entityTypeId = entityTypeId;
        this.attributeId = attributeId;
    }

    public Long getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(Long entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EntityTypeAttributeId that)) {
            return false;
        }
        return Objects.equals(entityTypeId, that.entityTypeId) && Objects.equals(attributeId, that.attributeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityTypeId, attributeId);
    }
}
