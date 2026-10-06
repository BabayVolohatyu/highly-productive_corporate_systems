package org.example.monitoring.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "entity_type_attribute")
public class EntityTypeAttribute {

    @EmbeddedId
    private EntityTypeAttributeId id = new EntityTypeAttributeId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("entityTypeId")
    @JoinColumn(name = "entity_type_id")
    private EntityType entityType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("attributeId")
    @JoinColumn(name = "attribute_id")
    private AttributeDefinition attribute;

    @Column(nullable = false)
    private boolean required;

    @Column(name = "unique_within_type", nullable = false)
    private boolean uniqueWithinType;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public EntityTypeAttributeId getId() {
        return id;
    }

    public void setId(EntityTypeAttributeId id) {
        this.id = id;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public AttributeDefinition getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeDefinition attribute) {
        this.attribute = attribute;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isUniqueWithinType() {
        return uniqueWithinType;
    }

    public void setUniqueWithinType(boolean uniqueWithinType) {
        this.uniqueWithinType = uniqueWithinType;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
