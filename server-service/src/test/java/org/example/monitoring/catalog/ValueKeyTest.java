package org.example.monitoring.catalog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValueKeyTest {

    @Test
    void equals_sameEntityAndAttribute_returnsTrue() {
        // Arrange
        ValueKey left = new ValueKey(1L, 2L);
        ValueKey right = new ValueKey(1L, 2L);

        // Act / Assert
        assertThat(left).isEqualTo(right);
        assertThat(left.hashCode()).isEqualTo(right.hashCode());
    }

    @Test
    void equals_differentAttribute_returnsFalse() {
        // Arrange
        ValueKey left = new ValueKey(1L, 2L);
        ValueKey right = new ValueKey(1L, 3L);

        // Act / Assert
        assertThat(left).isNotEqualTo(right);
    }
}
