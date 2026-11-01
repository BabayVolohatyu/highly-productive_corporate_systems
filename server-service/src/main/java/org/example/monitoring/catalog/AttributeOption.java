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
@Table(name = "attribute_option")
public class AttributeOption {

    @EmbeddedId
    private AttributeOptionId id = new AttributeOptionId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("attributeId")
    @JoinColumn(name = "attribute_id")
    private AttributeDefinition attribute;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false)
    private String label;

    public AttributeOptionId getId() {
        return id;
    }

    public void setId(AttributeOptionId id) {
        this.id = id;
    }

    public AttributeDefinition getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeDefinition attribute) {
        this.attribute = attribute;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
