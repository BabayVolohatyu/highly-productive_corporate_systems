package org.example.monitoring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.example.monitoring.server.ServerResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressMaskAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private AddressMaskAspect aspect;

    @Test
    void serverResponse_masksLastOctet() throws Throwable {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        ServerResponse raw = new ServerResponse(1L, "edge-1", "10.0.0.16", "STAGE", "UP", "x", now, now);
        when(joinPoint.proceed()).thenReturn(raw);

        Object result = aspect.maskAddress(joinPoint);

        assertThat(result).isInstanceOf(ServerResponse.class);
        assertThat(((ServerResponse) result).ipAddress()).isEqualTo("10.0.0.***");
        assertThat(((ServerResponse) result).hostname()).isEqualTo("edge-1");
    }

    @Test
    void nonResponse_leavesValueUnchanged() throws Throwable {
        when(joinPoint.proceed()).thenReturn("plain");

        Object result = aspect.maskAddress(joinPoint);

        assertThat(result).isEqualTo("plain");
    }
}
