package org.example.monitoring.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AttributeOptionId implements Serializable {

    @Column(name = "attribute_id")
    private Long attributeId;

    @Column(name = "id")
    private Long optionId;

    public AttributeOptionId() {
    }

    public AttributeOptionId(Long attributeId, Long optionId) {
        this.attributeId = attributeId;
        this.optionId = optionId;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AttributeOptionId that)) {
            return false;
        }
        return Objects.equals(attributeId, that.attributeId) && Objects.equals(optionId, that.optionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeId, optionId);
    }
}
