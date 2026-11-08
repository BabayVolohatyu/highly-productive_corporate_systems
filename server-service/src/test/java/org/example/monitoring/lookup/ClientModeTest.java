package org.example.monitoring.lookup;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientModeTest {

    @Test
    void acceptsHttpAndGrpc() {
        assertThat(ClientMode.requireKnown("http")).isEqualTo("http");
        assertThat(ClientMode.requireKnown("grpc")).isEqualTo("grpc");
    }

    @Test
    void unknownModeFailsImmediately() {
        assertThatThrownBy(() -> ClientMode.requireKnown("smtp"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("smtp");
    }
}
