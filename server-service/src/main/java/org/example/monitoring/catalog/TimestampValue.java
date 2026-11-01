package org.example.monitoring.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "timestamp_value")
public class TimestampValue {

    @EmbeddedId
    private ValueKey id = new ValueKey();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("entityId")
    @JoinColumn(name = "entity_id")
    private TrackedEntity entity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("attributeId")
    @JoinColumn(name = "attribute_id")
    private AttributeDefinition attribute;

    @Column(name = "value", nullable = false)
    private Instant value;

    public ValueKey getId() {
        return id;
    }

    public void setId(ValueKey id) {
        this.id = id;
    }

    public TrackedEntity getEntity() {
        return entity;
    }

    public void setEntity(TrackedEntity entity) {
        this.entity = entity;
    }

    public AttributeDefinition getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeDefinition attribute) {
        this.attribute = attribute;
    }

    public Instant getValue() {
        return value;
    }

    public void setValue(Instant value) {
        this.value = value;
    }
}
