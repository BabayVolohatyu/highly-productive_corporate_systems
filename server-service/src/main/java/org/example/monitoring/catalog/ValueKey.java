package org.example.monitoring.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ValueKey implements Serializable {

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "attribute_id")
    private Long attributeId;

    public ValueKey() {
    }

    public ValueKey(Long entityId, Long attributeId) {
        this.entityId = entityId;
        this.attributeId = attributeId;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
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
        if (!(other instanceof ValueKey that)) {
            return false;
        }
        return Objects.equals(entityId, that.entityId) && Objects.equals(attributeId, that.attributeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, attributeId);
    }
}
